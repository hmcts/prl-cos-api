package uk.gov.hmcts.reform.prl.services.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.RespPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.RespChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.RespDomesticAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespChildPassportDetails;
import uk.gov.hmcts.reform.prl.services.RespondentAllegationOfHarmService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentTaskErrorService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators.RespondentAllegationsOfHarmChecker;

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
public class RespondentAllegationsOfHarmCheckerTest {
    @Mock
    RespondentTaskErrorService respondentTaskErrorService;

    @Mock
    RespondentAllegationOfHarmService respondentAllegationOfHarmService;

    @InjectMocks
    RespondentAllegationsOfHarmChecker respondentAllegationsOfHarmChecker;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void whenNoCaseDataThenIsStartedIsFalse() {
        PartyDetails partyDetails = PartyDetails.builder()
                .response(Response.builder()
                        .respondentAllegationsOfHarmData(RespondentAllegationsOfHarmData.builder()
                                .build()).build())
                .build();

        assertFalse(respondentAllegationsOfHarmChecker.isStarted(partyDetails, true));
    }

    @Test
    public void whenPartialCaseDataThenIsStartedTrue() {
        PartyDetails partyDetails = PartyDetails.builder()
                .response(Response.builder()
                        .respondentAllegationsOfHarmData(RespondentAllegationsOfHarmData.builder().respAohYesOrNo(Yes)
                                .build()).build())
                .build();

        assertTrue(respondentAllegationsOfHarmChecker.isStarted(partyDetails, true));
    }


    @Test
    public void whenNoCaseDataThenNotFinished() {

        PartyDetails partyDetails = PartyDetails.builder()
                .response(Response.builder()
                        .respondentAllegationsOfHarmData(RespondentAllegationsOfHarmData.builder()
                                .build()).build())
                .build();

        boolean isFinished = respondentAllegationsOfHarmChecker.isFinished(partyDetails, true);

        assertFalse(isFinished);
    }

    @Test
    public void finishedFieldsValidatedToTrue() {

        PartyDetails partyDetails = PartyDetails.builder()
                .response(Response.builder()
                        .respondentAllegationsOfHarmData(RespondentAllegationsOfHarmData.builder().respAohYesOrNo(No)
                                .build()).build())
                .build();

        boolean isFinished = respondentAllegationsOfHarmChecker.isFinished(partyDetails, true);

        assertTrue(isFinished);
    }

    @Test
    public void validateAbusePresentFalse() {
        PartyDetails partyDetails = PartyDetails.builder()
                .response(Response.builder()
                        .respondentAllegationsOfHarmData(RespondentAllegationsOfHarmData.builder()
                                .build()).build())
                .build();

        boolean isAbusePresent = respondentAllegationsOfHarmChecker.isStarted(partyDetails, true);

        assertFalse(isAbusePresent);
    }


    @Test
    public void whenNoCaseDataValidateFieldsReturnsFalse() {

        assertFalse(respondentAllegationsOfHarmChecker.validateFields(RespondentAllegationsOfHarmData.builder()
                .build()));
    }

    @Test
    public void whenNoCaseDataThenValidateOtherConcernsIsFalse() {
        assertFalse(respondentAllegationsOfHarmChecker.validateOtherConcerns(RespondentAllegationsOfHarmData.builder()
                .build()));
    }

    @Test
    public void whenOtherConcernsPresentThenValidateOtherConcernsTrue() {
        assertTrue(respondentAllegationsOfHarmChecker.validateOtherConcerns(RespondentAllegationsOfHarmData.builder()
                .respAohOtherConcerns(Yes)
                .respAohOtherConcernsDetails("Details")
                .respAohOtherConcernsCourtActions("Court actions").build()));
    }


    @Test
    public void whenNoCaseDataThenValidateSubstanceAbuseIsFalse() {
        assertFalse(respondentAllegationsOfHarmChecker.validateSubstanceAbuse(RespondentAllegationsOfHarmData
                .builder().build()));
    }

    @Test
    public void whenOtherConcernsPresentThenValidateSubstanceAbuseTrue() {

        assertTrue(respondentAllegationsOfHarmChecker.validateSubstanceAbuse(RespondentAllegationsOfHarmData.builder()
                .respAohSubstanceAbuseYesNo(Yes)
                .respAohSubstanceAbuseDetails("Details").build()));
    }

