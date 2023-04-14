package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ATTENDING_THE_COURT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONSENT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CURRENT_OR_PREVIOUS_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.MIAM;

@RunWith(MockitoJUnitRunner.class)

public class RespondentsEventsChekerTest {

    @Mock
    ConsentToApplicationChecker consentToApplicationChecker;

    @Mock
    KeepDetailsPrivateChecker keepDetailsPrivateChecker;

    @Mock
    RespondentMiamChecker respondentMiamChecker;

    @Mock
    AbilityToParticipateChecker abilityToParticipateChecker;

    @Mock
    AttendToCourtChecker attendToCourtChecker;

    @Mock
    CurrentOrPastProceedingsChecker currentOrPastProceedingsChecker;

    @Mock
    InternationalElementsChecker internationalElementsChecker;

    @Mock
    RespondentContactDetailsChecker respondentContactDetailsChecker;

    @Mock
    RespondentAllegationsOfHarmChecker respondentAllegationsOfHarmChecker;


    @InjectMocks
    RespondentEventsChecker respondentEventsChecker;

    final CaseData caseData = CaseData.builder().build();

    final PartyDetails respondent = PartyDetails.builder().build();

    @Before
    public void init() {
        respondentEventsChecker.init();
    }


    @Test
    public void whenConsentEventHasMandatory_thenReturnsTrue() {
        when(consentToApplicationChecker.isFinished(respondent)).thenReturn(true);
        assertTrue(respondentEventsChecker.isFinished(CONSENT, respondent));

    }

    @Test
    public void whenAllegationsOfHarmEventHasMandatoryCompleted_MandatoryReturnsTrue() {
        when(respondentAllegationsOfHarmChecker.isFinished(respondent)).thenReturn(true);
        assertTrue(respondentEventsChecker.isFinished(ALLEGATION_OF_HARM, respondent));

    }

    @Test
    public void whenKeepDetailsPrivateEventIsStarted_thenEventCheckerStartedReturnsTrue() {
        when(keepDetailsPrivateChecker.isStarted(respondent)).thenReturn(true);
        assertTrue(respondentEventsChecker.isStarted(KEEP_DETAILS_PRIVATE, respondent));
    }

    @Test
    public void whenInternationalElementEventIsStarted_thenEventCheckerStartedReturnsTrue() {
        when(internationalElementsChecker.isStarted(respondent)).thenReturn(true);
        assertTrue(respondentEventsChecker.isStarted(INTERNATIONAL_ELEMENT, respondent));
    }

    @Test
    public void checkGetMiamEventStatus() {
        when(respondentMiamChecker.isFinished(respondent)).thenReturn(true);
        assertTrue(respondentEventsChecker.isFinished(MIAM, respondent));
    }

    @Test
    public void checkAttendToCourtEventStatus() {
        when(attendToCourtChecker.isFinished(respondent)).thenReturn(true);
        assertTrue(respondentEventsChecker.isFinished(ATTENDING_THE_COURT, respondent));
    }

    @Test
    public void checkGetEventStatus() {
        assertTrue(respondentEventsChecker.getEventStatus().containsKey(CURRENT_OR_PREVIOUS_PROCEEDINGS));
        assertTrue(respondentEventsChecker.getEventStatus().containsValue(currentOrPastProceedingsChecker));
    }

    @Test
    public void checkContactDetailsEventStatus() {
        assertTrue(respondentEventsChecker.getEventStatus().containsKey(CONFIRM_EDIT_CONTACT_DETAILS));
        assertTrue(respondentEventsChecker.getEventStatus().containsValue(respondentContactDetailsChecker));
    }
}

