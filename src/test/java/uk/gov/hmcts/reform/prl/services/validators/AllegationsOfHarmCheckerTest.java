package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren.applicants;
import static uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren.children;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class AllegationsOfHarmCheckerTest {
    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    AllegationsOfHarmChecker allegationsOfHarmChecker;

    @Test
    public void whenNoCaseDataThenIsStartedIsFalse() {
        CaseData casedata = CaseData.builder().build();

        assertFalse(allegationsOfHarmChecker.isStarted(casedata));
    }

    @Test
    public void whenPartialCaseDataThenIsStartedTrue() {
        CaseData casedata = CaseData.builder()
            .allegationsOfHarmYesNo(Yes)
            .build();

        assertTrue(allegationsOfHarmChecker.isStarted(casedata));
    }


    @Test
    public void whenNoCaseDataThenNotFinished() {

        CaseData casedata = CaseData.builder().build();

        boolean isFinished = allegationsOfHarmChecker.isFinished(casedata);

        assertFalse(isFinished);
    }

    @Test
    public void finishedFieldsValidatedToTrue() {

        CaseData casedata = CaseData.builder()
            .allegationsOfHarmYesNo(No)
            .build();

        boolean isFinished = allegationsOfHarmChecker.isFinished(casedata);

        assertTrue(isFinished);
    }

    @Test
    public void validateAbusePresentFalse() {
        CaseData casedata = CaseData.builder().build();

        boolean isAbusePresent = allegationsOfHarmChecker.isStarted(casedata);

        assertFalse(isAbusePresent);
    }

    @Test
    public void whenNoCaseDataThenHasMandatoryFalse() {

        CaseData casedata = CaseData.builder().build();

        assertFalse(allegationsOfHarmChecker.hasMandatoryCompleted(casedata));

    }

    @Test
    public void whenFinishedCaseDataThenHasMandatoryFalse() {

        CaseData casedata = CaseData.builder()
            .allegationsOfHarmYesNo(No)
            .build();

        assertFalse(allegationsOfHarmChecker.hasMandatoryCompleted(casedata));

    }

    @Test
    public void whenNoCaseDataValidateFieldsReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(allegationsOfHarmChecker.validateFields(caseData));
    }

    @Test
    public void whenAbuseDataPresentThenAbusePresentReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .allegationsOfHarmDomesticAbuseYesNo(Yes)
            .physicalAbuseVictim(Collections.singletonList(applicants))
            .build();

        assertTrue(allegationsOfHarmChecker.abusePresent(caseData));
    }

    @Test
    public void whenNoCaseDataThenValidateOtherConcernsIsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(allegationsOfHarmChecker.validateOtherConcerns(caseData));
    }

    @Test
    public void whenOtherConcernsPresentThenValidateOtherConcernsTrue() {
        CaseData caseData = CaseData.builder()
            .allegationsOfHarmOtherConcerns(Yes)
            .allegationsOfHarmOtherConcernsDetails("Details")
            .allegationsOfHarmOtherConcernsCourtActions("Court actions")
            .build();

        assertTrue(allegationsOfHarmChecker.validateOtherConcerns(caseData));
    }

    @Test
    public void whenNoCaseDataThenValidateChildContactIsFalse() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(allegationsOfHarmChecker.validateChildContact(caseData));

    }

    @Test
    public void whenChildContactPresentThenValidateChildContactTrue() {
        CaseData caseData = CaseData.builder()
            .agreeChildUnsupervisedTime(Yes)
            .agreeChildSupervisedTime(Yes)
            .agreeChildOtherContact(Yes)
            .build();

        assertTrue(allegationsOfHarmChecker.validateChildContact(caseData));
    }

    @Test
    public void whenNonMolestationOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .ordersNonMolestation(Yes)
            .ordersNonMolestationCurrent(Yes)
            .build();

        assertTrue(allegationsOfHarmChecker.validateNonMolestationOrder(caseData));
    }

    @Test
    public void whenOccupationOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .ordersOccupation(Yes)
            .ordersOccupationCurrent(Yes)
            .build();

        assertTrue(allegationsOfHarmChecker.validateOccupationOrder(caseData));
    }

    @Test
    public void whenForcedMarriageOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .ordersForcedMarriageProtection(Yes)
            .ordersForcedMarriageProtectionCurrent(Yes)
            .build();

        assertTrue(allegationsOfHarmChecker.validateForcedMarriageProtectionOrder(caseData));
    }

    @Test
    public void whenRestrainingOrderOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .ordersRestraining(Yes)
            .ordersRestrainingCurrent(Yes)
            .build();

        assertTrue(allegationsOfHarmChecker.validateRestrainingOrder(caseData));
    }

    @Test
    public void whenOtherInjunctiveOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .ordersOtherInjunctive(Yes)
            .ordersOtherInjunctiveCurrent(Yes)
            .build();

        assertTrue(allegationsOfHarmChecker.validateOtherInjunctiveOrder(caseData));
    }

    @Test
    public void whenUndertakingOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .ordersUndertakingInPlace(Yes)
            .ordersUndertakingInPlaceCurrent(Yes)
            .build();

        assertTrue(allegationsOfHarmChecker.validateUndertakingInPlaceOrder(caseData));
    }

    @Test
    public void whenNoCaseDataThenAbductionSectionNotComplete() {
        CaseData caseData = CaseData.builder().build();

        assertFalse(allegationsOfHarmChecker.validateAbductionSection(caseData));
    }

    @Test
    public void whenAnyAbusePresentThenReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationsOfHarmDomesticAbuseYesNo(Yes)
            .build();

        assertTrue(allegationsOfHarmChecker.abusePresent(caseData));
    }

    @Test
    public void whenBehaviourPresentButIncompleteReturnFalse() {

        Behaviours behaviour = Behaviours.builder()
            .abuseNatureDescription("Test String")
            .build();

        assertFalse(allegationsOfHarmChecker.validateBehaviour(behaviour));
    }

    @Test
    public void whenCompleteBehaviourReturnTrue() {

        Behaviours behaviour = Behaviours.builder()
            .abuseNatureDescription("Test")
            .behavioursStartDateAndLength("5 days")
            .behavioursNature("Testing")
            .behavioursApplicantSoughtHelp(Yes)
            .behavioursApplicantHelpSoughtWho("Who from")
            .behavioursApplicantHelpAction("Action")
            .build();

        assertTrue(allegationsOfHarmChecker.validateBehaviour(behaviour));
    }

    @Test
    public void whenAbuseSectionCompleteReturnTrue() {

        Behaviours behaviour = Behaviours.builder().build();
        Element<Behaviours> wrappedBehaviour = Element.<Behaviours>builder()
            .value(behaviour)
            .build();

        CaseData caseData = CaseData.builder()
            .allegationsOfHarmDomesticAbuseYesNo(Yes)
            .allegationsOfHarmChildAbductionYesNo(No)
            .sexualAbuseVictim(Collections.singletonList(children))
            .behaviours(Collections.singletonList(wrappedBehaviour))
            .build();

        assertTrue(allegationsOfHarmChecker.validateDomesticAbuseSection(caseData));
    }

    @Test
    public void whenAbuseInCompleteReturnFalse() {

        CaseData caseData = CaseData.builder()
            .allegationsOfHarmDomesticAbuseYesNo(Yes)
            .allegationsOfHarmChildAbductionYesNo(Yes)
            .childAbductionReasons("harm")
            .previousAbductionThreats(Yes)
            .previousAbductionThreatsDetails("none")
            .build();

        assertFalse(allegationsOfHarmChecker.validateDomesticAbuseSection(caseData));
    }

    @Test
    public void whenOrderPresentButIncompleteReturnsFalse() {
        CaseData caseData = CaseData.builder()
            .ordersRestraining(Yes)
            .ordersRestrainingCourtName("Test Court Name")
            .build();

        assertFalse(allegationsOfHarmChecker.validateOrders(caseData));
    }

    @Test
    public void whenOrderPresentAndCompleteMandatoryDataReturnTrue() {
        CaseData caseData = CaseData.builder()
            .ordersOtherInjunctiveCurrent(Yes)
            .ordersOtherInjunctiveCurrent(No)
            .build();

        assertTrue(allegationsOfHarmChecker.validateOrders(caseData));
    }
}
