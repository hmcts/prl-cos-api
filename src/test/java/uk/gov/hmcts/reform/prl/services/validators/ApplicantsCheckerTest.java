package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;

import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ApplicantsCheckerTest {

    @Mock
    private TaskErrorService taskErrorService;

    @InjectMocks
    private ApplicantsChecker applicantsChecker;

    @Test
    public void whenApplicantPresentButNotCompleteThenIsFinishedReturnsFalse() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder().applicants(applicantList).build();

        assert !applicantsChecker.isFinished(caseData);

    }

    @Test
    public void whenApplicantIsNotPresentThenIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder().applicants(null).build();

        assert !applicantsChecker.isFinished(caseData);

    }

    @Test
    public void whenApplicantPresentThenIsStartedReturnsTrue() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder().applicants(applicantList).build();

        assert applicantsChecker.isStarted(caseData);

    }

    @Test
    public void whenApplicantIsNotPresentThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder().applicants(null).build();

        assert !applicantsChecker.isStarted(caseData);

    }

    @Test
    public void whenApplicantPresentButNotCompletedThenHasMandatoryReturnsFalse() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder().applicants(applicantList).build();

        assert !applicantsChecker.hasMandatoryCompleted(caseData);

    }

    @Test
    public void whenApplicantIsNotPresentThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().applicants(null).build();

        assert !applicantsChecker.hasMandatoryCompleted(caseData);

    }



}
