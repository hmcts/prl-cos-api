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
    CaseNameChecker caseNameChecker;



    @Before
    public void init() {
        eventsChecker.init();
    }

    @Test
    public void testIsFinished() {
        Event event = Event.CASE_NAME;
        CaseData caseData = CaseData.builder().build();
        when(caseNameChecker.isFinished(caseData)).thenReturn(Boolean.TRUE);
        eventsChecker.init();
        assertTrue(eventsChecker.isFinished(event, caseData));

    }

    @Test
    public void testIsStarted() {
        Event event = Event.CASE_NAME;
        CaseData caseData = CaseData.builder().build();
        when(caseNameChecker.isStarted(caseData)).thenReturn(Boolean.TRUE);
        eventsChecker.init();
        assertTrue(eventsChecker.isStarted(event, caseData));

    }

    @Test
    public void testHasMandatoryCompleted() {
        Event event = Event.CASE_NAME;
        CaseData caseData = CaseData.builder().build();
        when(caseNameChecker.hasMandatoryCompleted(caseData)).thenReturn(Boolean.TRUE);
        eventsChecker.init();
        assertTrue(eventsChecker.hasMandatoryCompleted(event, caseData));

    }

    @Test
    public void testGetDefaultState() {
        Event event = Event.CASE_NAME;
        CaseData caseData = CaseData.builder().build();
        when(caseNameChecker.getDefaultTaskState(caseData)).thenReturn(TaskState.NOT_STARTED);
        eventsChecker.init();
        assertEquals(TaskState.NOT_STARTED,eventsChecker.getDefaultState(event,caseData));

    }

}
