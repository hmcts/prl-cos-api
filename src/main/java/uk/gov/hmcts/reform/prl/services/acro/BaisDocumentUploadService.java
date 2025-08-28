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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;
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
            //Fetch all cases with FL404A Orders
            AcroResponse acroResponse = acroCaseDataService.getCaseData(sysUserToken);

            FileAttribute<?> permissions = PosixFilePermissions.asFileAttribute(
                PosixFilePermissions.fromString("rwx------")
            );

            Path tempSourcePath = Files.createTempDirectory("acro-sources", permissions);
            Path tempOutputPath = Files.createTempDirectory("acro-output", permissions);

            // Override the configured directories with writable temp directories
            this.sourceDirectory = tempSourcePath.toString();
            this.outputDirectory = tempOutputPath.toString();

            if (acroResponse.getTotal() == 0 || acroResponse.getCases() == null || acroResponse.getCases().isEmpty()) {
                log.info("Search has resulted empty cases with Final FL404a orders, so need to send empty csv file");

                csvWriter.writeCcdOrderDataToCsv(AcroCaseData.builder().build(), false);
                acroZipService.zip();
                return;
            }

            acroResponse.getCases().forEach(acroCase -> {
                AcroCaseData caseData = acroCase.getCaseData();
                caseData.getFl404Orders().forEach(order -> {
                    String caseId = String.valueOf(acroCase.getId());
                    String fileName = getFileName(caseId, order.getDateCreated(), false);
                    String welshFileName = getFileName(caseId, order.getDateCreated(), true);
                    pdfExtractorService.downloadFl404aDocument(
                        caseId,
                        sysUserToken,
                        fileName,
                        order.getOrderDocument()
                    );
                    pdfExtractorService.downloadFl404aDocument(
                        caseId,
                        sysUserToken,
                        welshFileName,
                        order.getOrderDocumentWelsh()
                    );
                    try {
                        csvWriter.writeCcdOrderDataToCsv(caseData, true);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            });

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
