package uk.gov.hmcts.reform.prl.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.util.function.Supplier;

public class FeignRequestInterceptor implements RequestInterceptor {

    private Supplier<String> nextRequestId;

    @Override
    public void apply(RequestTemplate template) {

    }

    public FeignRequestInterceptor() {
        //this(RequestIdGenerator::next);
    }

    private FeignRequestInterceptor(Supplier<String> nextRequestId) {
        this.nextRequestId = nextRequestId;
    }
}
