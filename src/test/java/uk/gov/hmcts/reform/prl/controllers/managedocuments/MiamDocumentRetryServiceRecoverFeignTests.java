package uk.gov.hmcts.reform.prl.controllers.managedocuments;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.prl.controllers.testingsupport.TestLogAppender;
import uk.gov.hmcts.reform.prl.services.managedocuments.MiamDocumentRetryService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MiamDocumentRetryServiceRecoverFeignTests {

    private MiamDocumentRetryService service;

    private TestLogAppender logAppender;
    private Logger serviceLogger;

    private final String auth = "Bearer some-auth";
    private final String serviceAuth = "service-auth";
    private final UUID docId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new MiamDocumentRetryService(null);
        serviceLogger = (Logger) LoggerFactory.getLogger(MiamDocumentRetryService.class);
        logAppender = new TestLogAppender();
        logAppender.setName("TEST-APPENDER");
        logAppender.start();
        serviceLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDownAppender() {
        if (serviceLogger != null && logAppender != null) {
            serviceLogger.detachAppender(logAppender);
            logAppender.stop();
        }
    }

    @Test
    void recoverFeignShouldReturn401Unauthorized_whenUpstream401() {
        FeignException ex = buildFeignException(401, "Unauthorized");
        ResponseEntity<Resource> res = service.recover(ex, auth, serviceAuth, docId);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertNoBody(res);

        ILoggingEvent last = logAppender.getEvents().getLast();
        assertThat(last.getLevel()).isEqualTo(Level.ERROR);
        assertThat(last.getFormattedMessage())
            .contains("Upstream error for MIAM document id")
            .contains(docId.toString())
            .contains("HTTP 401");
    }

    @Test
    void recoverFeignShouldReturn403Forbidden_whenUpstream403() {
        FeignException ex = buildFeignException(403, "Forbidden");
        ResponseEntity<Resource> res = service.recover(ex, auth, serviceAuth, docId);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertNoBody(res);

        ILoggingEvent last = logAppender.getEvents().getLast();
        assertThat(last.getLevel()).isEqualTo(Level.ERROR);
        assertThat(last.getFormattedMessage())
            .contains("HTTP 403");
    }

    @Test
    void recoverFeignShouldReturn404NotFound_whenUpstream404() {
        FeignException ex = buildFeignException(404, "Not Found");
        ResponseEntity<Resource> res = service.recover(ex, auth, serviceAuth, docId);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertNoBody(res);

        ILoggingEvent last = logAppender.getEvents().getLast();
        assertThat(last.getLevel()).isEqualTo(Level.ERROR);
        assertThat(last.getFormattedMessage())
            .contains("HTTP 404");
    }

    @Test
    void recoverFeignShouldReturn500InternalServerError_whenUpstream500() {
        FeignException ex = buildFeignException(500, "Internal Server Error");
        ResponseEntity<Resource> res = service.recover(ex, auth, serviceAuth, docId);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertNoBody(res);

        ILoggingEvent last = logAppender.getEvents().getLast();
        assertThat(last.getLevel()).isEqualTo(Level.ERROR);
        assertThat(last.getFormattedMessage())
            .contains("HTTP 500");
    }

    @Test
    void recoverFeignShouldReturn502BadGateway_whenUpstream502() {
        FeignException ex = buildFeignException(502, "Bad Gateway");
        ResponseEntity<Resource> res = service.recover(ex, auth, serviceAuth, docId);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertNoBody(res);

        ILoggingEvent last = logAppender.getEvents().getLast();
        assertThat(last.getLevel()).isEqualTo(Level.ERROR);
        assertThat(last.getFormattedMessage())
            .contains("HTTP 502");
    }

    @Test
    void recoverFeignShouldReturn503ServiceUnavailable_whenUpstream503() {
        FeignException ex = buildFeignException(503, "Service Unavailable");
        ResponseEntity<Resource> res = service.recover(ex, auth, serviceAuth, docId);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertNoBody(res);

        ILoggingEvent last = logAppender.getEvents().getLast();
        assertThat(last.getLevel()).isEqualTo(Level.ERROR);
        assertThat(last.getFormattedMessage())
            .contains("HTTP 503");
    }

    @Test
    void recoverFeignShouldFallbackTo500_whenUpstreamNonStandard599() {
        FeignException ex = buildFeignException(599, "Network Connect Timeout");
        ResponseEntity<Resource> res = service.recover(ex, auth, serviceAuth, docId);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertNoBody(res);

        ILoggingEvent last = logAppender.getEvents().getLast();
        assertThat(last.getLevel()).isEqualTo(Level.ERROR);
        assertThat(last.getFormattedMessage())
            .contains("HTTP 599");
    }

    @Test
    void recoverFeignShouldHandleConflict409_mirroringStatus_withoutDedicatedRecover() {
        FeignException ex = buildFeignException(409, "Conflict");
        ResponseEntity<Resource> res = service.recover(ex, auth, serviceAuth, docId);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertNoBody(res);

        ILoggingEvent last = logAppender.getEvents().getLast();
        assertThat(last.getLevel()).isEqualTo(Level.ERROR);
        assertThat(last.getFormattedMessage())
            .contains("HTTP 409");
    }

    @Test
    void recoverFeign_shouldReturn429TooManyRequests_whenUpstream429() {
        FeignException ex = buildFeignException(429, "Too Many Requests");
        ResponseEntity<Resource> res = service.recover(ex, auth, serviceAuth, docId);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertNoBody(res);

        ILoggingEvent last = logAppender.getEvents().getLast();
        assertThat(last.getLevel()).isEqualTo(Level.ERROR);
        assertThat(last.getFormattedMessage())
            .contains("HTTP 429");
    }

    // -----------------------
    // Helpers
    // -----------------------

    /**
     * Builds a FeignException with a given HTTP status and reason using a minimal Response.
     * This avoids needing a live Feign client call.
     */
    private FeignException buildFeignException(int status, String reason) {
        Map<String, Collection<String>> headers = new HashMap<>();
        headers.put("Content-Type", List.of("application/json"));

        // Minimal mock request
        Request request = Request.create(
            Request.HttpMethod.GET,
            "https://doc-store/documents/" + docId,
            headers,
            Request.Body.empty(), // body (GET)
            new RequestTemplate()
                .method(Request.HttpMethod.GET)
                .uri("/ccd")
        );

        // Minimal mock response
        Response response = Response.builder()
            .status(status)
            .reason(reason)
            .headers(headers)
            .request(request)
            .body((byte[]) null)
            .build();

        // Create a FeignException tied to that response
        return FeignException.errorStatus("CaseDocumentClient#getDocumentBinary", response);
    }

    private static void assertNoBody(ResponseEntity<Resource> res) {
        assertThat(res.getBody()).isNull();
    }
}
