package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.testng.AssertJUnit.assertNotNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class RespondentsCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    RespondentsChecker respondentsChecker;

    Address address;

    @Before
    public void setUp() {
        address = Address.builder()
            .addressLine1("55 Test Street")
            .postTown("Town")
            .postCode("N12 3BH")
            .build();
    }

    @Test
    public void whenNoCaseDataThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        Assert.assertFalse(respondentsChecker.isStarted(caseData));
    }

    @Test
    public void whenNoCaseDataThenIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        Assert.assertFalse(respondentsChecker.isFinished(caseData));
    }

    @Test
    public void whenIncompleteCaseDataThenIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder().caseTypeOfApplication(FL401_CASE_TYPE)
            .respondentsFL401(PartyDetails.builder().build()).build();

        Assert.assertFalse(respondentsChecker.isFinished(caseData));
    }

    @Test
    public void whenNoCaseDataThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().build();

        Assert.assertFalse(respondentsChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenMinimalRelevantCaseDataThenIsStartedReturnsTrue() {
        PartyDetails respondent = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedRespondents);

        CaseData caseData = CaseData.builder()
            .respondents(applicantList)
            .build();

        Assert.assertTrue(respondentsChecker.isStarted(caseData));
    }

    @Test
    public void whenIncompleteCaseDataValidateMandatoryFieldsForRespondentReturnsFalse() {
        PartyDetails respondent = PartyDetails.builder().firstName("TestName").build();

        CaseData caseData = CaseData.builder().caseTypeOfApplication("Test")
            .build();

        Assert.assertFalse(respondentsChecker.validateMandatoryFieldsForRespondent(respondent, caseData.getCaseTypeOfApplication()));
    }

    @Test
    public void whenIncompleteC100CaseDataValidateMandatoryFieldsForRespondentReturnsFalse() {
        PartyDetails respondent = PartyDetails.builder()
            .firstName("TestName")
            .build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();

        Assert.assertFalse(respondentsChecker.validateMandatoryFieldsForRespondent(respondent, caseData.getCaseTypeOfApplication()));
    }

    @Test
    public void whenIncompleteCaseDataRespondentsDetailsStartedReturnsTrue() {
        PartyDetails respondent = PartyDetails.builder().firstName("TestName").build();

        Assert.assertTrue(respondentsChecker.respondentDetailsStarted(respondent));
    }

    @Test
    public void whenNoDataIsGenderCompletedFieldShouldEmpty() {
        PartyDetails respondent = PartyDetails.builder().build();
        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isGenderCompleted(respondent,fields);
        Assert.assertFalse(fields.size() > 1 && !fields.get(0).isEmpty());
    }

    @Test
    public void whenDataPresentIsGenderCompletedFieldShouldNotNull() {
        PartyDetails respondent = PartyDetails.builder()
            .firstName("TestName")
            .gender(Gender.other)
            .otherGender("Testing")
            .build();
        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isGenderCompleted(respondent,fields);
        Assert.assertTrue(fields.size() > 1 && !fields.get(0).isEmpty());

    }

    @Test
    public void whenNoDataIsPlaceOfBirthCompletedFieldShouldEmpty() {
        PartyDetails respondent = PartyDetails.builder().build();
        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isPlaceOfBirthCompleted(respondent,fields);
        Assert.assertFalse(fields.size() > 1 && !fields.get(0).isEmpty());
    }

    @Test
    public void whenDataPresentIsPlaceOfBirthCompletedFieldShouldNotNull() {
        PartyDetails respondent = PartyDetails.builder()
            .firstName("TestName")
            .isPlaceOfBirthKnown(Yes)
            .placeOfBirth("testing")
            .build();

        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isPlaceOfBirthCompleted(respondent,fields);
        Assert.assertTrue(fields.size() > 1 && !fields.get(0).isEmpty());

    }

    @Test
    public void whenNoDataIsCurrentAddressCompletedFieldShouldEmpty() {
        PartyDetails respondent = PartyDetails.builder().build();
        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isCurrentAddressCompleted(respondent,fields);
        Assert.assertFalse(fields.size() > 1 && !fields.get(0).isEmpty());
    }

    @Test
    public void whenDataPresentIsCurrentAddressCompletedFieldShouldNotNull() {
        PartyDetails respondent = PartyDetails.builder()
            .isCurrentAddressKnown(Yes)
            .isPlaceOfBirthKnown(Yes)
            .placeOfBirth("testing")
            .address(address)
            .build();

        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isCurrentAddressCompleted(respondent,fields);
        Assert.assertTrue(fields.size() > 1 && !fields.get(0).isEmpty());

    }

    @Test
    public void whenNoDataIsCanYouProvideEmailAddressCompletedFieldShouldEmpty() {
        PartyDetails respondent = PartyDetails.builder().build();
        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isCanYouProvideEmailAddressCompleted(respondent,fields);
        Assert.assertFalse(fields.size() > 1 && !fields.get(0).isEmpty());
    }

    @Test
    public void whenDataPresentIsCanYouProvideEmailAddressCompletedFieldShouldNotNull() {
        PartyDetails respondent = PartyDetails.builder()
            .canYouProvideEmailAddress(Yes)
            .email("testing@gmail.com")
            .build();

        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isCanYouProvideEmailAddressCompleted(respondent,fields);
        Assert.assertTrue(fields.size() > 1 && !fields.get(0).isEmpty());

    }

    @Test
    public void whenNoDataIsAtAddressLessThan5YearsCompletedFieldShouldEmpty() {
        PartyDetails respondent = PartyDetails.builder().build();
        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isAtAddressLessThan5YearsCompleted(respondent,fields);
        Assert.assertFalse(fields.size() > 1 && !fields.get(0).isEmpty());
    }

    @Test
    public void whenDataPresentIsAtAddressLessThan5YearsCompletedFieldShouldNotNull() {
        PartyDetails respondent = PartyDetails.builder()
            .isAtAddressLessThan5YearsWithDontKnow(YesNoDontKnow.yes)
            .addressLivedLessThan5YearsDetails("testing")
            .build();

        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isAtAddressLessThan5YearsCompleted(respondent,fields);
        Assert.assertTrue(fields.size() > 1 && !fields.get(0).isEmpty());

    }

    @Test
    public void whenNoDataIsDoTheyHaveLegalRepresentationCompletedFieldShouldEmpty() {
        PartyDetails respondent = PartyDetails.builder().build();
        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isDoTheyHaveLegalRepresentationCompleted(respondent,fields);
        Assert.assertFalse(fields.size() > 1 && !fields.get(0).isEmpty());
    }

    @Test
    public void whenDataPresentIsDoTheyHaveLegalRepresentationCompletedFieldShouldNotNull() {
        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorEmail("solicitor@gmail.com")
            .build();

        List<Optional<?>> fields = new ArrayList<>();
        respondentsChecker.isDoTheyHaveLegalRepresentationCompleted(respondent,fields);
        Assert.assertTrue(fields.size() > 1 && !fields.get(0).isEmpty());

    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(respondentsChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
