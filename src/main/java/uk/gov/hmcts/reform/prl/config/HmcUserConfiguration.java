package uk.gov.hmcts.reform.prl.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@Slf4j
public class HmcUserConfiguration {

    private final String hmcCftUserName;
    private final String hmcCftUserPassword;

    public HmcUserConfiguration(
        @Value("${idam.hmcCftUserAuth.username}") String hmcCftUserName,
        @Value("${idam.hmcCftUserAuth.password}") String hmcCftUserPassword
    ) {
        this.hmcCftUserName = hmcCftUserName;
        this.hmcCftUserPassword = hmcCftUserPassword;
    }

    @PostConstruct
    public void logHmcCredentialConfig() {
        log.info("HMC credential config loaded - username present: {}, password present: {}",
                 StringUtils.isNotBlank(hmcCftUserName),
                 StringUtils.isNotBlank(hmcCftUserPassword));

        log.info("HMC username preview: {}", mask(hmcCftUserName));

        if (StringUtils.isBlank(hmcCftUserName) || StringUtils.isBlank(hmcCftUserPassword)) {
            throw new IllegalStateException("Missing `hmc-cft-hearing.username` or `hmc-cft-hearing.password`");
        }
    }

    private String mask(String value) {
        if (StringUtils.isBlank(value)) {
            return "<empty>";
        }
        if (value.length() <= 2) {
            return "**";
        }
        return value.substring(0, 2) + "***";
    }

}
