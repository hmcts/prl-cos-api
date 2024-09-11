package uk.gov.hmcts.reform.prl.config.citizen;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.reform.prl.models.citizen.NotificationNames;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "citizen.dashboard")
@Validated
@Getter
public class DashboardNotificationsConfig {

    private final Map<String, Map<NotificationNames, String>> notifications = new HashMap<>();
}
