package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum;
import uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.models.c100respondentsolicitor.RespondentEventValidationErrors;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.ALLEGATIONS_OF_HARM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentEventErrorsEnum.ALLEGATION_OF_HARM_ERROR;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.MIAM;

public class RespondentTaskErrorServiceTest {

    RespondentTaskErrorService respondentTaskErrorService;

    int previousMapSize = 0;
    RespondentSolicitorEvents event = ALLEGATION_OF_HARM;
    RespondentEventErrorsEnum error = ALLEGATION_OF_HARM_ERROR;
    String errorString = ALLEGATIONS_OF_HARM_ERROR.toString();


    @Before
    public void setUp() {
        respondentTaskErrorService = new RespondentTaskErrorService();
        respondentTaskErrorService.addEventError(event, error, errorString);
        previousMapSize = respondentTaskErrorService.eventErrors.size();
    }

    @Test
    public void whenAddEventErrorCalledThenMapIncreasesInSize() {
        RespondentSolicitorEvents newEvent = MIAM;
        RespondentEventErrorsEnum newEventError = RespondentEventErrorsEnum.MIAM_ERROR;

        respondentTaskErrorService.addEventError(newEvent, newEventError, errorString);
        assertThat(respondentTaskErrorService.eventErrors).hasSize(previousMapSize + 1);
        assertTrue(respondentTaskErrorService.eventErrors.containsKey(newEventError));
    }

    @Test
    public void whenRemoveEventErrorCalledThenMapDecreasesInSize() {
        respondentTaskErrorService.removeError(error);
        assertThat(respondentTaskErrorService.eventErrors).hasSize(previousMapSize - 1);
        assertTrue(!respondentTaskErrorService.eventErrors.containsKey(error));
    }

    @Test
    public void whenClearingErrorsCalledThenMapCleared() {
        respondentTaskErrorService.clearErrors();
        assertThat(respondentTaskErrorService.eventErrors).isEmpty();
    }

    @Test
    public void whenGetErrorsCalledThenListOfErrorsReturned() {
        final CaseData caseData = CaseData.builder()
                .id(nextLong())
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .build();
        respondentTaskErrorService.addEventError(event, error, errorString);
        List<RespondentEventValidationErrors> expectedList = new ArrayList<>();
        RespondentEventValidationErrors errors = RespondentEventValidationErrors.builder()
                .event(event)
                .errors(Collections.singletonList(errorString))
                .build();
        expectedList.add(errors);

        boolean listSizeEqual = expectedList.size() == respondentTaskErrorService.getEventErrors(caseData).size();
        boolean listContentsEqual = expectedList.containsAll(respondentTaskErrorService.getEventErrors(caseData));

        assertTrue(listSizeEqual && listContentsEqual);
    }

}