    @Test
    public void whenNoCaseDataThenValidateChildContactIsFalse() {

        assertFalse(respondentAllegationsOfHarmChecker.validateChildContact(RespondentAllegationsOfHarmData
                .builder().build()));

    }

    @Test
    public void whenChildContactPresentThenValidateChildContactTrue() {
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respAgreeChildUnsupervisedTime(Yes)
                .respAgreeChildSupervisedTime(Yes)
                .respAgreeChildOtherContact(Yes).build();

        assertTrue(respondentAllegationsOfHarmChecker.validateChildContact(respondentAllegationsOfHarmData));
    }

    @Test
    public void whenNonMolestationOrderCurrentReturnTrue() {

        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respOrdersNonMolestation(Yes)
                .respOrdersNonMolestationCurrent(Yes)
                .build();

        assertTrue(respondentAllegationsOfHarmChecker.validateNonMolestationOrder(respondentAllegationsOfHarmData));
    }

    @Test
    public void whenOccupationOrderCurrentReturnTrue() {
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respOrdersOccupation(Yes)
                .respOrdersOccupationCurrent(Yes)
                .build();

        assertTrue(respondentAllegationsOfHarmChecker.validateOccupationOrder(respondentAllegationsOfHarmData));
    }

    @Test
    public void whenForcedMarriageOrderCurrentReturnTrue() {

        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respOrdersForcedMarriageProtection(Yes)
                .respOrdersForcedMarriageProtectionCurrent(Yes)
                .build();

        assertTrue(respondentAllegationsOfHarmChecker.validateForcedMarriageProtectionOrder(respondentAllegationsOfHarmData));
    }

    @Test
    public void whenRestrainingOrderOrderCurrentReturnTrue() {

        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respOrdersRestraining(Yes)
                .respOrdersRestrainingCurrent(Yes)
                .build();

        assertTrue(respondentAllegationsOfHarmChecker.validateRestrainingOrder(respondentAllegationsOfHarmData));
    }

    @Test
    public void whenOtherInjunctiveOrderCurrentReturnTrue() {

        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respOrdersOtherInjunctive(Yes)
                .respOrdersOtherInjunctiveCurrent(Yes)
                .build();

        assertTrue(respondentAllegationsOfHarmChecker.validateOtherInjunctiveOrder(respondentAllegationsOfHarmData));
    }

    @Test
    public void whenUndertakingOrderCurrentReturnTrue() {

        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respOrdersUndertakingInPlace(Yes)
                .respOrdersUndertakingInPlaceCurrent(Yes)
                .build();

        assertTrue(respondentAllegationsOfHarmChecker.validateUndertakingInPlaceOrder(respondentAllegationsOfHarmData));
    }

    @Test
    public void whenNoCaseDataThenAbductionSectionNotComplete() {

        assertFalse(respondentAllegationsOfHarmChecker.validateAbductionSection(RespondentAllegationsOfHarmData.builder()
                .build()));
    }

    @Test
    public void whenCaseDataPresentThenAbductionSectionReturnTrue() {

        List<RespPassportPossessionEnum> abductionChildPassportPosessionList =
                new ArrayList<>();
        abductionChildPassportPosessionList.add(RespPassportPossessionEnum.mother);
        RespChildPassportDetails childPassportDetails = RespChildPassportDetails.builder()
                .respChildPassportPossession(abductionChildPassportPosessionList)
                .respChildHasMultiplePassports(Yes)
                .respChildPassportPossessionOtherDetails("Test")
                .build();
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respAohChildAbductionYesNo(Yes)
                .respChildAbductionReasons("testing")
                .respPreviousAbductionThreats(Yes)
                .respPreviousAbductionThreatsDetails("Details")
                .respAbductionPassportOfficeNotified(No)
                .respAbductionChildHasPassport(Yes)
                .respChildPassportDetails(childPassportDetails)
                .respAbductionPreviousPoliceInvolvement(Yes)
                .respAbductionPreviousPoliceInvolvementDetails("Test")
                .respAbductionPreviousPoliceInvolvementDetails("Details")
                .build();

        assertTrue(respondentAllegationsOfHarmChecker.validateAbductionSection(respondentAllegationsOfHarmData));
    }


