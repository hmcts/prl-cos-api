package uk.gov.hmcts.reform.prl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class SystemUserConfiguration {

    private final String userName;
    private final String password;

    public SystemUserConfiguration(@Value("${PRL_SYSTEM_UPDATE_USERNAME}") String userName,
                                         @Value("${prl.system-update.password}") String password) {
        this.userName = userName;
        this.password = password;
    }
}
