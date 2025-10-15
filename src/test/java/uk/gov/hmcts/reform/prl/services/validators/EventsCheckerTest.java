package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class EventsCheckerTest {

    @InjectMocks
    EventsChecker eventsChecker;

    @Mock
    ApplicationTypeChecker applicationTypeChecker;



    @Before
    public void init() {
        eventsChecker.init();
    }

    @Test
    public void testIsFinished() {
        Event event = Event.TYPE_OF_APPLICATION;
        CaseData caseData = CaseData.builder().build();
        when(applicationTypeChecker.isFinished(caseData)).thenReturn(Boolean.TRUE);
        eventsChecker.init();
        assertTrue(eventsChecker.isFinished(event, caseData));

    }

    @Test
    public void testIsStarted() {
        Event event = Event.TYPE_OF_APPLICATION;
        CaseData caseData = CaseData.builder().build();
        when(applicationTypeChecker.isStarted(caseData)).thenReturn(Boolean.TRUE);
        eventsChecker.init();
        assertTrue(eventsChecker.isStarted(event, caseData));

    }

    @Test
    public void testHasMandatoryCompleted() {
        Event event = Event.TYPE_OF_APPLICATION;
        CaseData caseData = CaseData.builder().build();
        when(applicationTypeChecker.hasMandatoryCompleted(caseData)).thenReturn(Boolean.TRUE);
        eventsChecker.init();
        assertTrue(eventsChecker.hasMandatoryCompleted(event, caseData));

    }

    @Test
    public void testGetDefaultState() {
        Event event = Event.TYPE_OF_APPLICATION;
        CaseData caseData = CaseData.builder().build();
        when(applicationTypeChecker.getDefaultTaskState(caseData)).thenReturn(TaskState.NOT_STARTED);
        eventsChecker.init();
        assertEquals(TaskState.NOT_STARTED,eventsChecker.getDefaultState(event,caseData));

    }

}
