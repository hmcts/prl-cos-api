package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings.CurrentOrPreviousProceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorAbilityToParticipateInProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ResponseSubmitCheckerTest {

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
    AttendToCourtChecker attendToCourtChecker;

    @Mock
    CurrentOrPastProceedingsChecker currentOrPastProceedingsChecker;

    @Mock
    RespondentContactDetailsChecker respondentContactDetailsChecker;

    @Mock
    RespondentAllegationsOfHarmChecker respondentAllegationsOfHarmChecker;

    CaseData emptyCaseData;

    @Before
    public void setup() {

        PartyDetails respondent = PartyDetails.builder()
            .response(Response
                          .builder()
                          .activeRespondent(Yes)
                          .consent(Consent
                                       .builder()
                                       .build())
                          .keepDetailsPrivate(KeepDetailsPrivate
                                                  .builder()
                                                  .build())
                          .abilityToParticipate(SolicitorAbilityToParticipateInProceedings
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
                          .build())
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        emptyCaseData = CaseData.builder().respondents(respondentList).build();
    }

    @Test
    public void isStarted() {

        Boolean bool = responseSubmitChecker.isStarted(emptyCaseData);
        assertFalse(bool);
    }

    @Test
    public void hasMandatoryCompletedFalse() {

        when(respondentEventsChecker.getConsentToApplicationChecker()).thenReturn(consentToApplicationChecker);
        when(respondentEventsChecker.getKeepDetailsPrivateChecker()).thenReturn(keepDetailsPrivateChecker);
        when(respondentEventsChecker.getAbilityToParticipateChecker()).thenReturn(abilityToParticipateChecker);
        when(respondentEventsChecker.getAttendToCourtChecker()).thenReturn(attendToCourtChecker);
        when(respondentEventsChecker.getCurrentOrPastProceedingsChecker()).thenReturn(currentOrPastProceedingsChecker);
        when(respondentEventsChecker.getRespondentAllegationsOfHarmChecker()).thenReturn(respondentAllegationsOfHarmChecker);
        when(respondentEventsChecker.getRespondentContactDetailsChecker()).thenReturn(respondentContactDetailsChecker);


        Boolean bool = responseSubmitChecker.hasMandatoryCompleted(emptyCaseData);

        assertFalse(bool);
    }
}
