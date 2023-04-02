package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.ATTENDING_THE_COURT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.CONSENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.CURRENT_OR_PREVIOUS_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents.MIAM;

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

    @Before
    public void init() {
        respondentEventsChecker.init();
    }


    @Test
    public void whenConsentEventHasMandatory_thenReturnsTrue() {
        when(consentToApplicationChecker.isFinished(caseData, "A")).thenReturn(true);
        assertTrue(respondentEventsChecker.isFinished(CONSENT, caseData, "A"));

    }

    @Test
    public void whenAllegationsOfHarmEventHasMandatoryCompleted_MandatoryReturnsTrue() {
        when(respondentAllegationsOfHarmChecker.isFinished(caseData, "A")).thenReturn(true);
        assertTrue(respondentEventsChecker.isFinished(ALLEGATION_OF_HARM, caseData, "A"));

    }

    @Test
    public void whenKeepDetailsPrivateEventIsStarted_thenEventCheckerStartedReturnsTrue() {
        when(keepDetailsPrivateChecker.isStarted(caseData, "A")).thenReturn(true);
        assertTrue(respondentEventsChecker.isStarted(KEEP_DETAILS_PRIVATE, caseData, "A"));
    }

    @Test
    public void whenInternationalElementEventIsStarted_thenEventCheckerStartedReturnsTrue() {
        when(internationalElementsChecker.isStarted(caseData, "A")).thenReturn(true);
        assertTrue(respondentEventsChecker.isStarted(INTERNATIONAL_ELEMENT, caseData, "A"));
    }

    @Test
    public void checkGetMiamEventStatus() {
        when(respondentMiamChecker.isFinished(caseData, "A")).thenReturn(true);
        assertTrue(respondentEventsChecker.isFinished(MIAM, caseData, "A"));
    }

    @Test
    public void checkAttendToCourtEventStatus() {
        when(attendToCourtChecker.isFinished(caseData, "A")).thenReturn(true);
        assertTrue(respondentEventsChecker.isFinished(ATTENDING_THE_COURT, caseData, "A"));
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

