package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.EventErrorsEnum;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ALLEGATIONS_OF_HARM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.MIAM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.TYPE_OF_APPLICATION_ERROR;

public class TaskErrorServiceTest {

    TaskErrorService taskErrorService;

    int previousMapSize = 0;
    Event event = ALLEGATIONS_OF_HARM;
    EventErrorsEnum error = ALLEGATIONS_OF_HARM_ERROR;
    String errorString = ALLEGATIONS_OF_HARM_ERROR.toString();

    Event event2 = TYPE_OF_APPLICATION;
    EventErrorsEnum error2 = TYPE_OF_APPLICATION_ERROR;
    String errorString2 = TYPE_OF_APPLICATION_ERROR.toString();

    @Before
    public void setUp() {
        taskErrorService = new TaskErrorService();
        taskErrorService.addEventError(event, error, errorString);
        previousMapSize = taskErrorService.eventErrors.size();
    }

    @Test
    public void whenAddEventErrorCalledThenMapIncreasesInSize() {
        Event newEvent = MIAM;
        EventErrorsEnum newEventError = MIAM_ERROR;

        taskErrorService.addEventError(newEvent, newEventError, errorString);
        assertThat(taskErrorService.eventErrors).hasSize(previousMapSize + 1);
        assertTrue(taskErrorService.eventErrors.containsKey(newEventError));
    }

    @Test
    public void whenRemoveEventErrorCalledThenMapDecreasesInSize() {
        taskErrorService.removeError(error);
        assertThat(taskErrorService.eventErrors).hasSize(previousMapSize - 1);
        assertTrue(!taskErrorService.eventErrors.containsKey(error));
    }

    @Test
    public void whenClearingErrorsCalledThenMapCleared() {
        taskErrorService.clearErrors();
        assertThat(taskErrorService.eventErrors).isEmpty();
    }

    @Test
    public void whenGetErrorsCalledThenListOfErrorsReturned() {
        taskErrorService.addEventError(event, error, errorString);

        List<EventValidationErrors> expectedList = new ArrayList<>();
        EventValidationErrors errors = EventValidationErrors.builder()
            .event(event)
            .errors(Collections.singletonList(errorString))
            .build();
        expectedList.add(errors);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();

        boolean listSizeEqual = expectedList.size() == taskErrorService.getEventErrors(caseData).size();
        boolean listContentsEqual = expectedList.containsAll(taskErrorService.getEventErrors(caseData));

        assertTrue(listSizeEqual && listContentsEqual);
    }

    @Test
    public void testThatListOfErrorsInCorrectEventOrder() {
        TaskErrorService taskErrorServ = new TaskErrorService();

        taskErrorServ.addEventError(event, error, errorString);
        taskErrorServ.addEventError(event2, error2, errorString2);

        List<EventValidationErrors> expectedList = new ArrayList<>();

        EventValidationErrors error2 = EventValidationErrors.builder()
            .event(event2)
            .errors(Collections.singletonList(errorString2))
            .build();
        expectedList.add(error2);

        EventValidationErrors error = EventValidationErrors.builder()
            .event(event)
            .errors(Collections.singletonList(errorString))
            .build();
        expectedList.add(error);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();

        List<EventValidationErrors> actualList = taskErrorServ.getEventErrors(caseData);

        assertEquals(actualList.get(0), expectedList.get(0));
        assertEquals(actualList.get(1), expectedList.get(1));

    }
}
