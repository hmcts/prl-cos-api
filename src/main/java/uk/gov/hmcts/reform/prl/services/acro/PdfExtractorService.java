package uk.gov.hmcts.reform.prl.services.acro;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import java.io.File;
import java.util.Optional;



@Service
@Slf4j
@RequiredArgsConstructor
public class PdfExtractorService {

    private final AuthTokenGenerator authTokenGenerator;
    private final CaseDocumentClient caseDocumentClient;


// TODO - Get rid of this method and use jsut the one method
//    @Retryable(
//        retryFor = {Exception.class},
//        backoff = @Backoff(delay = 1000, multiplier = 2)
//    )
    public File downloadFl404aDocument(String caseId, String userToken, String fileName, Document document) {
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
                           java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                log.info("Downloaded FL404a PDF for case {}: {}", caseId, fileName);
                return outputFile;
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
    public Optional<File> downloadPdf(String caseId,
                                     Document document, String sysUserToken) {
        if (document == null || document.getDocumentBinaryUrl() == null) {
            log.debug("Skipping download for case {} - {} document is null or has no URL", caseId, documentType);
            return Optional.empty();
        }

        log.debug("Attempting to download {} FL404a PDF for case {}", documentType, caseId);


        Optional<File> downloadedFileOpt = downloadFl404aDocument(caseId, sysUserToken, fileName, document);

        if (downloadedFileOpt.isPresent()) {
            log.debug("Successfully downloaded {} PDF for case {} to output directory", documentType, caseId);
            return downloadedFileOpt;
        } else {
            log.warn("Failed to download {} PDF for case {} - will retry if attempts remaining", documentType, caseId);
            throw new RuntimeException("Failed to download FL404a document for case " + caseId);
        }
    }

}
