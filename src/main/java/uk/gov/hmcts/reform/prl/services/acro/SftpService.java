package uk.gov.hmcts.reform.prl.services.acro;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class SftpService {

    @Autowired
    private MessageChannel toSftpChannel;

    @Retryable(
        retryFor = {MessageHandlingException.class},
        maxAttempts = 4,
        backoff = @Backoff(delay = 1000, multiplier = 3)
    )
    public void uploadFile(File file) {
        log.info("Uploading file to SFTP");
        Message<File> message = MessageBuilder.withPayload(file).build();
        toSftpChannel.send(message);
        log.info("File {} uploaded to SFTP", file.getName());
    }


    @Recover
    public void uploadFileRecovery(RuntimeException e, Message<File> message) {
        log.error("Error while uploading file  {} to SFTP", message.getHeaders(), e);
    }
}
