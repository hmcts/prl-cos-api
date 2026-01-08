package uk.gov.hmcts.reform.prl.services.managedocuments;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MiamDocumentRetryService {

    private final CaseDocumentClient caseDocumentClient;

    @Retryable(
        retryFor = {
            feign.FeignException.Conflict.class},
        maxAttempts = 4,
        backoff = @Backoff(
            delay = 300,
            maxDelay = 2000,
            multiplier = 2)
    )

    public ResponseEntity<Resource> getMiamDocumentWithRetry(String authorisation,
                                                             String serviceAuth,
                                                             UUID documentId) {

        RetryContext retryContext = RetrySynchronizationManager.getContext();
        int attempt = (retryContext != null ? retryContext.getRetryCount() : 0) + 1;

        log.info("Getting MIAM document id: {} with retry attempt {}", documentId, attempt);

        return caseDocumentClient.getDocumentBinary(authorisation, serviceAuth, documentId);
    }

    @Recover
    public ResponseEntity<Resource> recover(feign.FeignException.Conflict ex,
                                            String authorisation,
                                            String serviceAuth,
                                            UUID documentId) {


        log.warn("Exhausted {} retries for MIAM document id: {} "
                     + "due to 409 Conflict. Last error: {}", 4, documentId, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @Recover
    public ResponseEntity<Resource> recover(
        FeignException ex,
        String authorisation,
        String serviceAuth,
        UUID documentId
    ) {

        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        log.error("Upstream error for MIAM document id {}: {} (HTTP {})",
                  documentId, ex.getMessage(), ex.status(), ex);

        return ResponseEntity.status(status).build();
    }

    @Recover
    public ResponseEntity<Resource> recover(
        Throwable ex,
        String authorisation,
        String serviceAuth,
        UUID documentId) {

        log.error("Unexpected error for MIAM document id {}: {}",
                  documentId, ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
