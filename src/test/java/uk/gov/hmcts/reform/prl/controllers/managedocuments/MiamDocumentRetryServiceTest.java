package uk.gov.hmcts.reform.prl.controllers.managedocuments;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.Sleeper;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.controllers.testingsupport.TestLogAppender;
import uk.gov.hmcts.reform.prl.services.managedocuments.MiamDocumentRetryService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MiamDocumentRetryServiceTest.TestConfig.class)
class MiamDocumentRetryServiceTest {

    private TestLogAppender logAppender;
    private Logger serviceLogger;

    @TestConfiguration
    @EnableRetry(proxyTargetClass = true) // Enable Spring Retry with CGLIB proxies
    static class TestConfig {
        @Bean
        MiamDocumentRetryService miamDocumentRetryService(CaseDocumentClient client) {
            return new MiamDocumentRetryService(client);
        }

        @Bean
        CapturingSleeper sleeper() {
            return new CapturingSleeper();
        }
    }

    static class CapturingSleeper implements Sleeper {
        private final List<Long> delays = new CopyOnWriteArrayList<>();

        @Override
        public void sleep(long millis) {
            delays.add(millis);
        }

        public List<Long> getDelays() {
            return delays;
        }

        public void clear() {
            delays.clear();
        }
    }

    @MockBean
    private CaseDocumentClient caseDocumentClient;

    @Autowired
    private MiamDocumentRetryService service;

    @Autowired
    private CapturingSleeper sleeper;

    private static final String AUTH = "auth";
    private static final String S2S = "s2s";
    private static final UUID DOC_ID = UUID.randomUUID();


    @BeforeEach
    void attachLogAppender() {
        serviceLogger = (Logger) LoggerFactory.getLogger(MiamDocumentRetryService.class);
        logAppender = new TestLogAppender();
        logAppender.setName("TEST-APPENDER");
        logAppender.start();
        serviceLogger.addAppender(logAppender);
    }

    @AfterEach
    void detachLogAppender() {
        if (serviceLogger != null && logAppender != null) {
            serviceLogger.detachAppender(logAppender);
            logAppender.stop();
        }
    }

    @Test
    void executesOnceWhenNoConflictOccurs() {
        sleeper.clear();
        when(caseDocumentClient.getDocumentBinary(any(), any(), (UUID) any()))
            .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> result = service.getMiamDocumentWithRetry(AUTH, S2S, DOC_ID);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(caseDocumentClient, times(1)).getDocumentBinary(any(), any(), (UUID) any());
        assertTrue(sleeper.getDelays().isEmpty(), "No backoff should occur when there is no failure");


        var last = logAppender.getEvents().getLast();
        assertThat(last.getLevel()).isEqualTo(Level.INFO);
        assertThat(last.getFormattedMessage())
            .contains("Getting MIAM document id:");
    }

    @Test
    void succeedsAfterRetriesWhenConflictOccursTransiently() {
        sleeper.clear();
        AtomicInteger attempts = new AtomicInteger(0);

        // Mock: throw Conflict twice, then return success
        when(caseDocumentClient.getDocumentBinary(any(), any(), (UUID) any()))
            .thenAnswer(invocation -> {
                int numRetries = attempts.incrementAndGet();
                if (numRetries <= 2) {
                    throw conflict("Transient 409 on attempt " + numRetries);
                }
                return ResponseEntity.ok().build();
            });

        ResponseEntity<?> result = service.getMiamDocumentWithRetry(AUTH, S2S, DOC_ID);

        assertEquals(HttpStatus.OK, result.getStatusCode(), "Should succeed after retries");
        assertEquals(3, attempts.get(), "Should attempt 3 times (2 failures + 1 success)");
        assertEquals(List.of(300L, 600L), sleeper.getDelays(), "Backoff delays should be 300ms, 600ms");


        boolean exhaustedRetriesWarn = logAppender.getEvents().stream()
            .anyMatch(e -> e.getLevel() == Level.WARN
                && e.getFormattedMessage().contains("Exhausted retries"));
        assertThat(exhaustedRetriesWarn).as("Should not log exhausted retries WARN on eventual success").isFalse();
    }

