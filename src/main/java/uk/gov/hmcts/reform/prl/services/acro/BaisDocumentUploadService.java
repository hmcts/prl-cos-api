package uk.gov.hmcts.reform.prl.services.acro;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroResponse;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
    private final StatementOfServiceValidationService statementOfServiceValidationService;

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

            AcroResponse acroResponse = acroCaseDataService.getCaseData(sysUserToken);

            File csvFile = csvWriter.createCsvFileWithHeaders();

            if (acroResponse.getTotal() == 0 || acroResponse.getCases() == null || acroResponse.getCases().isEmpty()) {
                log.info("Search has resulted empty cases with Final FL404a orders, creating empty CSV file");
                csvWriter.appendCsvRowToFile(csvFile, AcroCaseData.builder().build(), false, null);
            } else {
                processCasesAndCreateCsvRows(csvFile, acroResponse, sysUserToken);
            }

            log.info("All FL404a documents and manifest files prepared. Creating zip archive...");
            acroZipService.zip();

            log.info(
                "*** Total time taken to run Bais Document upload task - {}s ***",
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void processCasesAndCreateCsvRows(File csvFile, AcroResponse acroResponse, String sysUserToken) {
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
                            csvWriter.appendCsvRowToFile(csvFile, caseData, true, englishFile.getName());
                        }

                        if (statementOfServiceValidationService.isOrderServedViaStatementOfService(order, caseData.getStmtOfServiceForOrder(), caseData)) {
                            String statementOfServiceFileName = englishFileName.replace(".pdf", "_served.pdf");
                            pdfExtractorService.downloadPdf(
                                statementOfServiceFileName,
                                caseId,
                                order.getOrderDocument(),
                                sysUserToken
                            );
                            log.debug("Downloaded served version of FL404a document for case {}: {}", caseId, statementOfServiceFileName);
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

    private String getFilePrefix(String caseId, LocalDateTime orderCreatedDate) {
        ZonedDateTime zdt = ZonedDateTime.of(orderCreatedDate, ZoneId.systemDefault());
        return sourceDirectory + "/FL404A-" + caseId + "-" + zdt.toEpochSecond();
    }
}
