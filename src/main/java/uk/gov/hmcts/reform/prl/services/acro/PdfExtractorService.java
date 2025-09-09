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
    public File downloadPdf(String fileName, String caseId, Document document, String sysUserToken) {
        if (document == null || document.getDocumentBinaryUrl() == null) {
            log.info("Skipping download for case {} - document is null or has no URL", caseId);
            return null;
        }

        log.info("Attempting to download {} FL404a PDF for case {}", fileName, caseId);

        String serviceToken = authTokenGenerator.generate();
        try {
            ResponseEntity<Resource> response = caseDocumentClient.getDocumentBinary(
                    sysUserToken,
                    serviceToken,
                    document.getDocumentBinaryUrl()
            );

            if (response.getBody() != null) {
                File outputFile = new File(fileName);

                Files.copy(
                        response.getBody().getInputStream(),
                        outputFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );

                log.info("Successfully downloaded {} PDF for case {} to output directory", fileName, caseId);
                return outputFile;
            } else {
                log.warn("No response body received for FL404a document download for case {}", caseId);
                throw new RuntimeException("Empty response body for document download");
            }
        } catch (Exception e) {
            log.error("Failed to download {} PDF for case {} - will retry if attempts remaining", fileName, caseId);
            throw new RuntimeException("Failed to download FL404a document for case " + caseId);
        }
    }
}
