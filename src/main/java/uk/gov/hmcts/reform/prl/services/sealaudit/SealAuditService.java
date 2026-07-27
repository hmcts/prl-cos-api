package uk.gov.hmcts.reform.prl.services.sealaudit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.sealaudit.SealDetectionService.SealStatus;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static uk.gov.service.notify.NotificationClient.prepareUpload;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SealAuditService {

    private final CoreCaseDataApi coreCaseDataApi;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUserService systemUserService;
    private final CaseDocumentClient caseDocumentClient;
    private final SealDetectionService sealDetectionService;
    private final NotificationClient notificationClient;
    private final ObjectMapper objectMapper;

    @Value("${seal-audit.search-case-type-id:PRLAPPS}")
    private String searchCaseTypeId;

    @Value("${seal-audit.batch-size:100}")
    private int batchSize;

    @Value("${seal-audit.batch-delay-seconds:5}")
    private int batchDelaySeconds;

    @Value("${seal-audit.page-size:500}")
    private int pageSize;

    @Value("${seal-audit.from-date:2024-04-01}")
    private String fromDateStr;

    @Value("${seal-audit.email.to:}")
    private String toEmailAddress;

    @Value("${seal-audit.email.template-id:}")
    private String emailTemplateId;

    @Value("${seal-audit.email.enabled:false}")
    private boolean emailEnabled;

    private static final DateTimeFormatter LOG_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CSV_HEADER = "case_reference,court_name,order_type,order_filename,date_order_made,"
        + "seal_status,order_upload_timestamp";

    public void runAudit() {
        log.info("*** Starting Seal Audit Task ***");
        long startTime = System.currentTimeMillis();

        LocalDate fromDate = parseDate(fromDateStr).orElse(LocalDate.of(2024, 4, 1));
        LocalDate toDate = LocalDate.now();

        log.info("Audit case created date range: {} to {}", fromDate, toDate);

        String sysUserToken = systemUserService.getSysUserToken();
        String s2sToken = authTokenGenerator.generate();

        int totalCasesProcessed = 0;
        int totalOrders = 0;
        int missingSeals = 0;
        int presentSeals = 0;
        int errors = 0;
        boolean foundAnyCases = false;
        List<String> csvRows = new ArrayList<>();

        try {
            int from = 0;

            while (true) {
                SearchResult searchResult = searchServedOrders(
                    sysUserToken,
                    s2sToken,
                    fromDate,
                    toDate,
                    from
                );

                if (searchResult == null || searchResult.getCases() == null || searchResult.getCases().isEmpty()) {
                    log.info("No cases returned for page from {} size {}", from, pageSize);
                    break;
                }

                List<CaseDetails> cases = searchResult.getCases();
                foundAnyCases = true;

                log.info("Found {} cases to process from offset {}", cases.size(), from);

                for (CaseDetails caseDetails : cases) {
                    totalCasesProcessed++;
                    log.info("Processing Case {}", caseDetails.getId());

                    try {
                        CaseData caseData = objectMapper.convertValue(caseDetails.getData(), CaseData.class);

                        String caseReference = String.valueOf(caseDetails.getId());
                        String courtName = caseData.getCourtName();

                        List<Element<OrderDetails>> orderCollection = caseData.getOrderCollection();
                        if (CollectionUtils.isEmpty(orderCollection)) {
                            continue;
                        }

                        for (Element<OrderDetails> orderElement : orderCollection) {
                            OrderDetails order = orderElement.getValue();

                            if (!isServedPdfOrder(order)) {
                                continue;
                            }

                            totalOrders++;
                            Document orderDoc = order.getOrderDocument();

                            SealStatus status = checkSealStatus(orderDoc, sysUserToken, s2sToken, caseReference);

                            String orderType = order.getOrderTypeId();
                            String firstServedDateTime = getFirstServedDateTime(order);
                            String orderUploadTimestamp = orderDoc.getUploadTimeStamp() != null
                                ? orderDoc.getUploadTimeStamp().toString()
                                : firstServedDateTime;
                            String orderFilename = orderDoc.getDocumentFileName();
                            String dateOrderMade = order.getOtherDetails() != null
                                ? order.getOtherDetails().getOrderMadeDate() : null;

                            switch (status) {
                                case PRESENT -> presentSeals++;
                                case MISSING -> {
                                    missingSeals++;
                                    logOrderResult(
                                        caseReference,
                                        courtName,
                                        orderElement.getId().toString(),
                                        orderType,
                                        orderUploadTimestamp,
                                        orderFilename,
                                        dateOrderMade,
                                        firstServedDateTime,
                                        status
                                    );
                                    csvRows.add(buildCsvRow(
                                        caseReference,
                                        courtName,
                                        orderType,
                                        orderUploadTimestamp,
                                        orderFilename,
                                        dateOrderMade,
                                        status
                                    ));
                                }
                                case ERROR -> {
                                    errors++;
                                    logOrderResult(
                                        caseReference,
                                        courtName,
                                        orderElement.getId().toString(),
                                        orderType,
                                        orderUploadTimestamp,
                                        orderFilename,
                                        dateOrderMade,
                                        firstServedDateTime,
                                        status
                                    );
                                    csvRows.add(buildCsvRow(
                                        caseReference,
                                        courtName,
                                        orderType,
                                        orderUploadTimestamp,
                                        orderFilename,
                                        dateOrderMade,
                                        status
                                    ));
                                }
                                default -> log.warn("Unexpected seal status: {}", status);
                            }
                        }

                    } catch (Exception e) {
                        log.error("Error processing case {}: {}", caseDetails.getId(), e.getMessage());
                        errors++;
                    }

                    if (totalCasesProcessed % batchSize == 0) {
                        log.info(
                            "Processed {} cases, pausing for {} seconds",
                            totalCasesProcessed,
                            batchDelaySeconds
                        );
                        TimeUnit.SECONDS.sleep(batchDelaySeconds);
                    }
                }

                if (cases.size() < pageSize) {
                    log.info("Final page reached. Cases in final page: {}", cases.size());
                    break;
                }

                from += pageSize;
            }

            if (!foundAnyCases) {
                log.info("No cases with served orders found");
                sendSummaryEmail(0, 0, 0, 0, 0, csvRows);
                return;
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Seal audit interrupted", e);
        } catch (Exception e) {
            log.error("Error running seal audit", e);
        }

        long duration = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
        log.info("*** Seal Audit Complete ***");
        log.info("Total cases processed: {}", totalCasesProcessed);
        log.info("Total orders checked: {}", totalOrders);
        log.info("Seals present: {}", presentSeals);
        log.info("Seals missing: {}", missingSeals);
        log.info("Errors: {}", errors);
        log.info("Duration: {}s", duration);

        sendSummaryEmail(totalOrders, presentSeals, missingSeals, errors, duration, csvRows);
    }

    private SearchResult searchServedOrders(
        String userToken,
        String s2sToken,
        LocalDate fromDate,
        LocalDate toDate,
        int from
    ) {
        String query = """
            {
              "from": %s,
              "size": %s,
              "sort": [
                { "created_date": "asc" },
                { "reference.keyword": "asc" }
              ],
              "query": {
                "bool": {
                  "must": [
                    {
                      "exists": {
                        "field": "data.orderCollection"
                      }
                    },
                    {
                      "range": {
                        "created_date": {
                          "gte": "%sT00:00:00",
                          "lt": "%sT00:00:00"
                        }
                      }
                    }
                  ],
                  "must_not": [
                    {
                      "terms": {
                        "state.keyword": [
                          "AWAITING_SUBMISSION_TO_HMCTS",
                          "AWAITING_RESUBMISSION_TO_HMCTS",
                          "SUBMITTED_PAID",
                          "SUBMITTED_NOT_PAID",
                          "CASE_WITHDRAWN",
                          "DELETED"
                        ]
                      }
                    }
                  ]
                }
              },
              "_source": [
                "data.orderCollection",
                "data.courtName",
                "reference",
                "created_date"
              ]
            }
            """.formatted(from, pageSize, fromDate, toDate.plusDays(1));

        log.info(
            "Executing search query for cases created from {} to {} exclusive, from {}, size {}",
            fromDate,
            toDate.plusDays(1),
            from,
            pageSize
        );

        return coreCaseDataApi.searchCases(userToken, s2sToken, searchCaseTypeId, query);
    }

    private boolean isServedPdfOrder(OrderDetails order) {
        if (order == null || order.getOrderDocument() == null) {
            return false;
        }

        Document doc = order.getOrderDocument();
        if (doc.getDocumentFileName() == null || doc.getDocumentBinaryUrl() == null) {
            return false;
        }

        if (!doc.getDocumentFileName().toLowerCase().endsWith(".pdf")) {
            return false;
        }

        if (order.getServeOrderDetails() == null) {
            return false;
        }

        List<Element<ServedParties>> servedParties = order.getServeOrderDetails().getServedParties();
        return servedParties != null && !servedParties.isEmpty();
    }

    private Optional<LocalDate> parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(dateStr));
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr);
            return Optional.empty();
        }
    }

    private String getFirstServedDateTime(OrderDetails order) {
        if (order.getServeOrderDetails() == null
            || order.getServeOrderDetails().getServedParties() == null
            || order.getServeOrderDetails().getServedParties().isEmpty()) {
            return null;
        }

        return order.getServeOrderDetails().getServedParties().stream()
            .map(Element::getValue)
            .map(ServedParties::getServedDateTime)
            .filter(Objects::nonNull)
            .min(LocalDateTime::compareTo)
            .map(dt -> dt.format(LOG_DATE_FORMAT))
            .orElse(null);
    }

    private SealStatus checkSealStatus(Document document, String userToken, String s2sToken, String caseRef) {
        try {
            ResponseEntity<Resource> response = caseDocumentClient.getDocumentBinary(
                userToken,
                s2sToken,
                document.getDocumentBinaryUrl()
            );

            if (response.getBody() == null) {
                log.warn("Empty response body for document in case {}", caseRef);
                return SealStatus.ERROR;
            }

            byte[] pdfBytes = response.getBody().getInputStream().readAllBytes();
            return sealDetectionService.detectSeal(pdfBytes);

        } catch (Exception e) {
            log.error("Failed to download/check document for case {}: {}", caseRef, e.getMessage());
            return SealStatus.ERROR;
        }
    }

    private void logOrderResult(
        String caseReference,
        String courtName,
        String orderCollectionId,
        String orderType,
        String orderUploadTimestamp,
        String orderFilename,
        String dateOrderMade,
        String firstServedDatetime,
        SealStatus sealStatus
    ) {
        log.info("SEAL_AUDIT_RESULT | case_reference={} | court_name={} | order_collection_id={} | "
                     + "order_type={} | order_upload_timestamp={} | order_filename={} | date_order_made={} | "
                     + "first_served_datetime={} | seal_status={}",
                 caseReference,
                 courtName,
                 orderCollectionId,
                 orderType,
                 orderUploadTimestamp,
                 orderFilename,
                 dateOrderMade,
                 firstServedDatetime,
                 sealStatus
        );
    }

    private String buildCsvRow(
        String caseReference,
        String courtName,
        String orderType,
        String orderUploadTimestamp,
        String orderFilename,
        String dateOrderMade,
        SealStatus sealStatus
    ) {
        return String.join(",",
                           escapeCsv(caseReference),
                           escapeCsv(courtName),
                           escapeCsv(orderType),
                           escapeCsv(orderFilename),
                           escapeCsv(dateOrderMade),
                           sealStatus.name(),
                           escapeCsv(orderUploadTimestamp)
        );
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void sendSummaryEmail(
        int totalOrders,
        int presentSeals,
        int missingSeals,
        int errors,
        long durationSeconds,
        List<String> csvRows
    ) {
        if (!emailEnabled || toEmailAddress == null || toEmailAddress.isBlank()) {
            log.info("Email not enabled or no recipient configured, skipping email");
            return;
        }

        if (emailTemplateId == null || emailTemplateId.isBlank()) {
            log.error("Email template ID not configured, skipping email");
            return;
        }

        List<String> recipients = Arrays.stream(toEmailAddress.split(","))
            .map(String::trim)
            .filter(email -> !email.isBlank())
            .toList();

        if (recipients.isEmpty()) {
            log.info("No valid recipient configured, skipping email");
            return;
        }

        try {
            String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
            String statusSummary = missingSeals == 0 && errors == 0 ? "All seals present" : "Issues found";

            StringBuilder csvContent = new StringBuilder();
            csvContent.append(CSV_HEADER).append("\n");
            for (String row : csvRows) {
                csvContent.append(row).append("\n");
            }

            Map<String, Object> templateVars = new HashMap<>();
            templateVars.put("date", dateStr);
            templateVars.put("status", statusSummary);
            templateVars.put("total_orders", String.valueOf(totalOrders));
            templateVars.put("seals_present", String.valueOf(presentSeals));
            templateVars.put("seals_missing", String.valueOf(missingSeals));
            templateVars.put("errors", String.valueOf(errors));
            templateVars.put("duration", String.valueOf(durationSeconds));

            byte[] csvBytes = csvContent.toString().getBytes();
            Object fileUpload = prepareUpload(csvBytes, true, false, "26 weeks");
            templateVars.put("link_to_file", fileUpload);

            for (String recipient : recipients) {
                notificationClient.sendEmail(
                    emailTemplateId,
                    recipient,
                    templateVars,
                    "seal-audit-" + dateStr
                );
            }

            log.info("Seal audit summary email sent successfully to {}", recipients);

        } catch (NotificationClientException e) {
            log.error("Error sending seal audit summary email via Gov Notify", e);
        }
    }
}