    @Test
    void recoversToConflictResponseWhenConflictPersists() {
        sleeper.clear();
        AtomicInteger attempts = new AtomicInteger(0);

        // Mock: always throw Conflict
        when(caseDocumentClient.getDocumentBinary(any(), any(), (UUID) any()))
            .thenAnswer(invocation -> {
                attempts.incrementAndGet();
                throw conflict("Persistent 409");
            });

        ResponseEntity<?> result = service.getMiamDocumentWithRetry(AUTH, S2S, DOC_ID);

        assertEquals(HttpStatus.CONFLICT, result.getStatusCode(), "Recover should return 409 response");
        assertEquals(4, attempts.get(), "Should exhaust all retry attempts (maxAttempts=4)");
        assertEquals(List.of(300L, 600L, 1200L), sleeper.getDelays(), "Backoff sequence should be 300, 600, 1200");

        var warn = logAppender.getEvents().stream()
            .filter(e -> e.getLevel() == Level.WARN)
            .reduce((first, second) -> second)
            .orElseThrow();

        assertThat(warn.getMessage())
            .isEqualTo("Exhausted {} retries for MIAM document id: {} due to 409 Conflict. Last error: {}");

        assertThat(warn.getArgumentArray())
            .hasSize(3)
            .satisfies(args -> {
                assertThat(args[0]).as("maxAttempts").isEqualTo(4);
                assertThat(args[1]).as("documentId").isEqualTo(DOC_ID);
                assertThat(String.valueOf(args[2])).as("last error message").isNotBlank();
            });

        assertThat(warn.getFormattedMessage())
            .contains(DOC_ID.toString())
            .contains("409 Conflict");
    }

    @Test
    void genericRecoverReturns500OnNonRetryableException() {
        sleeper.clear();

        // Throw a non-retryable exception -> generic @Recover(Throwable...) should handle it with 500
        when(caseDocumentClient.getDocumentBinary(any(), any(), any(UUID.class)))
            .thenThrow(new RuntimeException("Non-retryable"));

        ResponseEntity<?> result = service.getMiamDocumentWithRetry(AUTH, S2S, DOC_ID);

        assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode(),
            "Generic recover should map unexpected exceptions to 500"
        );
        verify(caseDocumentClient, times(1)).getDocumentBinary(AUTH, S2S, DOC_ID);
        assertTrue(sleeper.getDelays().isEmpty(), "No backoff for non-retryable exception");
    }

    @Test
    void stopsRetryingAfterConflictThenNonRetryableException() {
        sleeper.clear();
        AtomicInteger attempts = new AtomicInteger(0);

        // First attempt: Conflict (retryable) -> schedules backoff
        // Second attempt: Non-retryable -> generic recover 500, stop
        when(caseDocumentClient.getDocumentBinary(any(), any(), any(UUID.class)))
            .thenAnswer(inv -> {
                int numRetries = attempts.incrementAndGet();
                if (numRetries == 1) {
                    throw conflict("First attempt 409");
                }
                throw new IllegalStateException("non-retryable");
            });

        ResponseEntity<?> result = service.getMiamDocumentWithRetry(AUTH, S2S, DOC_ID);

        assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode(),
            "Generic recover should map unexpected exceptions to 500"
        );
        assertEquals(2, attempts.get(), "Should stop after second attempt (non-retryable)");
        assertEquals(List.of(300L), sleeper.getDelays(), "Only one backoff (after first failure)");
    }

    @Test
    void contractTestThatForwardsArgumentsToClient() {
        sleeper.clear();

        when(caseDocumentClient.getDocumentBinary(any(), any(), any(UUID.class)))
            .thenReturn(ResponseEntity.ok().build());

        ResponseEntity<?> result = service.getMiamDocumentWithRetry(AUTH, S2S, DOC_ID);
        assertEquals(HttpStatus.OK, result.getStatusCode());

        ArgumentCaptor<String> authCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> s2sCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);

        verify(caseDocumentClient).getDocumentBinary(authCaptor.capture(), s2sCaptor.capture(), idCaptor.capture());
        assertEquals(AUTH, authCaptor.getValue());
        assertEquals(S2S, s2sCaptor.getValue());
        assertEquals(DOC_ID, idCaptor.getValue());
    }

    private static Throwable conflict(String message) {

        Request request = Request.create(
            Request.HttpMethod.POST,
            "https://example.test/ccd",
            Collections.emptyMap(),
            Request.Body.empty(),
            new RequestTemplate()
                .method(Request.HttpMethod.POST)
                .uri("/ccd")
        );

        Response response = Response.builder()
            .request(request)
            .status(409)
            .reason("Conflict")
            .headers(Collections.emptyMap())
            .build();

        FeignException feignException = FeignException.errorStatus("CCD#submitEvent", response);
        return (feignException instanceof FeignException.Conflict)
            ? (FeignException.Conflict) feignException
            : new FeignException.Conflict(message, request, null, Collections.emptyMap());
    }
}
