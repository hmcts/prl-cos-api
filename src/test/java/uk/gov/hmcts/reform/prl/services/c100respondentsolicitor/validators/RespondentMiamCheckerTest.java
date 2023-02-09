package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorMiam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.Gender.female;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RespondentMiamCheckerTest {

    @InjectMocks
    private RespondentMiamChecker respondentMiamChecker;

    private CaseData caseData;

    @Before
    public void setup() {

        Response miamResponse = Response.builder()
            .solicitorMiam(SolicitorMiam.builder()
                               .respSolHaveYouAttendedMiam(Miam.builder()
                                                               .attendedMiam(No)
                                                               .build())
                               .respSolWillingnessToAttendMiam(Miam.builder()
                                                                   .willingToAttendMiam(No)
                                                                   .reasonNotAttendingMiam("test")
                                                                   .build())
                               .build())
            .activeRespondent(Yes)
            .build();

        PartyDetails respondents = PartyDetails.builder()
            .firstName("x")
            .lastName("x")
            .gender(female)
            .isDateOfBirthKnown(No)
            .isPlaceOfBirthKnown(No)
            .isCurrentAddressKnown(No)
            .isAtAddressLessThan5Years(No)
            .canYouProvideEmailAddress(No)
            .canYouProvidePhoneNumber(No)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.dontKnow)
            .response(miamResponse)
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondents).build();
        List<Element<PartyDetails>> respondentsList = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder()
            .id(1234L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("test")
            .respondents(respondentsList)
            .build();
    }

    @Test
    public void testMiamAttendedIsStarted() {

        boolean isStarted;
        isStarted = respondentMiamChecker.isStarted(caseData);
        assertTrue(isStarted);

    }

    @Test
    public void hasMandatoryCompletedTest() {
        boolean anyNonEmpty = respondentMiamChecker.hasMandatoryCompleted(caseData);

        Assert.assertTrue(anyNonEmpty);
    }

    @Test
    public void hasMandatoryCompletedTestWithAttendedMiamYes() {

        Response miamResponse = Response.builder()
            .solicitorMiam(SolicitorMiam.builder()
                               .respSolHaveYouAttendedMiam(Miam.builder()
                                                               .attendedMiam(Yes)
                                                               .build())

                               .build())
            .activeRespondent(Yes)
            .build();

        PartyDetails respondents = PartyDetails.builder()
            .response(miamResponse)
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondents).build();
        List<Element<PartyDetails>> respondentsList = Collections.singletonList(wrappedRespondents);

        CaseData caseData1 = CaseData.builder()
            .id(1234L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantCaseName("test")
            .respondents(respondentsList)
            .build();

        boolean anyNonEmpty = respondentMiamChecker.hasMandatoryCompleted(caseData1);

        Assert.assertTrue(anyNonEmpty);
    }
}
