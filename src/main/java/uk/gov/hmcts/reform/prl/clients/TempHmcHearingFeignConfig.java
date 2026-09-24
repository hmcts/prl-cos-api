package uk.gov.hmcts.reform.prl.clients;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;

@Slf4j
public class TempHmcHearingFeignConfig {

    @Bean
    public RequestInterceptor hmcS2sInterceptor(
        @Value("${fis.s2s.microservice:fis_hmc_api}") String microservice,
        @Value("${fis.s2s.secret:${FIS_IDAM_S2S_AUTH_TOTP_SECRET:}}") String secret,
        ServiceAuthorisationApi serviceAuthorisationApi) {

        log.info("HMC S2S interceptor init - microservice={}, secret present={}",
            microservice, StringUtils.isNotBlank(secret));

        AuthTokenGenerator fisTokenGenerator =
            new ServiceAuthTokenGenerator(secret, microservice, serviceAuthorisationApi);

        return template -> {
            String token = fisTokenGenerator.generate();
            log.info("HMC S2S interceptor firing - overriding ServiceAuthorization, token present={}",
                StringUtils.isNotBlank(token));
            template.removeHeader("ServiceAuthorization");
            template.header("ServiceAuthorization", token);
        };
    }
}
