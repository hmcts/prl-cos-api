package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.EventErrorsEnum;
import uk.gov.hmcts.reform.prl.models.EventValidationErrors;
import uk.gov.hmcts.reform.prl.models.tasklist.Task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ALLEGATIONS_OF_HARM_ERROR;

public class TaskErrorServiceTest {

    @Test
    public void whenAddEventErrorCalledThenMapIncreasesInSize() {

        TaskErrorService taskErrorService = new TaskErrorService();

        Event event = ALLEGATIONS_OF_HARM;
        EventErrorsEnum error = ALLEGATIONS_OF_HARM_ERROR;
        String errorString = ALLEGATIONS_OF_HARM_ERROR.toString();

        int previousMapSize = taskErrorService.eventErrors.size();
        taskErrorService.addEventError(event, error ,errorString);

        assert taskErrorService.eventErrors.size() == previousMapSize + 1;

    }

    @Test
    public void whenRemoveEventErrorCalledThenMapDecreasesInSize() {

        TaskErrorService taskErrorService = new TaskErrorService();

        Event event = ALLEGATIONS_OF_HARM;
        EventErrorsEnum error = ALLEGATIONS_OF_HARM_ERROR;
        String errorString = ALLEGATIONS_OF_HARM_ERROR.toString();
        taskErrorService.addEventError(event, error ,errorString);

        int previousMapSize = taskErrorService.eventErrors.size();

        taskErrorService.removeError(error);
        assert taskErrorService.eventErrors.size() == previousMapSize - 1;

    }

    @Test
    public void whenAddEventErrorCalledThenMapContainsError() {

        TaskErrorService taskErrorService = new TaskErrorService();

        Event event = ALLEGATIONS_OF_HARM;
        EventErrorsEnum error = ALLEGATIONS_OF_HARM_ERROR;
        String errorString = ALLEGATIONS_OF_HARM_ERROR.toString();
        taskErrorService.addEventError(event, error ,errorString);

        assert taskErrorService.eventErrors.containsKey(error);

    }

    @Test
    public void whenRemoveEventErrorCalledThenMapNoLongerContainsError() {

        TaskErrorService taskErrorService = new TaskErrorService();

        Event event = ALLEGATIONS_OF_HARM;
        EventErrorsEnum error = ALLEGATIONS_OF_HARM_ERROR;
        String errorString = ALLEGATIONS_OF_HARM_ERROR.toString();
        taskErrorService.addEventError(event, error ,errorString);

        taskErrorService.removeError(error);

        assert !taskErrorService.eventErrors.containsKey(error);

    }

    @Test
    public void whenGetErrorsCalledThenListOfErrorsReturned() {

        TaskErrorService taskErrorService = new TaskErrorService();

        Event event = ALLEGATIONS_OF_HARM;
        EventErrorsEnum error = ALLEGATIONS_OF_HARM_ERROR;
        String errorString = ALLEGATIONS_OF_HARM_ERROR.toString();
        taskErrorService.addEventError(event, error ,errorString);

        List<EventValidationErrors> expectedList = new ArrayList<>();
        EventValidationErrors errors = EventValidationErrors.builder()
            .event(event)
            .errors(Collections.singletonList(errorString))
            .build();
        expectedList.add(errors);

        boolean listSizeEqual = expectedList.size() == taskErrorService.getEventErrors().size();
        boolean listContentsEqual = expectedList.containsAll(taskErrorService.getEventErrors());

        assert listSizeEqual && listContentsEqual;

    }





}