    @Test
    public void whenCaseDataPresentThenAbductionSectionReturnFalse() {

        List<RespPassportPossessionEnum> abductionChildPassportPosessionList =
                new ArrayList<>();
        abductionChildPassportPosessionList.add(RespPassportPossessionEnum.mother);
        RespChildPassportDetails childPassportDetails = RespChildPassportDetails.builder()
                .respChildPassportPossession(abductionChildPassportPosessionList)
                .respChildHasMultiplePassports(Yes)
                .respChildPassportPossessionOtherDetails("Test")
                .build();
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respAohChildAbductionYesNo(Yes)
                .respChildAbductionReasons("testing")
                .respPreviousAbductionThreats(Yes)
                .respPreviousAbductionThreatsDetails("Details")
                .respAbductionPassportOfficeNotified(No)
                .respAbductionChildHasPassport(Yes)
                .respChildPassportDetails(childPassportDetails)
                .respAbductionPreviousPoliceInvolvement(Yes)
                .respAbductionPreviousPoliceInvolvementDetails("Details")
                .build();

        assertTrue(respondentAllegationsOfHarmChecker.validateAbductionSection(respondentAllegationsOfHarmData));
    }


    @Test
    public void whenCaseDataPresentThenAbductionSectionReturnTrueWithNoPassport() {
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respAohChildAbductionYesNo(Yes)
                .respChildAbductionReasons("testing")
                .respPreviousAbductionThreats(Yes)
                .respPreviousAbductionThreatsDetails("Details")
                .respAbductionPassportOfficeNotified(No)
                .respAbductionChildHasPassport(No)
                .respAbductionPreviousPoliceInvolvement(Yes)
                .respAbductionPreviousPoliceInvolvementDetails("Test")
                .respAbductionPreviousPoliceInvolvementDetails("Details")
                .build();

        assertTrue(respondentAllegationsOfHarmChecker.validateAbductionSection(respondentAllegationsOfHarmData));
    }


    @Test
    public void whenDomesticBehaviourPresentButIncompleteReturnTure() {

        RespDomesticAbuseBehaviours behaviour = RespDomesticAbuseBehaviours.builder()
                .respTypeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
                .respAbuseNatureDescription("Test")
                .respBehavioursStartDateAndLength("5 days")
                .respBehavioursApplicantSoughtHelp(Yes)
                .respBehavioursApplicantHelpSoughtWho("Who from")
                .build();

        assertTrue(respondentAllegationsOfHarmChecker.validateDomesticAbuseBehaviours(behaviour));
    }


    @Test
    public void whenDomesticBehaviourPresentButIncompleteReturnFalse() {

        RespDomesticAbuseBehaviours behaviour = RespDomesticAbuseBehaviours.builder()
                .respTypeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
                .respBehavioursStartDateAndLength("5 days")
                .respBehavioursApplicantSoughtHelp(Yes)
                .respBehavioursApplicantHelpSoughtWho("Who from")
                .build();

        assertFalse(respondentAllegationsOfHarmChecker.validateDomesticAbuseBehaviours(behaviour));
    }


    @Test
    public void whenChildBehaviourPresentButIncompleteReturnTure() {

        RespDomesticAbuseBehaviours behaviour = RespDomesticAbuseBehaviours.builder()
                .respTypeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
                .respAbuseNatureDescription("Test")
                .respBehavioursStartDateAndLength("5 days")
                .respBehavioursApplicantSoughtHelp(Yes)
                .respBehavioursApplicantHelpSoughtWho("Who from")
                .build();

        assertTrue(respondentAllegationsOfHarmChecker.validateDomesticAbuseBehaviours(behaviour));
    }


    @Test
    public void whenChildBehaviourPresentButIncompleteReturnFalse() {

        RespDomesticAbuseBehaviours behaviour = RespDomesticAbuseBehaviours.builder()
                .respTypeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
                .respBehavioursStartDateAndLength("5 days")
                .respBehavioursApplicantSoughtHelp(Yes)
                .respBehavioursApplicantHelpSoughtWho("Who from")
                .build();

        assertFalse(respondentAllegationsOfHarmChecker.validateDomesticAbuseBehaviours(behaviour));
    }


