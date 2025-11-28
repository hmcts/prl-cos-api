package uk.gov.hmcts.reform.prl.config;

import lombok.Getter;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final SftpProperties sftpProperties;

    @Autowired
    public SftpConfig(SftpProperties sftpProperties) {
        this.sftpProperties = sftpProperties;
    }

    @Bean
    public SessionFactory<SftpClient.DirEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(sftpProperties.getHost());
        factory.setPort(sftpProperties.getPort());
        factory.setUser(sftpProperties.getUser());

        if (sftpProperties.isAzureDeployment()) {
            Resource privateKeyResource = new FileSystemResource(sftpProperties.getPrivateKey());
            factory.setPrivateKey(privateKeyResource);
            factory.setPrivateKeyPassphrase(sftpProperties.getPrivateKeyPassPhrase());
        } else {
            factory.setPassword(sftpProperties.getPassword());
        }

        factory.setAllowUnknownKeys(sftpProperties.isAllowUnknownKeys());
        CachingSessionFactory<SftpClient.DirEntry> cachingSessionFactory = new CachingSessionFactory<>(factory);
        cachingSessionFactory.setPoolSize(sftpProperties.getPoolSize());
        cachingSessionFactory.setSessionWaitTimeout(sftpProperties.getSessionWaitTimeout());
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
