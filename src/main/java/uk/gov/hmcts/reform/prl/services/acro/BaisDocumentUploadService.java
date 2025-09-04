package uk.gov.hmcts.reform.prl.services.acro;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroResponse;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BaisDocumentUploadService {

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;

    private final AcroCaseDataService acroCaseDataService;
    private final AcroZipService acroZipService;
    private final CsvWriter csvWriter;
    private final PdfExtractorService pdfExtractorService;

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

    private void processCasesAndCreateCsvRows(File csvFile, AcroResponse acroResponse, String sysUserToken) throws IOException {
        log.info("Processing {} cases for FL404a document extraction and CSV generation", acroResponse.getCases().size());
        int totalDocumentsProcessed = 0;
        int successfullyProcessed = 0;
        int casesWithoutFl404Orders = 0;
        int documentsWithNullContent = 0;

        for (var acroCase : acroResponse.getCases()) {
            AcroCaseData caseData = acroCase.getCaseData();
            String caseId = String.valueOf(acroCase.getId());

            if (caseData.getFl404Orders() != null && !caseData.getFl404Orders().isEmpty()) {
                log.debug("Processing case {} with {} FL404 orders", caseId, caseData.getFl404Orders().size());

                for (var order : caseData.getFl404Orders()) {
                    LocalDateTime orderCreatedDate = order.getDateCreated();
                    String filePrefix = getFilePrefix(caseId, orderCreatedDate);

                    // Process English PDF
                    if (order.getOrderDocument() != null) {
                        totalDocumentsProcessed++;
                        String englishFileName = filePrefix + ".pdf";

                        try {
                            File englishFile = pdfExtractorService.downloadPdf(
                                englishFileName,
                                caseId,
                                order.getOrderDocument(),
                                sysUserToken
                            );

                            if (englishFile != null) {
                                csvWriter.appendCsvRowToFile(csvFile, caseData, true, englishFile.getName());
                                successfullyProcessed++;
                                log.debug("Successfully processed English FL404a document for case {}: {}",
                                         caseId, englishFile.getName());
                            } else {
                                log.warn("Failed to download English FL404a document for case {}", caseId);
                            }
                        } catch (Exception e) {
                            log.error("Error processing English FL404a document for case {}: {}",
                                     caseId, e.getMessage(), e);
                        }
                    } else {
                        documentsWithNullContent++;
                        log.info("Case {} has FL404 order but English document is null - not processing", caseId);
                    }

                    // Process Welsh PDF
                    if (order.getOrderDocumentWelsh() != null) {
                        totalDocumentsProcessed++;
                        String welshFileName = filePrefix + "-Welsh.pdf";

                        try {
                            File welshFile = pdfExtractorService.downloadPdf(
                                welshFileName,
                                caseId,
                                order.getOrderDocumentWelsh(),
                                sysUserToken
                            );

                            if (welshFile != null) {
                                csvWriter.appendCsvRowToFile(csvFile, caseData, true, welshFile.getName());
                                successfullyProcessed++;
                                log.debug("Successfully processed Welsh FL404a document for case {}: {}",
                                         caseId, welshFile.getName());
                            } else {
                                log.warn("Failed to download Welsh FL404a document for case {}", caseId);
                            }
                        } catch (Exception e) {
                            log.error("Error processing Welsh FL404a document for case {}: {}",
                                     caseId, e.getMessage(), e);
                        }
                    } else {
                        documentsWithNullContent++;
                        log.info("Case {} has FL404 order but Welsh document is null - not processing", caseId);
                    }
                }
            } else {
                casesWithoutFl404Orders++;
                log.info("Case {} has no FL404 orders - skipping case entirely", caseId);
            }
        }

        log.info("FL404a document processing completed. Successfully processed {}/{} documents. "
                        + "Cases without FL404 orders: {}. Documents with null content: {}",
                successfullyProcessed, totalDocumentsProcessed, casesWithoutFl404Orders, documentsWithNullContent);
    }

    private String getFilePrefix(String caseId, LocalDateTime orderCreatedDate) {
        ZonedDateTime zdt = ZonedDateTime.of(orderCreatedDate, ZoneId.systemDefault());
        return sourceDirectory + "/FL404A-" + caseId + "-" + zdt.toEpochSecond();
    }

}
