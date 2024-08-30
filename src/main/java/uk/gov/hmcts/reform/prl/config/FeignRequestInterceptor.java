package uk.gov.hmcts.reform.prl.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;

import java.util.UUID;
import java.util.function.Supplier;

public class FeignRequestInterceptor implements RequestInterceptor {

    private Supplier<String> nextRequestId;

    public static final String REQUEST_ID = "Request-Id";
    public static final String ROOT_REQUEST_ID = "Root-Request-Id";
    public static final String ORIGIN_REQUEST_ID = "Origin-Request-Id";

    @Override
    public void apply(RequestTemplate template) {
        template.header(REQUEST_ID, nextRequestId.get());
        template.header(ROOT_REQUEST_ID, MDC.get("rootRequestId"));
        template.header(ORIGIN_REQUEST_ID, MDC.get("requestId"));
    }

    public FeignRequestInterceptor() {
        this(UUID.randomUUID()::toString);
    }

    private FeignRequestInterceptor(Supplier<String> nextRequestId) {
        this.nextRequestId = nextRequestId;
    }
}
