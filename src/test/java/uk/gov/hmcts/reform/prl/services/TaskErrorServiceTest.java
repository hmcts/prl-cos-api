package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.EventErrorsEnum;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ALLEGATIONS_OF_HARM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.FL401_TYPE_OF_APPLICATION_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.FL401_TYPE_OF_APPLICATION_TYPE_OF_ORDER_ERROR;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.MIAM_ERROR;

public class TaskErrorServiceTest {

    TaskErrorService taskErrorService;

    int previousMapSize = 0;
    Event event = ALLEGATIONS_OF_HARM;
    EventErrorsEnum error = ALLEGATIONS_OF_HARM_ERROR;
    String errorString = ALLEGATIONS_OF_HARM_ERROR.toString();
    Event fl401Event = FL401_TYPE_OF_APPLICATION;
    EventErrorsEnum fl401Error = FL401_TYPE_OF_APPLICATION_ERROR;
    EventErrorsEnum fl401NestedError = FL401_TYPE_OF_APPLICATION_TYPE_OF_ORDER_ERROR;

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
    public void whenGetErrorsCalledThenListOfErrorsReturned() {
        taskErrorService.addEventError(event, error, errorString);

        List<EventValidationErrors> expectedList = new ArrayList<>();
        EventValidationErrors errors = EventValidationErrors.builder()
            .event(event)
            .errors(Collections.singletonList(errorString))
            .build();
        expectedList.add(errors);

        boolean listSizeEqual = expectedList.size() == taskErrorService.getEventErrors().size();
        boolean listContentsEqual = expectedList.containsAll(taskErrorService.getEventErrors());

        assertTrue(listSizeEqual && listContentsEqual);
    }

    @Test
    public void whenNestedErrorAddedPresentInList() {
        taskErrorService.addEventError(fl401Event, fl401Error, fl401Error.getError());
        taskErrorService.addNestedEventErrors(fl401Event, fl401Error, Collections.singletonList(fl401NestedError));
        List<String> actual = taskErrorService.eventErrors.get(fl401Error).getNestedErrors();

        assertTrue(actual.contains(fl401NestedError.getError()));
    }
}
