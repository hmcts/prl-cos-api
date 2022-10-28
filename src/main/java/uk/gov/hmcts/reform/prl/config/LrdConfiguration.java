package uk.gov.hmcts.reform.prl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class LrdConfiguration {

    private final String url;
    private final String endpoint;

    public LrdConfiguration(
        @Value("${locationFinder.api.url}") String url,
        @Value("${locationFinder.api.endPoint}") String endpoint) {
        this.url = url;
        this.endpoint = endpoint;
    }
}
