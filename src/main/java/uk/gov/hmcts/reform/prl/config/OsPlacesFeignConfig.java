package uk.gov.hmcts.reform.prl.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OsPlacesFeignConfig {

    @Value("${postcodelookup.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor osPlacesRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                template.query("key", apiKey);
                template.query("maxresults", "1");
            }
        };
    }
}
