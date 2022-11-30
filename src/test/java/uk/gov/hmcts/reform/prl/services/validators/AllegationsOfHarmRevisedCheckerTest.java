package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.AbductionChildPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildPassportDetails;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class AllegationsOfHarmRevisedCheckerTest {
    @Mock
    TaskErrorService taskErrorService;

    @InjectMocks
    AllegationsOfHarmRevisedChecker allegationsOfHarmChecker;

    @Test
    public void whenNoCaseDataThenIsStartedIsFalse() {
        CaseData casedata = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder().build())
            .build();

        assertFalse(allegationsOfHarmChecker.isStarted(casedata));
    }

    @Test
    public void whenPartialCaseDataThenIsStartedTrue() {
        CaseData casedata = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newAllegationsOfHarmYesNo(Yes).build())
            .build();

        assertTrue(allegationsOfHarmChecker.isStarted(casedata));
    }

    @Test
    public void whenAllegationOfHarmSelectedNoIsStartedFalse() {
        CaseData casedata = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                         .newAllegationsOfHarmYesNo(No)
                                  .build())
            .build();

        assertFalse(allegationsOfHarmChecker.isStarted(casedata));
    }


    @Test
    public void whenNoCaseDataThenNotFinished() {

        CaseData casedata = CaseData.builder().allegationOfHarmRevised(AllegationOfHarmRevised.builder().build())
            .build();

        boolean isFinished = allegationsOfHarmChecker.isFinished(casedata);

        assertFalse(isFinished);
    }

    @Test
    public void finishedFieldsValidatedToTrue() {

        CaseData casedata = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newAllegationsOfHarmYesNo(No).build())
            .build();

        boolean isFinished = allegationsOfHarmChecker.isFinished(casedata);

        assertTrue(isFinished);
    }

    @Test
    public void validateAbusePresentFalse() {
        CaseData casedata = CaseData.builder().allegationOfHarmRevised(AllegationOfHarmRevised.builder().build())
            .build();

        boolean isAbusePresent = allegationsOfHarmChecker.isStarted(casedata);

        assertFalse(isAbusePresent);
    }

    @Test
    public void whenNoCaseDataThenHasMandatoryFalse() {

        CaseData casedata = CaseData.builder().allegationOfHarmRevised(AllegationOfHarmRevised.builder().build())
            .build();

        assertFalse(allegationsOfHarmChecker.hasMandatoryCompleted(casedata));

    }

    @Test
    public void whenFinishedCaseDataThenHasMandatoryFalse() {

        CaseData casedata = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newAllegationsOfHarmYesNo(No).build())
            .build();

        assertFalse(allegationsOfHarmChecker.hasMandatoryCompleted(casedata));

    }

    @Test
    public void whenNoCaseDataValidateFieldsReturnsFalse() {
        CaseData caseData = CaseData.builder().allegationOfHarmRevised(AllegationOfHarmRevised.builder().build())
            .build();

        assertFalse(allegationsOfHarmChecker.validateFields(caseData));
    }

    @Test
    public void whenNoCaseDataThenValidateOtherConcernsIsFalse() {
        CaseData caseData = CaseData.builder().allegationOfHarmRevised(AllegationOfHarmRevised.builder().build())
            .build();

        assertFalse(allegationsOfHarmChecker.validateOtherConcerns(caseData));
    }

    @Test
    public void whenOtherConcernsPresentThenValidateOtherConcernsTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newAllegationsOfHarmOtherConcerns(Yes)
                                  .newAllegationsOfHarmOtherConcernsDetails("Details")
                                  .newAllegationsOfHarmOtherConcernsCourtActions("Court actions").build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateOtherConcerns(caseData));
    }

    @Test
    public void whenNoCaseDataThenValidateChildContactIsFalse() {
        CaseData caseData = CaseData.builder().allegationOfHarmRevised(AllegationOfHarmRevised.builder().build())
            .build();


        assertFalse(allegationsOfHarmChecker.validateChildContact(caseData));

    }

    @Test
    public void whenChildContactPresentThenValidateChildContactTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newAgreeChildUnsupervisedTime(Yes)
                                  .newAgreeChildSupervisedTime(Yes)
                                  .newAgreeChildOtherContact(Yes).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateChildContact(caseData));
    }

    @Test
    public void whenNonMolestationOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newOrdersNonMolestation(Yes)
                                  .newOrdersNonMolestationCurrent(Yes).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateNonMolestationOrder(caseData));
    }

    @Test
    public void whenOccupationOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newOrdersOccupation(Yes)
                                  .newOrdersOccupationCurrent(Yes).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateOccupationOrder(caseData));
    }

    @Test
    public void whenForcedMarriageOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newOrdersForcedMarriageProtection(Yes)
                                  .newOrdersForcedMarriageProtectionCurrent(Yes).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateForcedMarriageProtectionOrder(caseData));
    }

    @Test
    public void whenRestrainingOrderOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newOrdersRestraining(Yes)
                                  .newOrdersRestrainingCurrent(Yes).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateRestrainingOrder(caseData));
    }

    @Test
    public void whenOtherInjunctiveOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newOrdersOtherInjunctive(Yes)
                                  .newOrdersOtherInjunctiveCurrent(Yes).build())

            .build();

        assertTrue(allegationsOfHarmChecker.validateOtherInjunctiveOrder(caseData));
    }

    @Test
    public void whenUndertakingOrderCurrentReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newOrdersUndertakingInPlace(Yes)
                                  .newOrdersUndertakingInPlaceCurrent(Yes).build())

            .build();

        assertTrue(allegationsOfHarmChecker.validateUndertakingInPlaceOrder(caseData));
    }

    @Test
    public void whenNoCaseDataThenAbductionSectionNotComplete() {

        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .build())
            .build();

        assertFalse(allegationsOfHarmChecker.validateAbductionSection(caseData));
    }

    @Test
    public void whenCaseDataPresentThenAbductionSectionReturnTrue() {

        List<AbductionChildPassportPossessionEnum> abductionChildPassportPosessionList =
            new ArrayList<>();
        abductionChildPassportPosessionList.add(AbductionChildPassportPossessionEnum.mother);
        ChildPassportDetails childPassportDetails = ChildPassportDetails.builder()
            .newChildPassportPossession(abductionChildPassportPosessionList)
            .newChildHasMultiplePassports(Yes)
            .newChildPassportPossessionOtherDetails("Test")
            .build();
        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newAllegationsOfHarmChildAbductionYesNo(Yes)
                                  .newChildAbductionReasons("testing")
                                  .newPreviousAbductionThreats(Yes)
                                  .newPreviousAbductionThreatsDetails("Details")
                                  .newAbductionPassportOfficeNotified(No)
                                  .newAbductionChildHasPassport(Yes)
                                         .childPassportDetails(childPassportDetails)
                                  .newAbductionPreviousPoliceInvolvement(No)
                                  .newAbductionPreviousPoliceInvolvementDetails("Details")
                                  .build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateAbductionSection(caseData));
    }


    @Test
    public void whenDomesticBehaviourPresentButIncompleteReturnTure() {

        DomesticAbuseBehaviours behaviour = DomesticAbuseBehaviours.builder()
            .typeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
            .newAbuseNatureDescription("Test")
            .newBehavioursStartDateAndLength("5 days")
            .newBehavioursApplicantSoughtHelp(Yes)
            .newBehavioursApplicantHelpSoughtWho("Who from")
            .build();

        assertTrue(allegationsOfHarmChecker.validateDomesticAbuseBehaviours(behaviour));
    }


    @Test
    public void whenDomesticBehaviourPresentButIncompleteReturnFalse() {

        DomesticAbuseBehaviours behaviour = DomesticAbuseBehaviours.builder()
            .typeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
            .newBehavioursStartDateAndLength("5 days")
            .newBehavioursApplicantSoughtHelp(Yes)
            .newBehavioursApplicantHelpSoughtWho("Who from")
            .build();

        assertFalse(allegationsOfHarmChecker.validateDomesticAbuseBehaviours(behaviour));
    }



    @Test
    public void whenChildBehaviourPresentButIncompleteReturnTure() {

        DomesticAbuseBehaviours behaviour = DomesticAbuseBehaviours.builder()
            .typeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
            .newAbuseNatureDescription("Test")
            .newBehavioursStartDateAndLength("5 days")
            .newBehavioursApplicantSoughtHelp(Yes)
            .newBehavioursApplicantHelpSoughtWho("Who from")
            .build();

        assertTrue(allegationsOfHarmChecker.validateDomesticAbuseBehaviours(behaviour));
    }


    @Test
    public void whenChildBehaviourPresentButIncompleteReturnFalse() {

        DomesticAbuseBehaviours behaviour = DomesticAbuseBehaviours.builder()
            .typeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
            .newBehavioursStartDateAndLength("5 days")
            .newBehavioursApplicantSoughtHelp(Yes)
            .newBehavioursApplicantHelpSoughtWho("Who from")
            .build();

        assertFalse(allegationsOfHarmChecker.validateDomesticAbuseBehaviours(behaviour));
    }



    @Test
    public void whenCompleteBehaviourReturnTrue() {

        DomesticAbuseBehaviours behaviour = DomesticAbuseBehaviours.builder()
            .typeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
            .newAbuseNatureDescription("Test")
            .newBehavioursStartDateAndLength("5 days")
            .newBehavioursApplicantSoughtHelp(Yes)
            .newBehavioursApplicantHelpSoughtWho("Who from")
            .build();

        assertTrue(allegationsOfHarmChecker.validateDomesticAbuseBehaviours(behaviour));
    }

    @Test
    public void whenAbuseInCompleteReturnFalse() {



        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newAllegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .newAllegationsOfHarmChildAbductionYesNo(Yes)
                                  .newChildAbductionReasons("harm")
                                  .newPreviousAbductionThreats(Yes)
                                  .newAbductionChildHasPassport(No)
                                  .newPreviousAbductionThreatsDetails("none").build())
            .build();

        assertFalse(allegationsOfHarmChecker.validateAbductionSection(caseData));
    }

    @Test
    public void whenOrderPresentButIncompleteReturnsFalse() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newOrdersRestraining(Yes)
                                  .newOrdersRestrainingCourtName("Test Court Name").build())
            .build();

        assertFalse(allegationsOfHarmChecker.validateOrders(caseData));
    }

    @Test
    public void whenOrderPresentAndCompleteMandatoryDataReturnTrue() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newOrdersOtherInjunctiveCurrent(Yes)
                                  .newOrdersOtherInjunctiveCurrent(No).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateOrders(caseData));
    }

    @Test
    public void whenNoDataIsSectionsFinishedReturnFalse() {
        CaseData caseData = CaseData.builder().allegationOfHarmRevised(AllegationOfHarmRevised.builder().build())
            .build();
        assertFalse(allegationsOfHarmChecker.isSectionsFinished(caseData,false,false,false));
    }

    @Test
    public void whenNoDataIsPreviousOrdersFinishedReturnFalse() {
        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder().build())

            .build();
        AllegationOfHarmRevised allegationOfHarm = caseData.getAllegationOfHarmRevised();
        Optional<YesOrNo> ordersNonMolestation = ofNullable(allegationOfHarm.getNewOrdersNonMolestation());
        Optional<YesOrNo> ordersOccupation = ofNullable(allegationOfHarm.getNewOrdersOccupation());
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(allegationOfHarm.getNewOrdersForcedMarriageProtection());
        Optional<YesOrNo> ordersRestraining = ofNullable(allegationOfHarm.getNewOrdersRestraining());
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(allegationOfHarm.getNewOrdersOtherInjunctive());
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(allegationOfHarm.getNewOrdersUndertakingInPlace());

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
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder().newOrdersNonMolestation(No)
                                  .newOrdersOccupation(No)
                                  .newOrdersForcedMarriageProtection(No)
                                  .newOrdersRestraining(No)
                                  .newOrdersOtherInjunctive(No)
                                  .newOrdersUndertakingInPlace(No).build())
            .build();

        AllegationOfHarmRevised allegationOfHarm = caseData.getAllegationOfHarmRevised();
        Optional<YesOrNo> ordersNonMolestation = ofNullable(allegationOfHarm.getNewOrdersNonMolestation());
        Optional<YesOrNo> ordersOccupation = ofNullable(allegationOfHarm.getNewOrdersOccupation());
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(allegationOfHarm.getNewOrdersForcedMarriageProtection());
        Optional<YesOrNo> ordersRestraining = ofNullable(allegationOfHarm.getNewOrdersRestraining());
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(allegationOfHarm.getNewOrdersOtherInjunctive());
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(allegationOfHarm.getNewOrdersUndertakingInPlace());

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
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newOrdersNonMolestation(No)
                                  .newOrdersOccupation(No)
                                  .newOrdersForcedMarriageProtection(No)
                                  .newOrdersRestraining(No)
                                  .newOrdersOtherInjunctive(No)
                                  .newOrdersUndertakingInPlace(No).build())

            .build();
        AllegationOfHarmRevised allegationOfHarm = caseData.getAllegationOfHarmRevised();
        Optional<YesOrNo> ordersNonMolestation = ofNullable(allegationOfHarm.getNewOrdersNonMolestation());
        Optional<YesOrNo> ordersOccupation = ofNullable(allegationOfHarm.getNewOrdersOccupation());
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(allegationOfHarm.getNewOrdersForcedMarriageProtection());
        Optional<YesOrNo> ordersRestraining = ofNullable(allegationOfHarm.getNewOrdersRestraining());
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(allegationOfHarm.getNewOrdersOtherInjunctive());
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(allegationOfHarm.getNewOrdersUndertakingInPlace());

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
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newOrdersNonMolestation(Yes)
                                  .newOrdersRestraining(Yes)
                                  .newOrdersOtherInjunctive(Yes)
                                  .newOrdersUndertakingInPlace(Yes).build())
            .build();
        AllegationOfHarmRevised allegationOfHarm = caseData.getAllegationOfHarmRevised();
        Optional<YesOrNo> ordersNonMolestation = ofNullable(allegationOfHarm.getNewOrdersNonMolestation());
        Optional<YesOrNo> ordersOccupation = ofNullable(allegationOfHarm.getNewOrdersOccupation());
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(allegationOfHarm.getNewOrdersForcedMarriageProtection());
        Optional<YesOrNo> ordersRestraining = ofNullable(allegationOfHarm.getNewOrdersRestraining());
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(allegationOfHarm.getNewOrdersOtherInjunctive());
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(allegationOfHarm.getNewOrdersUndertakingInPlace());

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

        DomesticAbuseBehaviours domesticAbuseBehaviours = DomesticAbuseBehaviours.builder()
            .typeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
            .newAbuseNatureDescription("Test")
            .newBehavioursStartDateAndLength("5 days")
            .newBehavioursApplicantSoughtHelp(Yes)
            .newBehavioursApplicantHelpSoughtWho("Who from")
            .build();

        Element<DomesticAbuseBehaviours>  domesticAbuseBehavioursElement = Element.<DomesticAbuseBehaviours>builder()
            .value(domesticAbuseBehaviours)
            .build();
        ChildAbuseBehaviours childAbuseBehaviours = ChildAbuseBehaviours.builder()
            .typeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
            .newAbuseNatureDescription("Test")
            .newBehavioursStartDateAndLength("5 days")
            .newBehavioursApplicantSoughtHelp(Yes)
            .newBehavioursApplicantHelpSoughtWho("Who from")
            .build();

        Element<ChildAbuseBehaviours> childAbuseBehavioursElement = Element.<ChildAbuseBehaviours>builder()
            .value(childAbuseBehaviours)
            .build();

        CaseData caseData = CaseData.builder()
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                  .newAllegationsOfHarmYesNo(Yes)
                                  .newAllegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .domesticBehaviours(Collections.singletonList(domesticAbuseBehavioursElement))
                                  .childAbuseBehaviours(Collections.singletonList(childAbuseBehavioursElement))
                                  .newOrdersNonMolestation(No)
                                  .newOrdersOccupation(No)
                                  .newOrdersForcedMarriageProtection(No)
                                  .newOrdersRestraining(No)
                                  .newOrdersOtherInjunctive(No)
                                  .newOrdersUndertakingInPlace(No)
                                  .newAllegationsOfHarmChildAbductionYesNo(No)
                                  .newAllegationsOfHarmOtherConcerns(Yes)
                                  .newAllegationsOfHarmOtherConcernsDetails("Details")
                                  .newAllegationsOfHarmOtherConcernsCourtActions("testing")
                                  .newAllegationsOfHarmOtherConcerns(No)
                                  .newAgreeChildUnsupervisedTime(No)
                                  .newAgreeChildSupervisedTime(No)
                                  .newAgreeChildOtherContact(No).build())
            .build();

        assertTrue(allegationsOfHarmChecker.validateFields(caseData));

    }
}
