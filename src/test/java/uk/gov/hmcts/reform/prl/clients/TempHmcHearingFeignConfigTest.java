package uk.gov.hmcts.reform.prl.clients;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TempHmcHearingFeignConfigTest {

    private TempHmcHearingFeignConfig config;
    private ServiceAuthorisationApi serviceAuthorisationApi;

    @BeforeEach
    void setUp() {
        config = new TempHmcHearingFeignConfig();
        serviceAuthorisationApi = mock(ServiceAuthorisationApi.class);
    }

    @Test
    void hmcS2sInterceptor_shouldOverrideServiceAuthorizationHeader() {
        String generatedToken = "Bearer test-s2s-token";
        when(serviceAuthorisationApi.serviceToken(anyMap())).thenReturn(generatedToken);
        RequestInterceptor interceptor = config.hmcS2sInterceptor(
            "fis_hmc_api",
            "test-secret",
            serviceAuthorisationApi
        );

        RequestTemplate template = mock(RequestTemplate.class);

        interceptor.apply(template);

        verify(template).removeHeader("ServiceAuthorization");
        verify(template).header("ServiceAuthorization", generatedToken);
    }
}
