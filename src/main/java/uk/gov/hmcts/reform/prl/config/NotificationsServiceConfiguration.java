package uk.gov.hmcts.reform.prl.config;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.service.notify.NotificationClient;

@Configuration
@ConfigurationProperties(prefix = "uk.gov.notify.api")
@Setter
public class NotificationsServiceConfiguration {

    @Value("${uk.gov.notify.api.key}")
    private String key;

    @Value("${uk.gov.notify.api.baseUrl}")
    private String baseUrl;

    @Bean
    public NotificationClient notificationClient() {
        return new NotificationClient(key, baseUrl);
    }

}
