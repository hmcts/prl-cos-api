package uk.gov.hmcts.reform.prl.services.acro;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;



@Service
@Slf4j
@RequiredArgsConstructor
public class PdfExtractorService {

    private final AuthTokenGenerator authTokenGenerator;
    private final CaseDocumentClient caseDocumentClient;

    @Retryable(
        retryFor = {Exception.class},
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Optional<File> downloadFl404aDocument(String caseId, String userToken, String fileName, Document document) {
        if (document == null || document.getDocumentBinaryUrl() == null) {
            return Optional.empty();
        }

        try {
            String serviceToken = authTokenGenerator.generate();
            ResponseEntity<Resource> response = caseDocumentClient.getDocumentBinary(
                userToken,
                serviceToken,
                document.getDocumentBinaryUrl()
            );

            if (response.getBody() != null) {
                File outputFile = new File(fileName);

                Files.copy(response.getBody().getInputStream(),
                           outputFile.toPath(),
                           StandardCopyOption.REPLACE_EXISTING);

                log.info("Downloaded FL404a PDF for case {}: {}", caseId, fileName);
                return Optional.of(outputFile);
            } else {
                log.warn("No response body received for FL404a document download for case {}", caseId);
                throw new RuntimeException("Empty response body for document download");
            }
        } catch (Exception e) {
            log.error("Failed to download FL404a document for case {}: {}", caseId, e.getMessage());
            throw new RuntimeException("Failed to download FL404a document for case " + caseId, e);
        }
    }

    @Retryable(
        retryFor = {Exception.class},
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Optional<File> downloadPdf(String caseId, LocalDateTime orderCreatedDate,
                                      uk.gov.hmcts.reform.prl.models.documents.Document document,
                                      boolean isWelsh, String sysUserToken, String outputDirectory) {
        String documentType = isWelsh ? "Welsh" : "English";

        if (document == null || document.getDocumentBinaryUrl() == null) {
            log.debug("Skipping download for case {} - {} document is null or has no URL", caseId, documentType);
            return Optional.empty();
        }

        log.debug("Attempting to download {} FL404a PDF for case {}", documentType, caseId);

        String fileName = generateFileName(caseId, orderCreatedDate, isWelsh, outputDirectory);

        Optional<File> downloadedFileOpt = downloadFl404aDocument(caseId, sysUserToken, fileName, document);

        if (downloadedFileOpt.isPresent()) {
            log.debug("Successfully downloaded {} PDF for case {} to output directory", documentType, caseId);
            return downloadedFileOpt;
        } else {
            log.warn("Failed to download {} PDF for case {} - will retry if attempts remaining", documentType, caseId);
            throw new RuntimeException("Failed to download FL404a document for case " + caseId);
        }
    }

    private String generateFileName(String caseId, LocalDateTime orderCreatedDate, boolean isWelsh, String outputDirectory) {
        ZonedDateTime zdt = ZonedDateTime.of(orderCreatedDate, ZoneId.systemDefault());
        String filename = outputDirectory + "/FL404A-" + caseId + "-" + zdt.toEpochSecond();
        return isWelsh ? filename + "-Welsh.pdf" : filename + ".pdf";
    }
}
