package uk.gov.hmcts.reform.prl.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(HttpProperties.class)
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, HttpProperties properties) {
        return builder
            .connectTimeout(properties.getConnectTimeout())
            .readTimeout(properties.getReadTimeout())
            .build();
    }
}
