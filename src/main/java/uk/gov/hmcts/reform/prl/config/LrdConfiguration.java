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
        @Value("${genApp.lrd.url}") String url,
        @Value("${genApp.lrd.endpoint}") String endpoint) {
        this.url = url;
        this.endpoint = endpoint;
    }
}
