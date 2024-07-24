package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;
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
        CaseData casedata = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder().build())
            .build();

        assertFalse(allegationsOfHarmChecker.isStarted(casedata));
    }

    @Test
    public void whenPartialCaseDataThenIsStartedTrue() {
        CaseData casedata = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes).build())
            .build();

        assertTrue(allegationsOfHarmChecker.isStarted(casedata));
    }

    @Test
    public void whenAllegationOfHarmSelectedNoIsStartedFalse() {
        CaseData casedata = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(No)
                                  .build())
            .build();

        assertFalse(allegationsOfHarmChecker.isStarted(casedata));
    }

    @Test
    public void whenNoCaseDataThenNotFinished() {

        CaseData casedata = CaseData.builder().allegationOfHarm(AllegationOfHarm.builder().build())
            .build();

        boolean isFinished = allegationsOfHarmChecker.isFinished(casedata);

        assertFalse(isFinished);
    }

    @Test
    public void finishedFieldsValidatedToTrue() {

        CaseData casedata = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(No).build())
            .build();

        boolean isFinished = allegationsOfHarmChecker.isFinished(casedata);

        assertTrue(isFinished);
    }

    @Test
    public void validateAbusePresentFalse() {
        CaseData casedata = CaseData.builder().allegationOfHarm(AllegationOfHarm.builder().build())
            .build();

        boolean isAbusePresent = allegationsOfHarmChecker.isStarted(casedata);

        assertFalse(isAbusePresent);
    }

    @Test
    public void whenNoCaseDataThenHasMandatoryFalse() {

        CaseData casedata = CaseData.builder().allegationOfHarm(AllegationOfHarm.builder().build())
            .build();

        assertFalse(allegationsOfHarmChecker.hasMandatoryCompleted(casedata));

    }

    @Test
    public void whenFinishedCaseDataThenHasMandatoryFalse() {

        CaseData casedata = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(No).build())
            .build();

        assertFalse(allegationsOfHarmChecker.hasMandatoryCompleted(casedata));

    }

    @Test
    public void whenNoCaseDataValidateFieldsReturnsFalse() {
        CaseData caseData = CaseData.builder().allegationOfHarm(AllegationOfHarm.builder().build())
            .build();

        assertFalse(allegationsOfHarmChecker.validateFields(caseData));
    }

    @Test
    public void whenAbuseDataPresentThenAbusePresentReturnsTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .physicalAbuseVictim(Collections.singletonList(applicants)).build())
            .build();

        assertTrue(allegationsOfHarmChecker.abusePresent(caseData));
    }

    @Test
    public void whenNoCaseDataThenValidateOtherConcernsIsFalse() {
        CaseData caseData = CaseData.builder().allegationOfHarm(AllegationOfHarm.builder().build())
            .build();

        assertFalse(allegationsOfHarmChecker.validateOtherConcerns(caseData));
    }

    @Test
    public void whenOtherConcernsPresentThenValidateOtherConcernsTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmOtherConcerns(Yes)
                                  .allegationsOfHarmOtherConcernsDetails("Details")
                                  .allegationsOfHarmOtherConcernsCourtActions("Court actions").build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateOtherConcerns(caseData));
    }

    @Test
    public void whenNoCaseDataThenValidateChildContactIsFalse() {
        CaseData caseData = CaseData.builder().allegationOfHarm(AllegationOfHarm.builder().build())
            .build();


        assertFalse(allegationsOfHarmChecker.validateChildContact(caseData));

    }

    @Test
    public void whenChildContactPresentThenValidateChildContactTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .agreeChildUnsupervisedTime(Yes)
                                  .agreeChildSupervisedTime(Yes)
                                  .agreeChildOtherContact(Yes).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateChildContact(caseData));
    }

    @Test
    public void whenNonMolestationOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .ordersNonMolestation(Yes)
                                  .ordersNonMolestationCurrent(Yes).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateNonMolestationOrder(caseData));
    }

    @Test
    public void whenOccupationOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .ordersOccupation(Yes)
                                  .ordersOccupationCurrent(Yes).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateOccupationOrder(caseData));
    }

    @Test
    public void whenForcedMarriageOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .ordersForcedMarriageProtection(Yes)
                                  .ordersForcedMarriageProtectionCurrent(Yes).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateForcedMarriageProtectionOrder(caseData));
    }

    @Test
    public void whenRestrainingOrderOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .ordersRestraining(Yes)
                                  .ordersRestrainingCurrent(Yes).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateRestrainingOrder(caseData));
    }

    @Test
    public void whenOtherInjunctiveOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .ordersOtherInjunctive(Yes)
                                  .ordersOtherInjunctiveCurrent(Yes).build())

            .build();

        assertTrue(allegationsOfHarmChecker.validateOtherInjunctiveOrder(caseData));
    }

    @Test
    public void whenUndertakingOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .ordersUndertakingInPlace(Yes)
                                  .ordersUndertakingInPlaceCurrent(Yes).build())

            .build();

        assertTrue(allegationsOfHarmChecker.validateUndertakingInPlaceOrder(caseData));
    }

    @Test
    public void whenNoCaseDataThenAbductionSectionNotComplete() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .build())
            .build();

        assertFalse(allegationsOfHarmChecker.validateAbductionSection(caseData));
    }

    @Test
    public void whenCaseDataPresentThenAbductionSectionReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmChildAbductionYesNo(Yes)
                                  .childAbductionReasons("testing")
                                  .previousAbductionThreats(Yes)
                                  .previousAbductionThreatsDetails("Details")
                                  .abductionPassportOfficeNotified(No)
                                  .abductionChildHasPassport(Yes)
                                  .abductionChildPassportPosession(AbductionChildPassportPossessionEnum.mother)
                                  .abductionPreviousPoliceInvolvement(No).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateAbductionSection(caseData));
    }

    @Test
    public void whenAnyAbusePresentThenReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes).build())

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
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .allegationsOfHarmChildAbductionYesNo(No)
                                  .sexualAbuseVictim(Collections.singletonList(children))
                                  .behaviours(Collections.singletonList(wrappedBehaviour)).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateDomesticAbuseSection(caseData));
    }

    @Test
    public void whenAbuseInCompleteReturnFalse() {

        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .allegationsOfHarmChildAbductionYesNo(Yes)
                                  .childAbductionReasons("harm")
                                  .previousAbductionThreats(Yes)
                                  .previousAbductionThreatsDetails("none").build())
            .build();

        assertFalse(allegationsOfHarmChecker.validateDomesticAbuseSection(caseData));
    }

    @Test
    public void whenOrderPresentButIncompleteReturnsFalse() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .ordersRestraining(Yes)
                                  .ordersRestrainingCourtName("Test Court Name").build())
            .build();

        assertFalse(allegationsOfHarmChecker.validateOrders(caseData));
    }

    @Test
    public void whenOrderPresentAndCompleteMandatoryDataReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .ordersOtherInjunctiveCurrent(Yes)
                                  .ordersOtherInjunctiveCurrent(No).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateOrders(caseData));
    }

    @Test
    public void whenNoDataIsSectionsFinishedReturnFalse() {
        CaseData caseData = CaseData.builder().allegationOfHarm(AllegationOfHarm.builder().build())
            .build();
        assertFalse(allegationsOfHarmChecker.isSectionsFinished(caseData,false,false));
    }

    @Test
    public void whenNoDataIsPreviousOrdersFinishedReturnFalse() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder().build())

            .build();
        AllegationOfHarm allegationOfHarm = caseData.getAllegationOfHarm();
        Optional<YesOrNo> ordersNonMolestation = ofNullable(allegationOfHarm.getOrdersNonMolestation());
        Optional<YesOrNo> ordersOccupation = ofNullable(allegationOfHarm.getOrdersOccupation());
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(allegationOfHarm.getOrdersForcedMarriageProtection());
        Optional<YesOrNo> ordersRestraining = ofNullable(allegationOfHarm.getOrdersRestraining());
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(allegationOfHarm.getOrdersOtherInjunctive());
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(allegationOfHarm.getOrdersUndertakingInPlace());

        assertFalse(allegationsOfHarmChecker.isPreviousOrdersFinished(
            ordersNonMolestation,
            ordersOccupation,
            ordersForcedMarriageProtection,
            ordersRestraining,
            ordersOtherInjunctive,
            ordersUndertakingInPlace
        ));
    }

    @Test
    public void whenDataPresentIsPreviousOrdersFinishedReturnFalse() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder().ordersNonMolestation(No)
                                  .ordersOccupation(No)
                                  .ordersForcedMarriageProtection(No)
                                  .ordersRestraining(No)
                                  .ordersOtherInjunctive(No)
                                  .ordersUndertakingInPlace(No).build())
            .build();

        AllegationOfHarm allegationOfHarm = caseData.getAllegationOfHarm();
        Optional<YesOrNo> ordersNonMolestation = ofNullable(allegationOfHarm.getOrdersNonMolestation());
        Optional<YesOrNo> ordersOccupation = ofNullable(allegationOfHarm.getOrdersOccupation());
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(allegationOfHarm.getOrdersForcedMarriageProtection());
        Optional<YesOrNo> ordersRestraining = ofNullable(allegationOfHarm.getOrdersRestraining());
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(allegationOfHarm.getOrdersOtherInjunctive());
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(allegationOfHarm.getOrdersUndertakingInPlace());

        assertTrue(allegationsOfHarmChecker.isPreviousOrdersFinished(
            ordersNonMolestation,
            ordersOccupation,
            ordersForcedMarriageProtection,
            ordersRestraining,
            ordersOtherInjunctive,
            ordersUndertakingInPlace
        ));
    }

    @Test
    public void whenAllDataPresentIsPreviousOrdersFinishedReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .ordersNonMolestation(No)
                                  .ordersOccupation(No)
                                  .ordersForcedMarriageProtection(No)
                                  .ordersRestraining(No)
                                  .ordersOtherInjunctive(No)
                                  .ordersUndertakingInPlace(No).build())

            .build();
        AllegationOfHarm allegationOfHarm = caseData.getAllegationOfHarm();
        Optional<YesOrNo> ordersNonMolestation = ofNullable(allegationOfHarm.getOrdersNonMolestation());
        Optional<YesOrNo> ordersOccupation = ofNullable(allegationOfHarm.getOrdersOccupation());
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(allegationOfHarm.getOrdersForcedMarriageProtection());
        Optional<YesOrNo> ordersRestraining = ofNullable(allegationOfHarm.getOrdersRestraining());
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(allegationOfHarm.getOrdersOtherInjunctive());
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(allegationOfHarm.getOrdersUndertakingInPlace());

        assertTrue(allegationsOfHarmChecker.isPreviousOrdersFinished(
            ordersNonMolestation,
            ordersOccupation,
            ordersForcedMarriageProtection,
            ordersRestraining,
            ordersOtherInjunctive,
            ordersUndertakingInPlace
        ));
    }

    @Test
    public void whenPartialDataPresentAsYesIsPreviousOrdersFinishedReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .ordersNonMolestation(Yes)
                                  .ordersRestraining(Yes)
                                  .ordersOtherInjunctive(Yes)
                                  .ordersUndertakingInPlace(Yes).build())
            .build();
        AllegationOfHarm allegationOfHarm = caseData.getAllegationOfHarm();
        Optional<YesOrNo> ordersNonMolestation = ofNullable(allegationOfHarm.getOrdersNonMolestation());
        Optional<YesOrNo> ordersOccupation = ofNullable(allegationOfHarm.getOrdersOccupation());
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(allegationOfHarm.getOrdersForcedMarriageProtection());
        Optional<YesOrNo> ordersRestraining = ofNullable(allegationOfHarm.getOrdersRestraining());
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(allegationOfHarm.getOrdersOtherInjunctive());
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(allegationOfHarm.getOrdersUndertakingInPlace());

        assertFalse(allegationsOfHarmChecker.isPreviousOrdersFinished(
            ordersNonMolestation,
            ordersOccupation,
            ordersForcedMarriageProtection,
            ordersRestraining,
            ordersOtherInjunctive,
            ordersUndertakingInPlace
        ));
    }


    @Test
    public void whenAllCaseDataPresentValidateFieldsReturnTrue() {

        Behaviours behaviour1 = Behaviours.builder()
            .abuseNatureDescription("Test1")
            .behavioursStartDateAndLength("5 days")
            .behavioursNature("Testing1")
            .behavioursApplicantSoughtHelp(Yes)
            .behavioursApplicantHelpSoughtWho("Who from1")
            .behavioursApplicantHelpAction("Action1")
            .build();
        Behaviours behaviour2 = Behaviours.builder()
                .abuseNatureDescription("Test2")
                .behavioursStartDateAndLength("5 days")
                .behavioursNature("Testing2")
                .behavioursApplicantSoughtHelp(Yes)
                .behavioursApplicantHelpSoughtWho("Who from2")
                .behavioursApplicantHelpAction("Action2")
                .build();

        Element<Behaviours> wrappedBehaviour1 = Element.<Behaviours>builder()
            .value(behaviour1)
            .build();
        Element<Behaviours> wrappedBehaviour2 = Element.<Behaviours>builder()
                .value(behaviour2)
                .build();

        CaseData caseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes)
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .behaviours(List.of(wrappedBehaviour1,wrappedBehaviour2))
                                  .physicalAbuseVictim(Collections.singletonList(applicants))
                                  .ordersNonMolestation(No)
                                  .ordersOccupation(No)
                                  .ordersForcedMarriageProtection(No)
                                  .ordersRestraining(No)
                                  .ordersOtherInjunctive(No)
                                  .ordersUndertakingInPlace(No)
                                  .allegationsOfHarmChildAbductionYesNo(No)
                                  .allegationsOfHarmOtherConcernsYesNo(No)
                                  .allegationsOfHarmOtherConcernsCourtActions("testing")
                                  .allegationsOfHarmOtherConcerns(No)
                                  .agreeChildUnsupervisedTime(No)
                                  .agreeChildSupervisedTime(No)
                                  .agreeChildOtherContact(No).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateFields(caseData));

    }

    @Test
    public void whenAllCaseDataPresentValidateFieldsReturnFalse() {

        Behaviours behaviour1 = Behaviours.builder()
                .abuseNatureDescription("Test1")
                .behavioursStartDateAndLength("5 days")
                .behavioursNature("")
                .behavioursApplicantSoughtHelp(Yes)
                .behavioursApplicantHelpSoughtWho("Who from1")
                .behavioursApplicantHelpAction("Action1")
                .build();
        Behaviours behaviour2 = Behaviours.builder()
                .abuseNatureDescription("Test2")
                .behavioursStartDateAndLength("5 days")
                .behavioursNature("Testing2")
                .behavioursApplicantSoughtHelp(Yes)
                .behavioursApplicantHelpSoughtWho("Who from2")
                .behavioursApplicantHelpAction("Action2")
                .build();

        Element<Behaviours> wrappedBehaviour1 = Element.<Behaviours>builder()
                .value(behaviour1)
                .build();
        Element<Behaviours> wrappedBehaviour2 = Element.<Behaviours>builder()
                .value(behaviour2)
                .build();

        CaseData caseData = CaseData.builder()
                .allegationOfHarm(AllegationOfHarm.builder()
                        .allegationsOfHarmYesNo(Yes)
                        .allegationsOfHarmDomesticAbuseYesNo(Yes)
                        .behaviours(List.of(wrappedBehaviour1,wrappedBehaviour2))
                        .physicalAbuseVictim(Collections.singletonList(applicants))
                        .ordersNonMolestation(No)
                        .ordersOccupation(No)
                        .ordersForcedMarriageProtection(No)
                        .ordersRestraining(No)
                        .ordersOtherInjunctive(No)
                        .ordersUndertakingInPlace(No)
                        .allegationsOfHarmChildAbductionYesNo(No)
                        .allegationsOfHarmOtherConcernsYesNo(No)
                        .allegationsOfHarmOtherConcernsCourtActions("testing")
                        .allegationsOfHarmOtherConcerns(No)
                        .agreeChildUnsupervisedTime(No)
                        .agreeChildSupervisedTime(No)
                        .agreeChildOtherContact(No).build())
                .build();

        assertFalse(allegationsOfHarmChecker.validateFields(caseData));

    }

    @Test
    public void whenNoCaseDataPresentThenDefaultTaskStateReturnsNotNull() {
        assertNotNull(allegationsOfHarmChecker.getDefaultTaskState(CaseData.builder().build()));
    }
}
