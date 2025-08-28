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

            // 28th August - Write AcroResponse to JSON file for QA
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            File jsonFile = new File(outputDirectory, "AcroResponse.json");
            objectMapper.writeValue(jsonFile, acroResponse);

            File csvFile;
            if (acroResponse.getTotal() == 0 || acroResponse.getCases() == null || acroResponse.getCases().isEmpty()) {
                log.info("Search has resulted empty cases with Final FL404a orders, so need to send empty csv file");
                csvFile = csvWriter.writeCcdOrderDataToCsv(java.util.List.of(AcroCaseData.builder().build()), false);
            } else {
                csvFile = csvWriter.writeCcdOrderDataToCsv(
                    acroResponse.getCases().stream().map(acroCase -> acroCase.getCaseData()).toList(),
                    true
                );
            }
            // Save the CSV file to outputDirectory
            if (csvFile != null && csvFile.exists()) {
                File outputCsv = new File(outputDirectory, "AcroReport.csv");
                Files.copy(csvFile.toPath(), outputCsv.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } else {
                log.error("CSV file does not exist: {}", csvFile != null ? csvFile.getAbsolutePath() : "null");
            }

            // Only process cases if not null and not empty
            if (acroResponse.getCases() != null && !acroResponse.getCases().isEmpty()) {
                acroResponse.getCases().forEach(acroCase -> {
                    AcroCaseData caseData = acroCase.getCaseData();
                    if (caseData.getFl404Orders() != null) {
                        caseData.getFl404Orders().forEach(order -> {
                            String caseId = String.valueOf(acroCase.getId());
                            String fileName = getFileName(caseId, order.getDateCreated(), false);
                            String welshFileName = getFileName(caseId, order.getDateCreated(), true);
                            java.util.Optional<File> downloadedFileOpt = pdfExtractorService.downloadFl404aDocument(
                                caseId,
                                sysUserToken,
                                fileName,
                                order.getOrderDocument()
                            );
                            if (downloadedFileOpt.isPresent() && downloadedFileOpt.get().exists()) {
                                File downloadedFile = downloadedFileOpt.get();
                                File outputFile = new File(outputDirectory, downloadedFile.getName());
                                try {
                                    Files.copy(downloadedFile.toPath(), outputFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e) {
                                    log.error("Failed to copy file {} to output directory: {}", downloadedFile.getName(), e.getMessage());
                                }
                            }
                            java.util.Optional<File> downloadedWelshFileOpt = pdfExtractorService.downloadFl404aDocument(
                                caseId,
                                sysUserToken,
                                welshFileName,
                                order.getOrderDocumentWelsh()
                            );
                            if (downloadedWelshFileOpt.isPresent() && downloadedWelshFileOpt.get().exists()) {
                                File downloadedWelshFile = downloadedWelshFileOpt.get();
                                File outputWelshFile = new File(outputDirectory, downloadedWelshFile.getName());
                                try {
                                    Files.copy(downloadedWelshFile.toPath(), outputWelshFile.toPath(),
                                               java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                } catch (IOException e) {
                                    log.error("Failed to copy Welsh file {} to output directory: {}", downloadedWelshFile.getName(), e.getMessage());
                                }
                            }
                        });
                    }
                });
            }

            acroZipService.zip();

            log.info(
                "*** Total time taken to run Bais Document upload task - {}s ***",
                TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getFileName(String caseId, LocalDateTime orderCreatedDate, boolean isWelsh) {
        ZonedDateTime zdt = ZonedDateTime.of(orderCreatedDate, ZoneId.systemDefault());
        String filename = sourceDirectory + "/FL404A-" + caseId + "-" + zdt.toEpochSecond();
        return isWelsh ? filename + "-Welsh.pdf" : filename + ".pdf";
    }

}
