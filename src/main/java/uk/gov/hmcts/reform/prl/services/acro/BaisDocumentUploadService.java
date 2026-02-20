package uk.gov.hmcts.reform.prl.services.acro;


import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.exception.BaisDocumentUploadRuntimeException;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroResponse;
import uk.gov.hmcts.reform.prl.models.dto.acro.CsvData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.reform.prl.services.SendgridService.MAIL_SEND;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BaisDocumentUploadService {

    private final SystemUserService systemUserService;
    private final AcroCaseDataService acroCaseDataService;
    private final AcroZipService acroZipService;
    private final CsvWriter csvWriter;
    private final PdfExtractorService pdfExtractorService;
    private final SftpService sftpService;
    private final SendGrid sendGrid;

    @Value("${acro.source-directory}")
    private String sourceDirectory;
    @Value("${acro.output-directory}")
    private String outputDirectory;

    public void uploadFL404Orders() {
        long startTime = System.currentTimeMillis();
        log.info("inside uploadFL404Orders");

        String sysUserToken = systemUserService.getSysUserToken();
        try {
            Files.createDirectories(Path.of(sourceDirectory));
            Files.createDirectories(Path.of(outputDirectory));

            AcroResponse acroResponse = acroCaseDataService.getNonMolestationData(sysUserToken);

            File csvFile = csvWriter.createCsvFileWithHeaders();

            if (acroResponse.getTotal() == 0 || acroResponse.getCases() == null || acroResponse.getCases().isEmpty()) {
                log.info("Search has resulted empty cases with Final FL404a orders, creating empty CSV file");
                csvWriter.appendCsvRowToFile(csvFile, CsvData.builder().build(), null);
            } else {
                createCsvRowsForFl404aOrders(csvFile, acroResponse, sysUserToken);
            }

            log.info("All FL404a documents and manifest files prepared. Creating zip archive...");
            String archivePath = acroZipService.zip();
            sftpService.uploadFile(new File(archivePath));
            log.info(
                "*** Total time taken to run Bais Document upload task - {}s ***",
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)
            );
            sendMail(new File(archivePath));
        } catch (Exception e) {
            throw new BaisDocumentUploadRuntimeException(
                "Document upload to Bais has failed for the run at "
                    + LocalDateTime.now(), e
            );
        }
    }

    private void sendMail(File file) {
        try {
            String subject = "Bais Document Upload";
            Content content = new Content("text/plain", " ");
            Attachments attachments = new Attachments();
            byte[] fileContent = Files.readAllBytes(file.toPath());
            String encoded = Base64.getEncoder().encodeToString(fileContent);
            attachments.setContent(encoded);
            attachments.setFilename(subject + ".zip");
            attachments.setType("application/zip");
            attachments.setDisposition("attachment");
            Mail mail = new Mail(
                new Email("dharmendra.kumar1@hmcts.net"), subject,
                new Email("dharmendra.kumar1@hmcts.net"), content
            );
            mail.addAttachments(attachments);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint(MAIL_SEND);
            request.setBody(mail.build());
            log.info("Initiating email through sendgrid");
            sendGrid.api(request);
            log.info("Notification to RPA sent successfully");
        } catch (IOException ex) {
            log.error(ex.getMessage());
        }
    }

    private void createCsvRowsForFl404aOrders(File csvFile, AcroResponse acroResponse, String sysUserToken) {
        log.info(
            "Processing {} cases for FL404a document extraction and CSV generation",
            acroResponse.getCases().size()
        );

        for (var acroCase : acroResponse.getCases()) {
            AcroCaseData caseData = acroCase.getCaseData();
            String caseId = String.valueOf(acroCase.getId());

            if (caseData.getFl404Orders() != null && !caseData.getFl404Orders().isEmpty()) {
                log.debug("Processing case {} with {} FL404 orders", caseId, caseData.getFl404Orders().size());

                for (var order : caseData.getFl404Orders()) {
                    LocalDateTime orderCreatedDate = order.getDateCreated();
                    String filePrefix = getFilePrefix(caseId, orderCreatedDate);
                    String englishFileName = filePrefix + ".pdf";
                    String welshFileName = filePrefix + "-welsh.pdf";

                    try {

                        File englishFile = pdfExtractorService.downloadPdf(
                            englishFileName,
                            caseId,
                            order.getOrderDocument(),
                            sysUserToken
                        );
                        pdfExtractorService.downloadPdf(
                            welshFileName,
                            caseId,
                            order.getOrderDocumentWelsh(),
                            sysUserToken
                        );

                        if (Optional.ofNullable(englishFile).isPresent()) {
                            CsvData csvData = prepareDataForCsv(caseData, order);
                            csvWriter.appendCsvRowToFile(csvFile, csvData, englishFile.getName());
                        }
                        log.info(
                            "FL404a document processing completed. Successfully processed {}/{} documents",
                            englishFileName,
                            welshFileName
                        );
                    } catch (Exception e) {
                        log.warn("Failed to download FL404a document for case {}", caseId);
                    }
                }
            } else {
                log.debug("Skipping case {} - no FL404 orders found", caseId);
            }


        }

    }

    private CsvData prepareDataForCsv(AcroCaseData caseData, OrderDetails order) {

        return CsvData.builder()
            .id(caseData.getId())
            .caseTypeOfApplication(caseData.getCaseTypeOfApplication())
            .applicant(caseData.getApplicant())
            .respondent(caseData.getRespondent())
            .courtName(caseData.getCourtName())
            .courtEpimsId(caseData.getCourtEpimsId())
            .courtTypeId(caseData.getCourtTypeId())
            .dateOrderMade(formateOrderMadeDate(order))
            .orderExpiryDate(getOrderExpiryDate(order))
            .familymanCaseNumber(caseData.getFamilymanCaseNumber())
            .build();
    }

    private static String formateOrderMadeDate(OrderDetails order) {

        String orderMadeDate = order.getOtherDetails().getOrderMadeDate();
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate date = LocalDate.parse(orderMadeDate, inputFormatter);
        return date.format(outputFormatter);
    }

    private String getFilePrefix(String caseId, LocalDateTime orderCreatedDate) {
        ZonedDateTime zdt = ZonedDateTime.of(orderCreatedDate, ZoneId.systemDefault());
        return sourceDirectory + "/FL404A-" + caseId + "-" + zdt.toEpochSecond();
    }

    private String getOrderExpiryDate(OrderDetails order) {

        if (order.getFl404CustomFields() != null && order.getFl404CustomFields().getOrderSpecifiedDateTime() != null) {
            return order.getFl404CustomFields().getOrderSpecifiedDateTime()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy_HH:mm"));
        }
        return null;
    }
}