    @Test
    public void whenAbuseInCompleteReturnFalse() {

        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respAohDomesticAbuseYesNo(Yes)
                .respAohChildAbductionYesNo(Yes)
                .respChildAbductionReasons("harm")
                .respPreviousAbductionThreats(Yes)
                .respAbductionChildHasPassport(No)
                .respPreviousAbductionThreatsDetails("none").build();

        assertFalse(respondentAllegationsOfHarmChecker.validateAbductionSection(respondentAllegationsOfHarmData));
    }

    @Test
    public void whenOrderPresentButIncompleteReturnsFalse() {
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respOrdersRestraining(Yes)
                .respOrdersRestrainingCourtName("Test Court Name").build();

        assertFalse(respondentAllegationsOfHarmChecker.validateOrders(respondentAllegationsOfHarmData));
    }

    @Test
    public void whenOrderPresentAndCompleteMandatoryDataReturnTrue() {
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respOrdersOtherInjunctiveCurrent(Yes)
                .respOrdersOtherInjunctiveCurrent(No).build();
        assertTrue(respondentAllegationsOfHarmChecker.validateOrders(respondentAllegationsOfHarmData));
    }

    @Test
    public void whenNoDataIsSectionsFinishedReturnFalse() {
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder().build();
        assertFalse(respondentAllegationsOfHarmChecker.isSectionsFinished(respondentAllegationsOfHarmData, false));
    }

    @Test
    public void whenIsSectionsFinishedTrue() {
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder().build();

        assertFalse(respondentAllegationsOfHarmChecker.isSectionsFinished(respondentAllegationsOfHarmData, true));
    }

