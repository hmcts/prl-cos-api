package uk.gov.hmcts.reform.prl.clients.hmc;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Minimal Spring Boot application used by the HMC hearing consumer Pact test.
 * Enables the Feign clients and supplies a mocked {@link ServiceAuthorisationApi} so that
 * the {@code TempHmcHearingFeignConfig} S2S request interceptor on {@code HmcHearingApiClient}
 * can be created without reaching a real service-auth provider.
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {
    "uk.gov.hmcts.reform.prl.clients"
})
public class HmcHearingApiClientConsumerApplication {

    @Bean
    public ServiceAuthorisationApi serviceAuthorisationApi() {
        ServiceAuthorisationApi serviceAuthorisationApi = mock(ServiceAuthorisationApi.class);
        when(serviceAuthorisationApi.serviceToken(anyMap()))
            .thenReturn("someServiceAuthToken");
        return serviceAuthorisationApi;
    }
}

