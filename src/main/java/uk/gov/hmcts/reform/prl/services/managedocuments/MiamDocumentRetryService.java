package uk.gov.hmcts.reform.prl.services.managedocuments;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.dao.OptimisticLockingFailureException;
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
            ConcurrencyException.class,
            ForbiddenException.class
        },
        maxAttempts = 4,
        backoff = @Backoff(
            delay = 300,
            maxDelay = 2000,
            multiplier = 2)
    )
    public ResponseEntity<Resource> getMiamDocumentWithRetry(String authorisation, String serviceAuth, UUID documentId) {
        try {
            return caseDocumentClient.getDocumentBinary(authorisation, serviceAuth, documentId);
        } catch (FeignException.Forbidden fex) {
            throw new ForbiddenException(
                "Access denied when trying to fetch MIAM document with id: "
                    + documentId, fex
            );
        } catch (FeignException.Conflict fex) {
            throw new ConcurrencyException(
                "Concurrency issue when trying to fetch MIAM document with id:  "
                    + documentId, fex
            );
        }
    }

    @Recover
    public ResponseEntity<Resource> recover(Exception ex, String authorisation, String serviceAuth, UUID documentId) {
        log.error("Failed to fetch MIAM document with id: {} after 4 attempts. Auth: {}, ServiceAuth: {}",
                  documentId,
                  authorisation != null ? "PRESENT" : "MISSING",
                  serviceAuth != null ? "PRESENT" : "MISSING",
                  ex);
        throw new RuntimeException(
            "Failed to fetch MIAM document with id: " + documentId + " after multiple attempts.",
            ex
        );
    }
}