    @Test
    public void whenNoDataIsPreviousOrdersFinishedReturnFalse() {
        RespondentAllegationsOfHarmData allegationOfHarm = RespondentAllegationsOfHarmData.builder().build();
        Optional<YesOrNo> ordersNonMolestation = ofNullable(allegationOfHarm.getRespOrdersNonMolestation());
        Optional<YesOrNo> ordersOccupation = ofNullable(allegationOfHarm.getRespOrdersOccupation());
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(allegationOfHarm.getRespOrdersForcedMarriageProtection());
        Optional<YesOrNo> ordersRestraining = ofNullable(allegationOfHarm.getRespOrdersRestraining());
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(allegationOfHarm.getRespOrdersOtherInjunctive());
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(allegationOfHarm.getRespOrdersUndertakingInPlace());

        assertFalse(respondentAllegationsOfHarmChecker.isPreviousOrdersFinished(
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
        RespondentAllegationsOfHarmData allegationOfHarm = RespondentAllegationsOfHarmData.builder().respOrdersNonMolestation(No)
                .respOrdersOccupation(No)
                .respOrdersForcedMarriageProtection(No)
                .respOrdersRestraining(No)
                .respOrdersOtherInjunctive(No)
                .respOrdersUndertakingInPlace(No).build();

        Optional<YesOrNo> ordersNonMolestation = ofNullable(allegationOfHarm.getRespOrdersNonMolestation());
        Optional<YesOrNo> ordersOccupation = ofNullable(allegationOfHarm.getRespOrdersOccupation());
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(allegationOfHarm.getRespOrdersForcedMarriageProtection());
        Optional<YesOrNo> ordersRestraining = ofNullable(allegationOfHarm.getRespOrdersRestraining());
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(allegationOfHarm.getRespOrdersOtherInjunctive());
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(allegationOfHarm.getRespOrdersUndertakingInPlace());

        assertTrue(respondentAllegationsOfHarmChecker.isPreviousOrdersFinished(
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
        RespondentAllegationsOfHarmData allegationOfHarm = RespondentAllegationsOfHarmData.builder()
                .respOrdersNonMolestation(No)
                .respOrdersOccupation(No)
                .respOrdersForcedMarriageProtection(No)
                .respOrdersRestraining(No)
                .respOrdersOtherInjunctive(No)
                .respOrdersUndertakingInPlace(No).build();
        Optional<YesOrNo> ordersNonMolestation = ofNullable(allegationOfHarm.getRespOrdersNonMolestation());
        Optional<YesOrNo> ordersOccupation = ofNullable(allegationOfHarm.getRespOrdersOccupation());
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(allegationOfHarm.getRespOrdersForcedMarriageProtection());
        Optional<YesOrNo> ordersRestraining = ofNullable(allegationOfHarm.getRespOrdersRestraining());
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(allegationOfHarm.getRespOrdersOtherInjunctive());
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(allegationOfHarm.getRespOrdersUndertakingInPlace());

        assertTrue(respondentAllegationsOfHarmChecker.isPreviousOrdersFinished(
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
        RespondentAllegationsOfHarmData allegationOfHarm = RespondentAllegationsOfHarmData.builder()
                .respOrdersNonMolestation(Yes)
                .respOrdersRestraining(Yes)
                .respOrdersOtherInjunctive(Yes)
                .respOrdersUndertakingInPlace(Yes).build();
        Optional<YesOrNo> ordersNonMolestation = ofNullable(allegationOfHarm.getRespOrdersNonMolestation());
        Optional<YesOrNo> ordersOccupation = ofNullable(allegationOfHarm.getRespOrdersOccupation());
        Optional<YesOrNo> ordersForcedMarriageProtection = ofNullable(allegationOfHarm.getRespOrdersForcedMarriageProtection());
        Optional<YesOrNo> ordersRestraining = ofNullable(allegationOfHarm.getRespOrdersRestraining());
        Optional<YesOrNo> ordersOtherInjunctive = ofNullable(allegationOfHarm.getRespOrdersOtherInjunctive());
        Optional<YesOrNo> ordersUndertakingInPlace = ofNullable(allegationOfHarm.getRespOrdersUndertakingInPlace());

        assertFalse(respondentAllegationsOfHarmChecker.isPreviousOrdersFinished(
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

        RespDomesticAbuseBehaviours domesticAbuseBehaviours = RespDomesticAbuseBehaviours.builder()
                .respTypeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
                .respAbuseNatureDescription("Test")
                .respBehavioursStartDateAndLength("5 days")
                .respBehavioursApplicantSoughtHelp(Yes)
                .respBehavioursApplicantHelpSoughtWho("Who from")
                .build();

        Element<RespDomesticAbuseBehaviours> domesticAbuseBehavioursElement = Element.<RespDomesticAbuseBehaviours>builder()
                .value(domesticAbuseBehaviours)
                .build();

        RespChildAbuse childPhysicalAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription("test")
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();

        RespChildAbuse childPsychologicalAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription("test")
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();

        RespChildAbuse childSexualAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription("test")
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();

        RespChildAbuse childEmotionalAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription("test")
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();

        RespChildAbuse childFinancialAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription("test")
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();


        RespondentAllegationsOfHarmData allegationOfHarm = RespondentAllegationsOfHarmData.builder()
                .respAohYesOrNo(Yes)
                .respAohDomesticAbuseYesNo(Yes)
                .respDomesticBehaviours(Collections.singletonList(domesticAbuseBehavioursElement))
                .respOrdersNonMolestation(No)
                .respOrdersOccupation(No)
                .respOrdersForcedMarriageProtection(No)
                .respOrdersRestraining(No)
                .respOrdersOtherInjunctive(No)
                .respOrdersUndertakingInPlace(No)
                .respAohChildAbductionYesNo(No)
                .respAohOtherConcerns(Yes)
                .respAohOtherConcernsDetails("Details")
                .respAohOtherConcernsCourtActions("testing")
                .respAohSubstanceAbuseYesNo(Yes)
                .respAohSubstanceAbuseDetails("Details")
                .respAohOtherConcerns(No)
                .respAgreeChildUnsupervisedTime(No)
                .respAgreeChildSupervisedTime(No)
                .respAgreeChildOtherContact(No)
                .respChildPhysicalAbuse(childPhysicalAbuse)
                .respChildFinancialAbuse(childFinancialAbuse)
                .respChildEmotionalAbuse(childEmotionalAbuse)
                .respChildPsychologicalAbuse(childPsychologicalAbuse)
                .respChildSexualAbuse(childSexualAbuse)
                .build();

        assertTrue(respondentAllegationsOfHarmChecker.validateFields(allegationOfHarm));

    }

    @Test
    public void testValidateFieldsReturnFalseForPhysicalAbuse() {
        RespChildAbuse childAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription(null)
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();

        RespondentAllegationsOfHarmData allegationOfHarm = RespondentAllegationsOfHarmData.builder()
                .respAohYesOrNo(Yes)
                .respAohChildAbuseYesNo(Yes).respChildPhysicalAbuse(childAbuse)
                .build();

        assertFalse(respondentAllegationsOfHarmChecker.validateFields(allegationOfHarm));
    }

    @Test
    public void testValidateFieldsReturnFalseForPhsychologicalAbuse() {

        RespChildAbuse childAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription(null)
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();

        RespondentAllegationsOfHarmData allegationOfHarm = RespondentAllegationsOfHarmData.builder()
                .respAohYesOrNo(Yes)
                .respAohChildAbuseYesNo(Yes).respChildPsychologicalAbuse(childAbuse)
                .build();

        assertFalse(respondentAllegationsOfHarmChecker.validateFields(allegationOfHarm));
    }

    @Test
    public void testValidateFieldsReturnFalseForSexualAbuse() {

        RespChildAbuse childAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription(null)
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();

        RespondentAllegationsOfHarmData allegationOfHarm = RespondentAllegationsOfHarmData.builder()
                .respAohYesOrNo(Yes)
                .respAohChildAbuseYesNo(Yes).respChildSexualAbuse(childAbuse)
                .build();

        assertFalse(respondentAllegationsOfHarmChecker.validateFields(allegationOfHarm));
    }

    @Test
    public void testValidateFieldsReturnFalseForEmotionalAbuse() {

        RespChildAbuse childAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription(null)
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();

        RespondentAllegationsOfHarmData allegationOfHarm = RespondentAllegationsOfHarmData.builder()
                .respAohYesOrNo(Yes)
                .respAohChildAbuseYesNo(Yes).respChildEmotionalAbuse(childAbuse)
                .build();

        assertFalse(respondentAllegationsOfHarmChecker.validateFields(allegationOfHarm));
    }


    @Test
    public void testValidateFieldsReturnFalseForFinancialAbuse() {

        RespChildAbuse childAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription(null)
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();

        RespondentAllegationsOfHarmData allegationOfHarm = RespondentAllegationsOfHarmData.builder()
                .respAohYesOrNo(Yes)
                .respAohChildAbuseYesNo(Yes).respChildFinancialAbuse(childAbuse)
                .build();

        assertFalse(respondentAllegationsOfHarmChecker.validateFields(allegationOfHarm));
    }

    @Test
    public void testValidateChildAbuseBehaviours() {
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respAllChildrenAreRiskPhysicalAbuse(No)
                .respAllChildrenAreRiskPsychologicalAbuse(No)
                .respAllChildrenAreRiskSexualAbuse(No)
                .respAllChildrenAreRiskEmotionalAbuse(No)
                .respAllChildrenAreRiskFinancialAbuse(No)
                .respWhichChildrenAreRiskPhysicalAbuse(DynamicMultiSelectList
                        .builder().value(List.of(DynamicMultiselectListElement
                                .builder().code("test").build())).build())
                .respWhichChildrenAreRiskPsychologicalAbuse(DynamicMultiSelectList
                        .builder().value(List.of(DynamicMultiselectListElement
                                .builder().code("test").build())).build())
                .respWhichChildrenAreRiskEmotionalAbuse(DynamicMultiSelectList
                        .builder().value(List.of(DynamicMultiselectListElement
                                .builder().code("test").build())).build())
                .respWhichChildrenAreRiskFinancialAbuse(DynamicMultiSelectList
                        .builder().value(List.of(DynamicMultiselectListElement
                                .builder().code("test").build())).build())
                .respWhichChildrenAreRiskSexualAbuse(DynamicMultiSelectList
                        .builder().value(List.of(DynamicMultiselectListElement
                                .builder().code("test").build())).build())
                .build();
        RespChildAbuse childAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription("test")
                .respBehavioursApplicantHelpSoughtWho("test")
                .respBehavioursApplicantSoughtHelp(Yes)
                .respBehavioursStartDateAndLength("test")
                .build();

        assertTrue(respondentAllegationsOfHarmChecker.validateChildAbuseBehaviours(respondentAllegationsOfHarmData,
                childAbuse,ChildAbuseEnum.physicalAbuse));

    }

}
