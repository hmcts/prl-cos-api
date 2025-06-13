package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ALLEGATION_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.ATTENDING_THE_COURT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONFIRM_EDIT_CONTACT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.CONSENT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.KEEP_DETAILS_PRIVATE;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.MIAM;
import static uk.gov.hmcts.reform.prl.enums.c100respondentsolicitor.RespondentSolicitorEvents.OTHER_PROCEEDINGS;

@ExtendWith(MockitoExtension.class)

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

    @Mock
    ViewDraftResponseChecker viewDraftResponseChecker;


    @Mock
    ResponseSubmitChecker responseSubmitChecker;


    @InjectMocks
    RespondentEventsChecker respondentEventsChecker;

    final CaseData caseData = CaseData.builder().build();

    final PartyDetails respondent = PartyDetails.builder().build();

    @BeforeEach
    void init() {
        respondentEventsChecker.init();
    }


    @Test
    void whenConsentEventHasMandatory_thenReturnsTrue() {
        when(consentToApplicationChecker.isFinished(respondent, true)).thenReturn(true);
        assertTrue(respondentEventsChecker.isFinished(CONSENT, respondent, true));

    }

    @Test
    void whenAllegationsOfHarmEventHasMandatoryCompleted_MandatoryReturnsTrue() {
        when(respondentAllegationsOfHarmChecker.isFinished(respondent, true)).thenReturn(true);
        assertTrue(respondentEventsChecker.isFinished(ALLEGATION_OF_HARM, respondent, true));

    }

    @Test
    void whenKeepDetailsPrivateEventIsStarted_thenEventCheckerStartedReturnsTrue() {
        when(keepDetailsPrivateChecker.isStarted(respondent, true)).thenReturn(true);
        assertTrue(respondentEventsChecker.isStarted(KEEP_DETAILS_PRIVATE, respondent, true));
    }

    @Test
    void whenInternationalElementEventIsStarted_thenEventCheckerStartedReturnsTrue() {
        when(internationalElementsChecker.isStarted(respondent, true)).thenReturn(true);
        assertTrue(respondentEventsChecker.isStarted(INTERNATIONAL_ELEMENT, respondent, true));
    }

    @Test
    void checkGetMiamEventStatus() {
        when(respondentMiamChecker.isFinished(respondent, true)).thenReturn(true);
        assertTrue(respondentEventsChecker.isFinished(MIAM, respondent, true));
    }

    @Test
    void checkAttendToCourtEventStatus() {
        when(attendToCourtChecker.isFinished(respondent, true)).thenReturn(true);
        assertTrue(respondentEventsChecker.isFinished(ATTENDING_THE_COURT, respondent, true));
    }

    @Test
    void checkGetEventStatus() {
        assertTrue(respondentEventsChecker.getEventStatus().containsKey(OTHER_PROCEEDINGS));
        assertTrue(respondentEventsChecker.getEventStatus().containsValue(currentOrPastProceedingsChecker));
    }

    @Test
    void checkContactDetailsEventStatus() {
        assertTrue(respondentEventsChecker.getEventStatus().containsKey(CONFIRM_EDIT_CONTACT_DETAILS));
        assertTrue(respondentEventsChecker.getEventStatus().containsValue(respondentContactDetailsChecker));
    }

    @Test
    void testCheckerFields() {
        assertNotNull(respondentEventsChecker.getConsentToApplicationChecker());
        assertNotNull(respondentEventsChecker.getKeepDetailsPrivateChecker());
        assertNotNull(respondentEventsChecker.getRespondentMiamChecker());
        assertNotNull(respondentEventsChecker.getAttendToCourtChecker());
        assertNotNull(respondentEventsChecker.getAbilityToParticipateChecker());
        assertNotNull(respondentEventsChecker.getInternationalElementsChecker());
        assertNotNull(respondentEventsChecker.getViewDraftResponseChecker());
        assertNotNull(respondentEventsChecker.getCurrentOrPastProceedingsChecker());
        assertNotNull(respondentEventsChecker.getRespondentContactDetailsChecker());
        assertNotNull(respondentEventsChecker.getRespondentAllegationsOfHarmChecker());
        assertNotNull(respondentEventsChecker.getResponseSubmitChecker());
    }
}

