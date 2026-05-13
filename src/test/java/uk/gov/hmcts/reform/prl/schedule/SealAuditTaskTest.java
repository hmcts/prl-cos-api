package uk.gov.hmcts.reform.prl.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.services.sealaudit.SealAuditService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SealAuditTaskTest {

    @Mock
    private SealAuditService sealAuditService;

    @InjectMocks
    private SealAuditTask sealAuditTask;

    @Test
    void shouldCallSealAuditService() {
        sealAuditTask.run();
        verify(sealAuditService).runAudit();
    }

    @Test
    void shouldPropagateException() {
        doThrow(new RuntimeException("Test error")).when(sealAuditService).runAudit();
        assertThrows(RuntimeException.class, () -> sealAuditTask.run());
    }
}
