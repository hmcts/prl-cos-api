package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class FL401ApplicantFamilyCheckerTest {

    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    FL401ApplicantFamilyChecker fl401ApplicantFamilyChecker;

    private CaseData caseData;
    private ApplicantFamilyDetails applicantFamilyDetails;
    private ApplicantChild applicantChild;

    @Before
    public void setUp() {

        caseData = CaseData.builder().build();
        applicantFamilyDetails = ApplicantFamilyDetails.builder().build();
        applicantChild = ApplicantChild.builder().build();

    }

    @Test
    public void whenNoCaseDataThenIsStartedIsFalse() {

        Assertions.assertFalse(fl401ApplicantFamilyChecker.isStarted(caseData));
    }

    @Test
    public void whenPartialCaseDataWhenApplicantHasFamilyDetailsThenIsStartedTrue() {

        applicantFamilyDetails = applicantFamilyDetails.toBuilder()
            .doesApplicantHaveChildren(Yes)
            .build();

        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .build();


        Assertions.assertTrue(fl401ApplicantFamilyChecker.isStarted(caseData));
    }

    @Test
    public void whenNoCaseDataThenNotFinished() {

        Assertions.assertFalse(fl401ApplicantFamilyChecker.isFinished(caseData));
    }

    @Test
    public void finishedFieldsValidatedToTrue() {

        applicantFamilyDetails = applicantFamilyDetails.toBuilder()
            .doesApplicantHaveChildren(No)
            .build();

        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .build();

        Assertions.assertTrue(fl401ApplicantFamilyChecker.isFinished(caseData));
    }

    @Test
    public void whenNoCaseDataThenHasMandatoryFalse() {

        Assertions.assertFalse(fl401ApplicantFamilyChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenFinishedCaseDataThenHasMandatoryFalse() {

        applicantFamilyDetails = applicantFamilyDetails.toBuilder()
            .doesApplicantHaveChildren(No)
            .build();

        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .build();

        Assertions.assertFalse(fl401ApplicantFamilyChecker.hasMandatoryCompleted(caseData));
    }

    @Test
    public void whenNoCaseDateValidateObjectFieldsReturnFalse() {

        Assertions.assertFalse(fl401ApplicantFamilyChecker.validateObjectFields(caseData));
    }

    @Test
    public void whenNullApplicantFamilyDetailsPresentValidateObjectFieldsReturnFalse() {

        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .build();

        Assertions.assertFalse(fl401ApplicantFamilyChecker.validateObjectFields(caseData));
    }

    @Test
    public void whenNoCaseDataValidateFieldsReturnsFalse() {

        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .build();

        Assertions.assertFalse(fl401ApplicantFamilyChecker.validateFields(caseData));
    }

    @Test
    public void whenApplicationHasChildWithEmptyApplicantChildValidateFieldsReturnsFalse() {

        Element<ApplicantChild> wrappedApplicantChild = Element.<ApplicantChild>builder().value(applicantChild).build();
        List<Element<ApplicantChild>> listOfApplicantChild = Collections.singletonList(wrappedApplicantChild);

        applicantFamilyDetails = applicantFamilyDetails.toBuilder()
            .doesApplicantHaveChildren(Yes)
            .applicantChildren(listOfApplicantChild)
            .build();

        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .build();

        Assertions.assertFalse(fl401ApplicantFamilyChecker.validateFields(caseData));
    }

    @Test
    public void whenApplicationHasChildWithSomeApplicantChildValidateFieldsReturnsFalse() {
        applicantChild = applicantChild.toBuilder()
            .fullName("Testing Child")
            .applicantChildRelationship("Testing")
            .build();

        Element<ApplicantChild> wrappedApplicantChild = Element.<ApplicantChild>builder().value(applicantChild).build();
        List<Element<ApplicantChild>> listOfApplicantChild = Collections.singletonList(wrappedApplicantChild);

        applicantFamilyDetails = applicantFamilyDetails.toBuilder()
            .doesApplicantHaveChildren(Yes)
            .applicantChildren(listOfApplicantChild)
            .build();

        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .build();

        Assertions.assertFalse(fl401ApplicantFamilyChecker.validateFields(caseData));
    }

    @Test
    public void whenApplicationHasChildWithAllApplicantChildValidateFieldsReturnsTrue() {
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
            .applicantChildren(listOfApplicantChild)
            .build();

        caseData = caseData.toBuilder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .build();

        Assertions.assertTrue(fl401ApplicantFamilyChecker.validateFields(caseData));
    }

    @Test
    public void applicantChildrenEmptyValidMandatoryFieldsCompleteReturnFalse() {

        Assertions.assertFalse(fl401ApplicantFamilyChecker.validateMandatoryFieldsCompleted(applicantChild));
    }

    @Test
    public void applicantChildrenHasPartialDataValidMandatoryFieldsCompleteReturnFalse() {
        applicantChild = applicantChild.toBuilder()
            .fullName("Testing")
            .applicantChildRelationship("Mother")
            .build();

        Assertions.assertFalse(fl401ApplicantFamilyChecker.validateMandatoryFieldsCompleted(applicantChild));
    }

    @Test
    public void applicantChildrenHasCompleteDataValidMandatoryFieldsCompleteReturnFalse() {
        applicantChild = applicantChild.toBuilder()
            .fullName("Testing Child")
            .dateOfBirth(LocalDate.of(2000, 12, 22))
            .applicantChildRelationship("TestingMother")
            .applicantRespondentShareParental(Yes)
            .respondentChildRelationship("Testing Step Father")
            .build();

        Assertions.assertTrue(fl401ApplicantFamilyChecker.validateMandatoryFieldsCompleted(applicantChild));
    }
}
