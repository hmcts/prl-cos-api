package uk.gov.hmcts.reform.prl.schedule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.services.acro.BaisDocumentUploadService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
class BaisDocumentUploadTaskTest {
    @InjectMocks
    BaisDocumentUploadTask baisDocumentUploadTask;

    @Mock
    BaisDocumentUploadService baisDocumentUploadService;

    @Test
    void runTaskWithHearingAwayDays() {
        baisDocumentUploadTask.run();

        verify(baisDocumentUploadService, times(1)).uploadFL404Orders();
    }
}
