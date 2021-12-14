package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;

import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.List;

public class ApplicantsCheckerTest {

    @Test
    public void whenApplicantPresentThenIsFinishedReturnsTrue() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder().applicants(applicantList).build();

        ApplicantsChecker applicantsChecker = new ApplicantsChecker();

        assert applicantsChecker.isFinished(caseData);

    }

    @Test
    public void whenApplicantIsNotPresentThenIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder().applicants(null).build();

        ApplicantsChecker applicantsChecker = new ApplicantsChecker();

        assert !applicantsChecker.isFinished(caseData);

    }

    @Test
    public void whenApplicantPresentThenIsStartedReturnsTrue() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder().applicants(applicantList).build();

        ApplicantsChecker applicantsChecker = new ApplicantsChecker();

        assert applicantsChecker.isStarted(caseData);

    }

    @Test
    public void whenApplicantIsNotPresentThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder().applicants(null).build();

        ApplicantsChecker applicantsChecker = new ApplicantsChecker();

        assert !applicantsChecker.isStarted(caseData);

    }

    @Test
    public void whenApplicantPresentThenHasMandatoryReturnsFalse() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder().applicants(applicantList).build();

        ApplicantsChecker applicantsChecker = new ApplicantsChecker();

        assert !applicantsChecker.hasMandatoryCompleted(caseData);

    }

    @Test
    public void whenApplicantIsNotPresentThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().applicants(null).build();

        ApplicantsChecker applicantsChecker = new ApplicantsChecker();

        assert !applicantsChecker.hasMandatoryCompleted(caseData);

    }



}
