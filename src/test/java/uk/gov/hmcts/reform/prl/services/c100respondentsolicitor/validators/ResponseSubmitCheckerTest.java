package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.abilitytoparticipate.AbilityToParticipate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings.CurrentOrPreviousProceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResponseToAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResponseSubmitCheckerTest {

    @InjectMocks
    ResponseSubmitChecker responseSubmitChecker;

    @Mock
    RespondentEventsChecker respondentEventsChecker;

    @Mock
    ConsentToApplicationChecker consentToApplicationChecker;

    @Mock
    KeepDetailsPrivateChecker keepDetailsPrivateChecker;

    @Mock
    AbilityToParticipateChecker abilityToParticipateChecker;

    @Mock
    RespondentMiamChecker respondentMiamChecker;

    @Mock
    AttendToCourtChecker attendToCourtChecker;

    @Mock
    CurrentOrPastProceedingsChecker currentOrPastProceedingsChecker;

    @Mock
    RespondentContactDetailsChecker respondentContactDetailsChecker;

    @Mock
    RespondentAllegationsOfHarmChecker respondentAllegationsOfHarmChecker;

    @Mock
    ResponseToAllegationsOfHarmChecker responseToAllegationsOfHarmChecker;

    @Mock
    InternationalElementsChecker internationalElementsChecker;

    CaseData emptyCaseData;

    PartyDetails respondent;

    PartyDetails emptyRespondent;

    @BeforeEach
    void setup() {
        emptyRespondent = PartyDetails.builder().build();

        PartyDetails respondent = PartyDetails.builder()
                .response(Response
                        .builder()
                        .consent(Consent
                                .builder()
                                .build())
                        .keepDetailsPrivate(KeepDetailsPrivate
                                .builder()
                                .build())
                        .abilityToParticipate(AbilityToParticipate
                                .builder()
                                .build())
                        .attendToCourt(AttendToCourt
                                .builder()
                                .build())
                        .currentOrPreviousProceedings(CurrentOrPreviousProceedings
                                .builder()
                                .build())
                        .respondentAllegationsOfHarmData(RespondentAllegationsOfHarmData
                                .builder()
                                .build())
                        .responseToAllegationsOfHarm(ResponseToAllegationsOfHarm
                                .builder()
                                .build())
                        .build())
                .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        emptyCaseData = CaseData.builder().respondents(respondentList).build();
    }

    @Test
    void isStarted() {

        Boolean bool = responseSubmitChecker.isStarted(emptyRespondent, true);
        assertFalse(bool);
    }

    @Test
    void hasMandatoryCompletedFalse() {

        when(respondentEventsChecker.getConsentToApplicationChecker()).thenReturn(consentToApplicationChecker);
        when(respondentEventsChecker.getKeepDetailsPrivateChecker()).thenReturn(keepDetailsPrivateChecker);
        when(respondentEventsChecker.getAbilityToParticipateChecker()).thenReturn(abilityToParticipateChecker);
        when(respondentEventsChecker.getAttendToCourtChecker()).thenReturn(attendToCourtChecker);
        when(respondentEventsChecker.getCurrentOrPastProceedingsChecker()).thenReturn(currentOrPastProceedingsChecker);
        when(respondentEventsChecker.getRespondentAllegationsOfHarmChecker()).thenReturn(respondentAllegationsOfHarmChecker);
        when(respondentEventsChecker.getRespondentContactDetailsChecker()).thenReturn(respondentContactDetailsChecker);
        when(respondentEventsChecker.getResponseToAllegationsOfHarmChecker()).thenReturn(responseToAllegationsOfHarmChecker);


        Boolean bool = responseSubmitChecker.isFinished(emptyRespondent, true);

        assertFalse(bool);
    }

    @Test
    void hasMandatoryCompletedTrue() {

        when(respondentEventsChecker.getConsentToApplicationChecker()).thenReturn(consentToApplicationChecker);
        when(consentToApplicationChecker.isFinished(respondent, true)).thenReturn(true);

        when(respondentEventsChecker.getKeepDetailsPrivateChecker()).thenReturn(keepDetailsPrivateChecker);
        when(keepDetailsPrivateChecker.isFinished(respondent, true)).thenReturn(true);

        when(respondentEventsChecker.getRespondentMiamChecker()).thenReturn(respondentMiamChecker);
        when(respondentMiamChecker.isFinished(respondent, true)).thenReturn(true);

        when(respondentEventsChecker.getAbilityToParticipateChecker()).thenReturn(abilityToParticipateChecker);
        when(abilityToParticipateChecker.isFinished(respondent, true)).thenReturn(true);

        when(respondentEventsChecker.getAttendToCourtChecker()).thenReturn(attendToCourtChecker);
        when(attendToCourtChecker.isFinished(respondent, true)).thenReturn(true);

        when(respondentEventsChecker.getCurrentOrPastProceedingsChecker()).thenReturn(currentOrPastProceedingsChecker);
        when(currentOrPastProceedingsChecker.isFinished(respondent, true)).thenReturn(true);

        when(respondentEventsChecker.getRespondentAllegationsOfHarmChecker()).thenReturn(respondentAllegationsOfHarmChecker);
        when(respondentAllegationsOfHarmChecker.isFinished(respondent, true)).thenReturn(true);

        when(respondentEventsChecker.getRespondentContactDetailsChecker()).thenReturn(respondentContactDetailsChecker);
        when(respondentContactDetailsChecker.isFinished(respondent, true)).thenReturn(true);

        when(respondentEventsChecker.getInternationalElementsChecker()).thenReturn(internationalElementsChecker);
        when(internationalElementsChecker.isFinished(respondent, true)).thenReturn(true);

        when(respondentEventsChecker.getResponseToAllegationsOfHarmChecker()).thenReturn(responseToAllegationsOfHarmChecker);
        when(responseToAllegationsOfHarmChecker.isFinished(respondent, true)).thenReturn(true);

        Boolean bool = responseSubmitChecker.isFinished(respondent, true);

        assertTrue(bool);
    }
}
