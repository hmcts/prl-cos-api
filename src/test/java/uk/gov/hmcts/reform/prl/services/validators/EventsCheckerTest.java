package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class EventsCheckerTest {

    @InjectMocks
    EventsChecker eventsChecker;

    @Mock
    CaseNameChecker caseNameChecker;



    @BeforeEach
    void init() {
        eventsChecker.init();
    }

    @Test
    void testIsFinished() {
        Event event = Event.CASE_NAME;
        CaseData caseData = CaseData.builder().build();
        when(caseNameChecker.isFinished(caseData)).thenReturn(Boolean.TRUE);
        eventsChecker.init();
        assertTrue(eventsChecker.isFinished(event, caseData));

    }

    @Test
    void testIsStarted() {
        Event event = Event.CASE_NAME;
        CaseData caseData = CaseData.builder().build();
        when(caseNameChecker.isStarted(caseData)).thenReturn(Boolean.TRUE);
        eventsChecker.init();
        assertTrue(eventsChecker.isStarted(event, caseData));

    }

    @Test
    void testHasMandatoryCompleted() {
        Event event = Event.CASE_NAME;
        CaseData caseData = CaseData.builder().build();
        when(caseNameChecker.hasMandatoryCompleted(caseData)).thenReturn(Boolean.TRUE);
        eventsChecker.init();
        assertTrue(eventsChecker.hasMandatoryCompleted(event, caseData));

    }

    @Test
    void testGetDefaultState() {
        Event event = Event.CASE_NAME;
        CaseData caseData = CaseData.builder().build();
        when(caseNameChecker.getDefaultTaskState(caseData)).thenReturn(TaskState.NOT_STARTED);
        eventsChecker.init();
        assertEquals(TaskState.NOT_STARTED,eventsChecker.getDefaultState(event,caseData));

    }

}
