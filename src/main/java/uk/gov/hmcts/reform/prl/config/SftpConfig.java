package uk.gov.hmcts.reform.prl.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Builder
@Getter
@Setter
@Configuration
@ConfigurationProperties("acro.sftp")
public class SftpConfig {

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
