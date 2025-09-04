package uk.gov.hmcts.reform.prl.services.acro;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import java.util.Optional;
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

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            File jsonFile = new File(outputDirectory, "AcroResponse.json");
            objectMapper.writeValue(jsonFile, acroResponse);

            File csvFile = csvWriter.createCsvFileWithHeaders();

            if (acroResponse.getTotal() == 0 || acroResponse.getCases() == null || acroResponse.getCases().isEmpty()) {
                log.info("Search has resulted empty cases with Final FL404a orders, creating empty CSV file");
                csvWriter.appendCsvRowToFile(csvFile, AcroCaseData.builder().build(), false, null);
            } else {
                processCasesAndCreateCsvRows(csvFile, acroResponse, sysUserToken);
            }

            File outputCsv = new File(outputDirectory, "AcroReport.csv");
            Files.copy(csvFile.toPath(), outputCsv.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

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

        for (var acroCase : acroResponse.getCases()) {
            AcroCaseData caseData = acroCase.getCaseData();
            String caseId = String.valueOf(acroCase.getId());

            if (caseData.getFl404Orders() != null && !caseData.getFl404Orders().isEmpty()) {
                log.debug("Processing case {} with {} FL404 orders", caseId, caseData.getFl404Orders().size());

                for (var order : caseData.getFl404Orders()) {
                    String filePrefix = getFilePrefix(caseId, order.getDateCreated());
                    String englishFileName = filePrefix + ".pdf";
                    String welshFileName = filePrefix + "-welsh.pdf";

                    // Process English PDF
                        processDocument(csvFile, caseData, caseId, order.getDateCreated(),
                                                               order.getOrderDocument(), englishFileName, sysUserToken);
                    // Process Welsh PDF
                        processDocument(csvFile, caseData, caseId, order.getDateCreated(),
                                                             order.getOrderDocumentWelsh(), welshFileName, sysUserToken);
                }
            } else {
                log.debug("Skipping case {} - no FL404 orders found", caseId);
            }
        }

        log.info("FL404a document processing completed. Successfully processed {}/{} documents",
                successfullyProcessed, totalDocumentsProcessed);
    }

    private void processDocument(File csvFile, AcroCaseData caseData, String caseId,
                                  LocalDateTime orderCreatedDate,
                                  uk.gov.hmcts.reform.prl.models.documents.Document document,
                                  String name, String sysUserToken) {

        try {
            Optional<File> downloadedFile = pdfExtractorService.downloadPdf(
                caseId, document, sysUserToken
            );

            if (downloadedFile.isPresent()) {
                File file = downloadedFile.get();

                appendCsvRow(csvFile, caseData, file.getName());
                log.debug("Successfully processed {} FL404a document for case {}: {}",
                         documentType, caseId, file.getName());
            } else {
                log.warn("Failed to download {} FL404a document for case {}", documentType, caseId);
            }
        } catch (Exception e) {
            log.error("Error processing {} FL404a document for case {}: {}",
                     documentType, caseId, e.getMessage(), e);
        }
    }

    private void appendCsvRow(File csvFile, AcroCaseData caseData, String fileName) {
        try {
            csvWriter.appendCsvRowToFile(csvFile, caseData, true, fileName);
        } catch (IOException e) {
            log.error("Failed to append CSV row for file {}: {}", fileName, e.getMessage());
        }
    }

    private String getFilePrefix(String caseId, LocalDateTime orderCreatedDate) {
        ZonedDateTime zdt = ZonedDateTime.of(orderCreatedDate, ZoneId.systemDefault());
        return sourceDirectory + "/FL404A-" + caseId + "-" + zdt.toEpochSecond();
    }

}
