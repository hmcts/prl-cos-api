package uk.gov.hmcts.reform.prl.config;

import lombok.Getter;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.expression.common.LiteralExpression;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
@Getter
public class SftpConfig {

    @Value("${acro.sftp.host}")
    private String host;

    @Value("${acro.sftp.port}")
    private int port;

    @Value("${acro.sftp.user}")
    private String user;

    @Value("${acro.sftp.remote-directory}")
    private String remoteDirectory;

    @Value("${acro.sftp.private-key}")
    private String privateKey;

    @Value("${acro.sftp.private-key-pass-phrase}")
    private String privateKeyPassPhrase;

    @Value("${acro.sftp.allow-unknown-keys}")
    private boolean allowUnknownKeys;

    @Value("${acro.sftp.pool-size}")
    private int poolSize;

    @Value("${acro.sftp.session-wait-timeout}")
    private int sessionWaitTimeout;

    @Value("${acro.sftp.azure-deployment}")
    private boolean azureDeployment;

    @Value("${acro.sftp.password}")
    private String password;

    @Bean
    public SessionFactory<SftpClient.DirEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(host);
        factory.setPort(port);
        factory.setUser(user);


        if (azureDeployment) {
            Resource privateKeyResource = new FileSystemResource(privateKey);
            factory.setPrivateKey(privateKeyResource);
            factory.setPrivateKeyPassphrase(privateKeyPassPhrase);
        } else {
            factory.setPassword(password);
        }

        factory.setAllowUnknownKeys(allowUnknownKeys);
        CachingSessionFactory<SftpClient.DirEntry> cachingSessionFactory = new CachingSessionFactory<>(factory);
        cachingSessionFactory.setPoolSize(poolSize);
        cachingSessionFactory.setSessionWaitTimeout(sessionWaitTimeout);
        return cachingSessionFactory;
    }

    @Bean
    @ServiceActivator(inputChannel = "toSftpChannel")
    public MessageHandler toSftpChannelPrintDestinationHandler() {
        SftpMessageHandler handler = new SftpMessageHandler(sftpSessionFactory());
        handler.setRemoteDirectoryExpression(new LiteralExpression(""));
        handler.setUseTemporaryFileName(false);
        handler.setLoggingEnabled(true);
        return handler;
    }

    @Bean(name = "toSftpChannel")
    public MessageChannel toSftpChannel() {
        return new DirectChannel();
    }
}
