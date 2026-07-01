package uk.gov.hmcts.reform.prl.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "acro.sftp")
@Validated
@Getter
@Setter
public class SftpProperties {

    private String host;

    private int port;

    private String user;

    private String remoteDirectory;

    private String privateKey;

    private String privateKeyPassPhrase;

    private boolean allowUnknownKeys;

    private int poolSize;

    private int sessionWaitTimeout;

    private boolean azureDeployment;

    private String password;
}
