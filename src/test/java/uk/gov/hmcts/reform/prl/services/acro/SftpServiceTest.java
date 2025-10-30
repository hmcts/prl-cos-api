package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandlingException;

import java.io.File;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {SftpService.class})
public class SftpServiceTest {

    @MockBean
    private MessageChannel toSftpChannel;
    @Autowired
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
    public void testRetryOnGetCaseDataThrowingException() {
        File file = new File("test.txt");
        when(toSftpChannel.send(any(Message.class)))
            .thenThrow(new MessageHandlingException(mock(Message.class), "Simulated failure"))
            .thenThrow(new MessageHandlingException(mock(Message.class), "Simulated failure"))
            .thenThrow(new MessageHandlingException(mock(Message.class), "Simulated failure"))
            .thenThrow(new MessageHandlingException(mock(Message.class), "Simulated failure"))
            .thenThrow(new MessageHandlingException(mock(Message.class), "Simulated failure"));


        assertThrows(RuntimeException.class, () -> sftpService.uploadFile(file));

        verify(toSftpChannel, atMost(5)).send(any(Message.class));
    }
}
