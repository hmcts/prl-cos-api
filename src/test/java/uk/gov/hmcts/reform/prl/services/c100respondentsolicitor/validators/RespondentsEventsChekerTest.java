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
        when(consentToApplicationChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        assertTrue(respondentEventsChecker.hasMandatoryCompleted(CONSENT, caseData));

    }

    @Test
    public void whenAllegationsOfHarmEventHasMandatoryCompleted_MandatoryReturnsTrue() {
        when(respondentAllegationsOfHarmChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        assertTrue(respondentEventsChecker.hasMandatoryCompleted(ALLEGATION_OF_HARM, caseData));

    }

    @Test
    public void whenKeepDetailsPrivateEventIsStarted_thenEventCheckerStartedReturnsTrue() {
        when(keepDetailsPrivateChecker.isStarted(caseData)).thenReturn(true);
        assertTrue(respondentEventsChecker.isStarted(KEEP_DETAILS_PRIVATE, caseData));
    }

    @Test
    public void whenInternationalElementEventIsStarted_thenEventCheckerStartedReturnsTrue() {
        when(internationalElementsChecker.isStarted(caseData)).thenReturn(true);
        assertTrue(respondentEventsChecker.isStarted(INTERNATIONAL_ELEMENT, caseData));
    }

    @Test
    public void checkGetMiamEventStatus() {
        when(respondentMiamChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        assertTrue(respondentEventsChecker.hasMandatoryCompleted(MIAM, caseData));
    }

    @Test
    public void checkAttendToCourtEventStatus() {
        when(attendToCourtChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        assertTrue(respondentEventsChecker.hasMandatoryCompleted(ATTENDING_THE_COURT, caseData));
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

