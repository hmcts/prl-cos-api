package uk.gov.hmcts.reform.prl.services.acro;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class SftpService {

    @Autowired
    private MessageChannel toSftpChannel;

    public void uploadFile(File file) {
        log.info("Uploading file to SFTP");
        Message<File> message = MessageBuilder.withPayload(file).build();
        try {
            toSftpChannel.send(message);
        } catch (Exception e) {
            log.error("Error while uploading file to SFTP", e);
            throw new RuntimeException(e);
        }
        log.info("File {} uploaded to SFTP", file.getName());
    }
}
