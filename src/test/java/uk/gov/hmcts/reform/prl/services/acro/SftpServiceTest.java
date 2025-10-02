package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.io.File;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SftpServiceTest {

    @Mock
    private MessageChannel toSftpChannel;
    @InjectMocks
    private SftpService sftpService;

    @Test
    public void testUploadFile_success() {
        // Arrange
        File file = new File("test.txt");
        when(toSftpChannel.send(any(Message.class))).thenReturn(true);
        // Act
        sftpService.uploadFile(file);
        // Assert
        verify(toSftpChannel, times(1)).send(any(Message.class));
    }

    @Test
    public void testUploadFile_failure() {
        // Arrange
        File file = new File("test.txt");
        when(toSftpChannel.send(any(Message.class))).thenThrow(new RuntimeException("SFTP error"));
        // Act
        sftpService.uploadFile(file);
        // Assert
        verify(toSftpChannel, times(1)).send(any(Message.class));
    }
}
