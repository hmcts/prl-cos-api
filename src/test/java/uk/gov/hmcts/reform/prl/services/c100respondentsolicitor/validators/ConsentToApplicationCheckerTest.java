package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ConsentToApplicationCheckerTest {

    @InjectMocks
    ConsentToApplicationChecker consentToApplicationChecker;

    @Mock
    RespondentTaskErrorService respondentTaskErrorService;

    CaseData caseData;
    PartyDetails respondent;

    @Before
    public void setUp() {
        User user = User.builder().email("respondent@example.net")
            .idamId("1234-5678").solicitorRepresented(Yes).build();
        respondent = PartyDetails.builder()
            .user(user)
            .response(Response
                          .builder()
                          .consent(Consent
                                       .builder()
                                       .noConsentReason("test")
                                       .courtOrderDetails("test")
                                       .consentToTheApplication(No)
                                       .applicationReceivedDate(LocalDate.now())
                                       .permissionFromCourt(Yes)
                                       .build())
                          .build())
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder().respondents(respondentList).build();
    }

    @Test
    public void isStartedTest() {
        boolean anyNonEmpty = consentToApplicationChecker.isStarted(respondent, true);

        assertTrue(anyNonEmpty);
    }

    @Test
    public void isStartedTest_whenNot() {
        PartyDetails respondent = PartyDetails.builder()
            .response(Response
                          .builder()
                          .consent(Consent
                                       .builder()
                                       .noConsentReason(null)
                                       .courtOrderDetails(null)
                                       .consentToTheApplication(null)
                                       .applicationReceivedDate(null)
                                       .permissionFromCourt(null)
                                       .build())
                          .build())
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder().respondents(respondentList).build();
        doNothing().when(respondentTaskErrorService).addEventError(Mockito.any(),Mockito.any(),Mockito.any());
        boolean anyNonEmpty = consentToApplicationChecker.isStarted(respondent, true);

        assertFalse(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedTest() {
        boolean anyNonEmpty = consentToApplicationChecker.isFinished(respondent, true);

        Assert.assertTrue(anyNonEmpty);
    }

    @Test
    public void hasStartedNoResponse() {
        PartyDetails blankRespondent = PartyDetails.builder().build();
        boolean anyNonEmpty = consentToApplicationChecker.isStarted(blankRespondent, true);

        Assert.assertFalse(anyNonEmpty);
    }

    @Test
    public void hasFinishedNoResponse() {
        PartyDetails blankRespondent = PartyDetails.builder().build();
        boolean anyNonEmpty = consentToApplicationChecker.isFinished(blankRespondent, true);

        Assert.assertFalse(anyNonEmpty);
    }

    @Test
    public void hasFinishedEmptyResponse() {
        PartyDetails blankRespondent = PartyDetails
            .builder()
            .response(Response
                          .builder()
                          .build())
            .build();
        boolean anyNonEmpty = consentToApplicationChecker.isFinished(blankRespondent, true);

        Assert.assertFalse(anyNonEmpty);
    }
}
