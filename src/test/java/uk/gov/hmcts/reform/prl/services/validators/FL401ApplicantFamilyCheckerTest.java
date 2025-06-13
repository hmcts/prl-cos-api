package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class FL401ApplicantFamilyCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    FL401ApplicantFamilyChecker fl401ApplicantFamilyChecker;

    private CaseData caseData;
    private ApplicantFamilyDetails applicantFamilyDetails;
    private ApplicantChild applicantChild;

    @BeforeEach
    void setUp() {

        caseData = CaseData.builder().build();
        applicantFamilyDetails = ApplicantFamilyDetails.builder().build();
        applicantChild = ApplicantChild.builder().build();
    }

    @Test
    void whenNoCaseDataThenIsStartedIsFalse() {
        assertFalse(fl401ApplicantFamilyChecker.isStarted(caseData));
    }

    @Test
    void whenNoCaseDataThenNotFinished() {
        assertFalse(fl401ApplicantFamilyChecker.isFinished(caseData));
    }

    @Test
    void whenNoCaseDataThenHasMandatoryFalse() {
        assertFalse(fl401ApplicantFamilyChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenNoCaseDateValidateObjectFieldsReturnFalse() {
        assertFalse(fl401ApplicantFamilyChecker.validateObjectFields(caseData));
    }

    @Test
    void applicantChildrenEmptyValidMandatoryFieldsCompleteReturnFalse() {
        assertFalse(fl401ApplicantFamilyChecker.validateMandatoryFieldsCompleted(applicantChild));
    }

    @Test
    void whenPartialCaseDataWhenApplicantHasFamilyDetailsThenIsStartedTrue() {
        applicantFamilyDetails = applicantFamilyDetails.toBuilder()
            .doesApplicantHaveChildren(Yes)
            .build();

        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .build();

        assertTrue(fl401ApplicantFamilyChecker.isStarted(caseData));
    }

    @Test
    void finishedFieldsValidatedToTrue() {
        applicantFamilyDetails = applicantFamilyDetails.toBuilder()
            .doesApplicantHaveChildren(No)
            .build();

        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .build();

        assertTrue(fl401ApplicantFamilyChecker.isFinished(caseData));
    }

    @Test
    void whenFinishedCaseDataThenHasMandatoryFalse() {
        applicantFamilyDetails = applicantFamilyDetails.toBuilder()
            .doesApplicantHaveChildren(No)
            .build();

        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .build();

        assertFalse(fl401ApplicantFamilyChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    void whenNullApplicantFamilyDetailsPresentValidateObjectFieldsReturnFalse() {
        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .build();

        assertFalse(fl401ApplicantFamilyChecker.validateObjectFields(caseData));
    }

    @Test
    void whenNoCaseDataValidateFieldsReturnsFalse() {
        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .build();

        assertFalse(fl401ApplicantFamilyChecker.validateFields(caseData));
    }

    @Test
    void whenApplicationHasChildWithEmptyApplicantChildValidateFieldsReturnsFalse() {
        Element<ApplicantChild> wrappedApplicantChild = Element.<ApplicantChild>builder().value(applicantChild).build();
        List<Element<ApplicantChild>> listOfApplicantChild = Collections.singletonList(wrappedApplicantChild);

        applicantFamilyDetails = applicantFamilyDetails.toBuilder()
            .doesApplicantHaveChildren(Yes)
            .build();

        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .applicantChildDetails(listOfApplicantChild)
            .build();

        assertFalse(fl401ApplicantFamilyChecker.validateFields(caseData));
    }

    @Test
    void whenApplicationHasChildWithSomeApplicantChildValidateFieldsReturnsFalse() {
        applicantChild = applicantChild.toBuilder()
            .fullName("Testing Child")
            .applicantChildRelationship("Testing")
            .build();

        Element<ApplicantChild> wrappedApplicantChild = Element.<ApplicantChild>builder().value(applicantChild).build();
        List<Element<ApplicantChild>> listOfApplicantChild = Collections.singletonList(wrappedApplicantChild);

        applicantFamilyDetails = applicantFamilyDetails.toBuilder()
            .doesApplicantHaveChildren(Yes)
            .build();

        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .applicantChildDetails(listOfApplicantChild)
            .build();

        assertFalse(fl401ApplicantFamilyChecker.validateFields(caseData));
    }

    @Test
    void whenApplicationHasChildWithAllApplicantChildValidateFieldsReturnsTrue() {
        applicantChild = applicantChild.toBuilder()
            .fullName("Testing Child")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .applicantChildRelationship("TestingMother")
            .applicantRespondentShareParental(Yes)
            .respondentChildRelationship("Testing Step Father")
            .build();

        Element<ApplicantChild> wrappedApplicantChild = Element.<ApplicantChild>builder().value(applicantChild).build();
        List<Element<ApplicantChild>> listOfApplicantChild = Collections.singletonList(wrappedApplicantChild);

        applicantFamilyDetails = applicantFamilyDetails.toBuilder()
            .doesApplicantHaveChildren(Yes)
            .build();

        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .applicantChildDetails(listOfApplicantChild)
            .build();

        assertTrue(fl401ApplicantFamilyChecker.validateFields(caseData));
    }

    @Test
    void applicantChildrenHasPartialDataValidMandatoryFieldsCompleteReturnFalse() {
        applicantChild = applicantChild.toBuilder()
            .fullName("Testing")
            .applicantChildRelationship("Mother")
            .build();

        assertFalse(fl401ApplicantFamilyChecker.validateMandatoryFieldsCompleted(applicantChild));
    }

    @Test
    void applicantChildrenHasCompleteDataValidMandatoryFieldsCompleteReturnFalse() {
        applicantChild = applicantChild.toBuilder()
            .fullName("Testing Child")
            .dateOfBirth(LocalDate.of(2010, 12, 22))
            .applicantChildRelationship("TestingMother")
            .applicantRespondentShareParental(Yes)
            .respondentChildRelationship("Testing Step Father")
            .build();

        assertTrue(fl401ApplicantFamilyChecker.validateMandatoryFieldsCompleted(applicantChild));
    }

    @Test
    void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(fl401ApplicantFamilyChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
