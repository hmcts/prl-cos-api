package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@ExtendWith(MockitoExtension.class)
class ApplicantsCheckerTest {

    @Mock
    private TaskErrorService taskErrorService;

    @InjectMocks
    private ApplicantsChecker applicantsChecker;

    @Mock
    private CaseData caseData;

    @BeforeEach
    void setup() {
        caseData = CaseData.builder().build();
    }


    @Test
    void whenApplicantPresentButNotCompleteThenIsFinishedReturnsFalse() {

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
    void whenApplicantPresentButNotCompleteThenIsFinishedReturnsFalseForFL401() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(applicant)
            .applicants(applicantList)
            .build();

        assertFalse(applicantsChecker.isFinished(caseData));
    }

    @Test
    void whenApplicantIsNotPresentThenIsFinishedReturnsFalse() {

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(null)
            .build();

        assertFalse(applicantsChecker.isFinished(caseData));
    }

    @Test
    void whenApplicantPresentThenIsStartedReturnsTrue() {

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
    void whenApplicantIsNotPresentThenIsStartedReturnsFalse() {
        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(null)
            .build();

        assertFalse(applicantsChecker.isStarted(caseData));
    }

    @Test
    void whenApplicantPresentButNotCompletedThenHasMandatoryReturnsFalse() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").liveInRefuge(YesOrNo.Yes).build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicantList)
            .build();

        assertFalse(applicantsChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenApplicantPresentButNotCompletedThenHasMandatoryReturnsFalseForFL401() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(applicant)
            .applicants(applicantList)
            .build();

        assertFalse(applicantsChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenApplicantIsNotPresentThenHasMandatoryReturnsFalse() {
        caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicants(null)
            .build();

        assertFalse(applicantsChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenApplicantIsNotPresentThenHasMandatoryReturnsFalseForCourtAdmin() {
        caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.COURT_ADMIN)
            .applicantsFL401(PartyDetails.builder().liveInRefuge(YesOrNo.Yes).build())
            .build();

        assertFalse(applicantsChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenApplicantPresentButNotCompletedThenHasMandatoryReturnsFalseForCourtAdmin() {

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
    void whenApplicantPresentButNotCompletedThenHasMandatoryReturnsFalseForSolicitorFields() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName")
            .representativeFirstName("test")
            .representativeLastName("test")
            .solicitorEmail("test@test.com")
            .solicitorAddress(Address.builder()
                                  .addressLine1("address line 1")
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
    void whenApplicantWithOrgCompletedThenHasMandatoryReturnsFalseForSolicitorFields() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName")
            .representativeFirstName("test")
            .representativeLastName("test")
            .solicitorEmail("test@test.com")
            .solicitorOrg(Organisation.builder().organisationID("testId").build())
            .solicitorAddress(Address.builder()
                                  .addressLine1("address line 1")
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
    void whenSolicitorAddressIsEmptyThenHasMandatoryReturnsFalseForSolicitorFields() {

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
    void whenApplicantPresentAndSolicitorCreateCaseThenHasMandatoryReturnsFalseForSolicitorFields() {

        PartyDetails applicant = PartyDetails.builder().firstName("TestName")
            .representativeFirstName("test")
            .representativeLastName("test")
            .solicitorEmail("test@test.com")
            .solicitorAddress(Address.builder()
                                  .addressLine1("address line 1")
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
    void whenIncompleteAddressDataThenVerificationReturnsFalse() {
        Address address = Address.builder()
            .addressLine2("Test")
            .country("UK")
            .build();

        assertFalse(applicantsChecker.verifyAddressCompleted(address));
    }

    @Test
    void whenCompleteAddressDataThenVerificationReturnsTrue() {
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
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(applicantsChecker.getDefaultTaskState(CaseData.builder().build()));
    }

}
