package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class RespondentsCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    RespondentsChecker respondentsChecker;

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
}
