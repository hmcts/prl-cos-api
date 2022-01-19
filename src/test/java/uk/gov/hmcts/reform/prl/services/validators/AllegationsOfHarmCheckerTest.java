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

import static uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren.applicants;
import static uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren.children;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;

@RunWith(MockitoJUnitRunner.class)
public class AllegationsOfHarmCheckerTest {

    @Mock
    TaskErrorService taskErrorService;
    @InjectMocks
    AllegationsOfHarmChecker allegationsOfHarmChecker;


    @Test
    public void whenNoCaseDataThenIsStartedIsFalse() {

        CaseData casedata = CaseData.builder().build();

        assert !allegationsOfHarmChecker.isStarted(casedata);

    }

    @Test
    public void whenPartialCaseDataThenIsStartedTrue() {
        CaseData casedata = CaseData.builder()
            .allegationsOfHarmYesNo(YES)
            .build();

        assert  allegationsOfHarmChecker.isStarted(casedata);
    }



    @Test
    public void whenNoCaseDataThenNotFinished() {

        CaseData casedata = CaseData.builder().build();

        boolean isFinished = allegationsOfHarmChecker.isFinished(casedata);

        assert (!isFinished);
    }

    @Test
    public void finishedFieldsValidatedToTrue() {

        CaseData casedata = CaseData.builder()
            .allegationsOfHarmYesNo(NO)
            .build();

        boolean isFinished = allegationsOfHarmChecker.isFinished(casedata);

        assert (isFinished);
    }

    @Test
    public void validateAbusePresentFalse() {
        CaseData casedata = CaseData.builder().build();

        boolean isAbusePresent = allegationsOfHarmChecker.isStarted(casedata);

        assert (!isAbusePresent);
    }

    @Test
    public void whenNoCaseDataThenHasMandatoryFalse() {

        CaseData casedata = CaseData.builder().build();

        assert !allegationsOfHarmChecker.hasMandatoryCompleted(casedata);

    }

    @Test
    public void whenFinishedCaseDataThenHasMandatoryFalse() {

        CaseData casedata = CaseData.builder()
            .allegationsOfHarmYesNo(NO)
            .build();

        assert !allegationsOfHarmChecker.hasMandatoryCompleted(casedata);

    }

    @Test
    public void whenNoCaseDataValidateFieldsReturnsFalse() {
        CaseData caseData = CaseData.builder().build();

        assert !allegationsOfHarmChecker.validateFields(caseData);
    }

