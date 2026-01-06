package uk.gov.hmcts.reform.prl.services.managedocuments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
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

    public ResponseEntity<Resource> getMiamDocumentWithRetry(String authorisation, String serviceAuth, UUID documentId) {
        log.info("Getting MIAM document id: {} with retry", documentId);
        return caseDocumentClient.getDocumentBinary(authorisation, serviceAuth, documentId);
    }

    @Recover
    public ResponseEntity<Resource> recover(feign.FeignException.Conflict ex,
                                            String authorisation,
                                            String serviceAuth,
                                            UUID documentId) {


        log.warn("Exhausted retries for MIAM document id: {} due to 409 Conflict: {}", documentId, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
