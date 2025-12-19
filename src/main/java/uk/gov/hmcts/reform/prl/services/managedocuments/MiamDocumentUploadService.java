package uk.gov.hmcts.reform.prl.services.managedocuments;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MiamDocumentUploadService {

    private final CaseDocumentClient caseDocumentClient;

    @Retryable(
        retryFor = {
            OptimisticLockingFailureException.class,
            ConcurrencyException.class,
            ForbiddenException.class
        },
        maxAttempts = 4,
        backoff = @Backoff(delay = 300, multiplier = 2)
    )
    public ResponseEntity<Resource> getMiamDocumentWithRetry(String authorisation, String serviceAuth, UUID documentId) {
        try {
            return caseDocumentClient.getDocumentBinary(authorisation, serviceAuth, documentId);
        } catch (FeignException.Forbidden fEx) {
            throw new ForbiddenException(
                "Access denied when trying to fetch MIAM document with id: {} ",
                documentId,
                fEx
            );
        } catch (FeignException.Conflict cEx) {
            throw new ConcurrencyException(
                "Concurrency issue when trying to fetch MIAM document with id: {} ",
                documentId,
                cEx
            );
        }
    }
}
