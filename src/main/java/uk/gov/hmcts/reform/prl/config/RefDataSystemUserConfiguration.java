package uk.gov.hmcts.reform.prl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class RefDataSystemUserConfiguration {
    private final String userName;
    private final String password;

    public RefDataSystemUserConfiguration(@Value("${prl.refdata.username}") String userName,
                                   @Value("${prl.refdata.password}") String password) {
        this.userName = userName;
        this.password = password;
    }
}
