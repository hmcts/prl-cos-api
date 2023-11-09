package uk.gov.hmcts.reform.prl.config;


import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "send-grid.notification.email")
@Validated
@Getter
public class SendgridEmailTemplatesConfig {

    private final Map<LanguagePreference, Map<SendgridEmailTemplateNames, String>> templates = new HashMap<>();

}
