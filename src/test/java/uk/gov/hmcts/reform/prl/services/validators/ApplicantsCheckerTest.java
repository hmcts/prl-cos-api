package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class ApplicantsCheckerTest {

    @Mock
    private TaskErrorService taskErrorService;

    @InjectMocks
    private ApplicantsChecker applicantsChecker;

    @Mock
    private CaseData caseData;

    @Before
    public void setup() {
        caseData = CaseData.builder().build();
    }


    @Test
    public void whenApplicantPresentButNotCompleteThenIsFinishedReturnsFalse() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicantList)
            .build();

        assertFalse(applicantsChecker.isFinished(caseData));
    }

    @Test
    public void whenApplicantIsNotPresentThenIsFinishedReturnsFalse() {

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(null)
            .build();

        assertFalse(applicantsChecker.isFinished(caseData));
    }

    @Test
    public void whenApplicantPresentThenIsStartedReturnsTrue() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicantList)
            .build();

        assertTrue(applicantsChecker.isStarted(caseData));
    }

    @Test
    public void whenApplicantIsNotPresentThenIsStartedReturnsFalse() {
        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(null)
            .build();

        assertFalse(applicantsChecker.isStarted(caseData));
    }

    @Test
    public void whenApplicantPresentButNotCompletedThenHasMandatoryReturnsFalse() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicantList)
            .build();

        assertFalse(applicantsChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenApplicantIsNotPresentThenHasMandatoryReturnsFalse() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicants(null)
            .build();

        assertFalse(applicantsChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenApplicantIsNotPresentThenHasMandatoryReturnsFalseForCourtAdmin() {
        caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.COURT_ADMIN)
            .applicants(null)
            .build();

        assertFalse(applicantsChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenApplicantPresentButNotCompletedThenHasMandatoryReturnsFalseForCourtAdmin() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.COURT_ADMIN)
            .applicants(applicantList)
            .build();

        assertFalse(applicantsChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenApplicantPresentButNotCompletedThenHasMandatoryReturnsFalseForSolicitorFields() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName")
            .representativeFirstName("test")
            .representativeLastName("test")
            .solicitorEmail("test@test.com")
            .solicitorAddress(Address.builder()
                                  .addressLine1("address lin1")
                                  .postCode("ADVWE11").build()).build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.COURT_ADMIN)
            .applicants(applicantList)
            .build();

        assertFalse(applicantsChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenApplicantWithOrgCompletedThenHasMandatoryReturnsFalseForSolicitorFields() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName")
            .representativeFirstName("test")
            .representativeLastName("test")
            .solicitorEmail("test@test.com")
            .solicitorOrg(Organisation.builder().organisationID("testId").build())
            .solicitorAddress(Address.builder()
                                  .addressLine1("address lin1")
                                  .postCode("ADVWE11").build()).build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.COURT_ADMIN)
            .applicants(applicantList)
            .build();

        assertFalse(applicantsChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenSolicitorAddressIsEmptyThenHasMandatoryReturnsFalseForSolicitorFields() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName")
            .representativeFirstName("test")
            .representativeLastName("test")
            .solicitorEmail("test@test.com")
            .solicitorOrg(Organisation.builder().build())
            .solicitorAddress(Address.builder()
                                  .addressLine1("")
                                  .postCode("").build()).build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicantList)
            .build();

        assertFalse(applicantsChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenApplicantPresentAndSolicitorCreateCaseThenHasMandatoryReturnsFalseForSolicitorFields() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName")
            .representativeFirstName("test")
            .representativeLastName("test")
            .solicitorEmail("test@test.com")
            .solicitorAddress(Address.builder()
                                  .addressLine1("address lin1")
                                  .postCode("ADVWE11").build()).build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicantList)
            .build();

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

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(applicantsChecker.getDefaultTaskState(CaseData.builder().build()));
    }

}
