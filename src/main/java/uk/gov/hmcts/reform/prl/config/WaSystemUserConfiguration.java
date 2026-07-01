package uk.gov.hmcts.reform.prl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class WaSystemUserConfiguration {

    private final String userName;
    private final String password;

    public WaSystemUserConfiguration(@Value("${wa-task-management.system.username}") String userName,
                                     @Value("${wa-task-management.system.password}") String password) {
        this.userName = userName;
        this.password = password;
    }
}
