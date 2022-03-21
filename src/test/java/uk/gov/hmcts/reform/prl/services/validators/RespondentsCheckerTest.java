package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class RespondentsCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    RespondentsChecker respondentsChecker;

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(respondentsChecker.isStarted(caseData));
    }

    @Test
    public void whenNoCaseDataThenIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(respondentsChecker.isFinished(caseData));
    }

    @Test
    public void whenNoCaseDataThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        assertFalse(respondentsChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenMinimalRelevantCaseDataThenIsStartedReturnsTrue() {
        PartyDetails respondent = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedRespondents);

        CaseData caseData = CaseData.builder()
            .respondents(applicantList)
            .build();

        assertTrue(respondentsChecker.isStarted(caseData));
    }

    @Test
    public void whenIncompleteCaseDataValidateMandatoryFieldsForRespondentReturnsFalse() {
        PartyDetails respondent = PartyDetails.builder().firstName("TestName").build();

        CaseData caseData = CaseData.builder().caseTypeOfApplication("Test")
            .build();

        assertFalse(respondentsChecker.validateMandatoryFieldsForRespondent(respondent, caseData.getCaseTypeOfApplication()));
    }

    @Test
    public void whenIncompleteCaseDataRespondentsDetailsStartedReturnsTrue() {
        PartyDetails respondent = PartyDetails.builder().firstName("TestName").build();

        assertTrue(respondentsChecker.respondentDetailsStarted(respondent));
    }

    @Test
    public void testThatC100SpecificFieldsAreReturned() {
        PartyDetails respondent = PartyDetails.builder()
            .gender(Gender.other)
            .otherGender("Other gender")
            .isPlaceOfBirthKnown(YesOrNo.Yes)
            .placeOfBirth("Test place")
            .isAtAddressLessThan5YearsWithDontKnow(YesNoDontKnow.yes)
            .addressLivedLessThan5YearsDetails("test details")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorEmail("test@gmail.com").build();

        List<Optional<?>> actual = respondentsChecker.getSpecificC100Fields(respondent);
        assertEquals(8, actual.size());
        assertTrue(actual.contains(Optional.ofNullable(respondent.getOtherGender())));
    }

    @Test
    public void whenCaseTypeIsC100AndIncompleteData_thenC100FieldsAddedToFieldsListAndFinishedReturnFalse() {
        PartyDetails respondent = PartyDetails.builder()
            .firstName("TestName")
            .gender(Gender.other)
            .otherGender("Gender")
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .respondents(Collections.singletonList(element(respondent)))
            .build();

        assertFalse(respondentsChecker.isFinished(caseData));
    }


}
