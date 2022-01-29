package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

        assertFalse(applicantsChecker.isFinished(caseData));

    }

    @Test
    public void whenApplicantIsNotPresentThenIsFinishedReturnsFalse() {

        CaseData caseData = CaseData.builder().applicants(null).build();

        assertFalse(applicantsChecker.isFinished(caseData));

    }

    @Test
    public void whenApplicantPresentThenIsStartedReturnsTrue() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder().applicants(applicantList).build();

        assertTrue(applicantsChecker.isStarted(caseData));

    }

    @Test
    public void whenApplicantIsNotPresentThenIsStartedReturnsFalse() {

        CaseData caseData = CaseData.builder().applicants(null).build();

        assertFalse(applicantsChecker.isStarted(caseData));

    }

    @Test
    public void whenApplicantPresentButNotCompletedThenHasMandatoryReturnsFalse() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        CaseData caseData = CaseData.builder().applicants(applicantList).build();

        assertFalse(applicantsChecker.hasMandatoryCompleted(caseData));

    }

    @Test
    public void whenApplicantIsNotPresentThenHasMandatoryReturnsFalse() {

        CaseData caseData = CaseData.builder().applicants(null).build();

        assertFalse(applicantsChecker.hasMandatoryCompleted(caseData));

    }

    @Test
    public void whenIncompleteAddressDataThenVerificationReturnsFalse() {
        Address address = Address.builder()
            .addressLine2("Test")
            .country("UK")
            .build();

        assertFalse(applicantsChecker.verifyAddressCompleted(address));
    }

    @Test
    public void whenCompleteAddressDataThenVerificationReturnsTrue() {
        Address address = Address.builder()
            .addressLine1("Test")
            .addressLine2("Test")
            .addressLine3("Test")
            .county("London")
            .country("UK")
            .postTown("Southgate")
            .postCode("N14 5EF")
            .build();

        assertTrue(applicantsChecker.verifyAddressCompleted(address));
    }


}