    @Test
    public void whenAbuseDataPresentThenAbusePresentReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .allegationsOfHarmDomesticAbuseYesNo(YES)
            .physicalAbuseVictim(Collections.singletonList(applicants))
            .build();

    }

    @Test
    public void whenNoCaseDataThenValidateOtherConcernsIsFalse() {
        CaseData caseData = CaseData.builder().build();

        assert !allegationsOfHarmChecker.validateOtherConcerns(caseData);

    }

    @Test
    public void whenOtherConcernsPresentThenValidateOtherConcernsTrue() {
        CaseData caseData = CaseData.builder()
            .allegationsOfHarmOtherConcernsYesNo(YES)
            .allegationsOfHarmOtherConcernsDetails("Details")
            .allegationsOfHarmOtherConcernsCourtActions("Court actions")
            .build();

        assert allegationsOfHarmChecker.validateOtherConcerns(caseData);
    }

    @Test
    public void whenNoCaseDataThenValidateChildContactIsFalse() {
        CaseData caseData = CaseData.builder().build();

        assert !allegationsOfHarmChecker.validateChildContact(caseData);

    }

    @Test
    public void whenChildContactPresentThenValidateChildContactTrue() {
        CaseData caseData = CaseData.builder()
            .agreeChildUnsupervisedTime(YES)
            .agreeChildSupervisedTime(YES)
            .agreeChildOtherContact(YES)
            .build();

        assert allegationsOfHarmChecker.validateChildContact(caseData);
    }

    @Test
    public void whenNonMolestationOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .ordersNonMolestation(YES)
            .ordersNonMolestationCurrent(YES)
            .build();

        assert allegationsOfHarmChecker.validateNonMolestationOrder(caseData);
    }

    @Test
    public void whenOccupationOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .ordersOccupation(YES)
            .ordersOccupationCurrent(YES)
            .build();

        assert allegationsOfHarmChecker.validateOccupationOrder(caseData);
    }

    @Test
    public void whenForcedMarriageOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .ordersForcedMarriageProtection(YES)
            .ordersForcedMarriageProtectionCurrent(YES)
            .build();

        assert allegationsOfHarmChecker.validateForcedMarriageProtectionOrder(caseData);
    }

    @Test
    public void whenRestrainingOrderOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .ordersRestraining(YES)
            .ordersRestrainingCurrent(YES)
            .build();

        assert allegationsOfHarmChecker.validateRestrainingOrder(caseData);
    }

    @Test
    public void whenOtherInjunctiveOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .ordersOtherInjunctive(YES)
            .ordersOtherInjunctiveCurrent(YES)
            .build();

        assert allegationsOfHarmChecker.validateOtherInjunctiveOrder(caseData);
    }

    @Test
    public void whenUndertakingOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .ordersUndertakingInPlace(YES)
            .ordersUndertakingInPlaceCurrent(YES)
            .build();

        assert allegationsOfHarmChecker.validateUndertakingInPlaceOrder(caseData);
    }

    @Test
    public void whenNoCaseDataThenAbductionSectionNotComplete() {
        CaseData caseData = CaseData.builder().build();

        assert !allegationsOfHarmChecker.validateAbductionSection(caseData);
    }

    @Test
    public void whenAnyAbusePresentThenReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationsOfHarmDomesticAbuseYesNo(YES)
            .build();

        assert allegationsOfHarmChecker.abusePresent(caseData);
    }

    @Test
    public void whenBehaviourPresentButIncompleteReturnFalse() {

        Behaviours behaviour = Behaviours.builder()
            .abuseNatureDescription("Test String")
            .build();

        assert !allegationsOfHarmChecker.validateBehaviour(behaviour);
    }

    @Test
    public void whenCompleteBehaviourReturnTrue() {

        Behaviours behaviour = Behaviours.builder()
            .abuseNatureDescription("Test")
            .behavioursStartDateAndLength("5 days")
            .behavioursNature("Testing")
            .behavioursApplicantSoughtHelp(YES)
            .behavioursApplicantHelpSoughtWho("Who from")
            .behavioursApplicantHelpAction("Action")
            .build();

        assert allegationsOfHarmChecker.validateBehaviour(behaviour);


    }

    @Test
    public void whenAbuseSectionCompleteReturnTrue() {

        Behaviours behaviour = Behaviours.builder().build();
        Element<Behaviours> wrappedBehaviour = Element.<Behaviours>builder()
            .value(behaviour)
            .build();

        CaseData caseData = CaseData.builder()
            .allegationsOfHarmDomesticAbuseYesNo(YES)
            .sexualAbuseVictim(Collections.singletonList(children))
            .behaviours(Collections.singletonList(wrappedBehaviour))
            .build();

        assert allegationsOfHarmChecker.validateDomesticAbuseSection(caseData);

    }

    @Test
    public void whenAbuseInCompleteReturnFalse() {

        CaseData caseData = CaseData.builder()
            .allegationsOfHarmDomesticAbuseYesNo(YES)
            .build();

        assert !allegationsOfHarmChecker.validateDomesticAbuseSection(caseData);

    }

    @Test
    public void whenOrderPresentButIncompleteReturnsFalse() {
        CaseData caseData = CaseData.builder()
            .ordersRestraining(YES)
            .ordersRestrainingCourtName("Test Court Name")
            .build();

        assert !allegationsOfHarmChecker.validateOrders(caseData);
    }

    @Test
    public void whenOrderPresentAndCompleteMandatoryDataReturnTrue() {
        CaseData caseData = CaseData.builder()
            .ordersOtherInjunctiveCurrent(YES)
            .ordersOtherInjunctiveCurrent(NO)
            .build();

        assert allegationsOfHarmChecker.validateOrders(caseData);
    }

}
