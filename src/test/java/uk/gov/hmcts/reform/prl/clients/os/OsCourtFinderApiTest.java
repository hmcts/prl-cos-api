package uk.gov.hmcts.reform.prl.clients.os;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class OsCourtFinderApiTest {

    @Test
    void shouldAddApiKeyAndMaxResultsToRequest() {

        // Arrange: create config and inject API key manually
        OsCourtFinderApi.FeignClientConfiguration config =
            new OsCourtFinderApi.FeignClientConfiguration();

        ReflectionTestUtils.setField(config, "apiKey", "TEST-API-KEY");

        RequestInterceptor interceptor = config.osCourtFinderRequestInterceptor();

        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        Assertions.assertThat(template.queries())
            .containsEntry("key", java.util.List.of("TEST-API-KEY"))
            .containsEntry("maxresults", java.util.List.of("1"));
    }
}
