package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipEnum;
import uk.gov.hmcts.reform.prl.enums.ApplicantRelationshipOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.FamilyHomeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LivingSituationEnum;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MortgageNamedAfterEnum;
import uk.gov.hmcts.reform.prl.enums.NewPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.PeopleLivingAtThisAddressEnum;
import uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum;
import uk.gov.hmcts.reform.prl.enums.ProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.ReasonForOrderWithoutGivingNoticeEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoBothEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.miampolicyupgrade.MiamPolicyUpgradeChildProtectionConcernEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenLiveAtAddress;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.Landlord;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.Mortgage;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDetailsOfWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonRelationshipToChild;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChildDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ReasonForWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.RelationshipDateComplex;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBailConditionDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationDateInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationObjectType;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationOptionsInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Applicant;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.AttendingTheHearing;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.ChildDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.FL401Applicant;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Fl401OtherProceedingsDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Fl401TypeOfApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.HearingUrgency;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.InternationalElement;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.LitigationCapacity;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.MiamExemptions;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Order;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.OtherPersonInTheCase;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.OtherProceedingsDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Respondent;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.TypeOfApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.WelshLanguageRequirements;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.AllegationsOfHarmOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.AllegationsOfHarmOtherConcerns;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.AllegationsOfHarmOverview;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.ChildAbductionDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.DomesticAbuseVictim;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.AllegationsOfHarmRevisedOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.OrderRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.RevisedChildAbductionDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildPassportDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.THIS_INFORMATION_IS_CONFIDENTIAL;
import static uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingEnum.applicantStopFromRespondentEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.ApplicantStopFromRespondentDoingToChildEnum.applicantStopFromRespondentDoingToChildEnum_Value_1;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationsTabServiceTest {

    @InjectMocks
    ApplicationsTabService applicationsTabService;

    @Mock
    ApplicationsTabServiceHelper applicationsTabServiceHelper;

    @Mock
    AllegationOfHarmRevisedService allegationOfHarmRevisedService;

    @Mock
    MiamPolicyUpgradeService miamPolicyUpgradeService;

    @Mock
    ObjectMapper objectMapper;

    CaseData caseDataWithParties;
    CaseData emptyCaseData;
    Address address;
    List<Element<PartyDetails>> partyList;
    PartyDetails partyDetails;
    Order order;
    OrderRevised orderRevised;
    AllegationsOfHarmRevisedOrders allegationsOfHarmRevisedOrders;
    AllegationsOfHarmOrders allegationsOfHarmOrders;
    AllegationsOfHarmOrders emptyAllegationOfHarmOrder;

    @Before
    public void setup() {
        address = Address.builder()
            .addressLine1("55 Test Street")
            .postTown("Town")
            .postCode("N12 3BH")
            .build();

        partyDetails = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(Gender.male)
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .otherPersonRelationshipToChildren(List.of(Element.<OtherPersonRelationshipToChild>builder().value(
                OtherPersonRelationshipToChild.builder().personRelationshipToChild("Bro").build()).build()))
            .email("test@test.com")
            .build();

        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder().value(partyDetails).build();
        partyList = Collections.singletonList(partyDetailsElement);

        order = Order.builder()
            .dateIssued(LocalDate.of(1990, 8, 1))
            .endDate(LocalDate.of(1991, 8, 1))
            .orderCurrent(YesOrNo.Yes)
            .courtName("Court name")
            .build();

        allegationsOfHarmOrders = AllegationsOfHarmOrders.builder()
            .ordersNonMolestation(YesOrNo.Yes)
            .nonMolestationOrder(order)
            .ordersOccupation(YesOrNo.Yes)
            .occupationOrder(order)
            .ordersForcedMarriageProtection(YesOrNo.Yes)
            .forcedMarriageProtectionOrder(order)
            .ordersRestraining(YesOrNo.Yes)
            .restrainingOrder(order)
            .ordersOtherInjunctive(YesOrNo.Yes)
            .otherInjunctiveOrder(order)
            .ordersUndertakingInPlace(YesOrNo.Yes)
            .undertakingInPlaceOrder(order)
            .build();

        emptyAllegationOfHarmOrder = AllegationsOfHarmOrders.builder()
            .ordersNonMolestation(YesOrNo.Yes)
            .ordersOccupation(YesOrNo.Yes)
            .ordersForcedMarriageProtection(YesOrNo.Yes)
            .ordersRestraining(YesOrNo.Yes)
            .ordersOtherInjunctive(YesOrNo.Yes)
            .ordersUndertakingInPlace(YesOrNo.Yes)
            .build();

        ProceedingDetails proceedingDetails = ProceedingDetails.builder()
            .previousOrOngoingProceedings(ProceedingsEnum.previous)
            .caseNumber("12345")
            .dateStarted(LocalDate.of(1990, 8, 1))
            .dateEnded(LocalDate.of(1991, 8, 1))
            .typeOfOrder(Collections.singletonList(TypeOfOrderEnum.otherOrder))
            .otherTypeOfOrder("Test Order")
            .nameOfJudge("Test Judge")
            .nameOfCourt("Test Court")
            .nameOfChildrenInvolved("Children")
            .nameOfGuardian("Guardian")
            .build();

        Element<ProceedingDetails> proceedingDetailsElement = Element.<ProceedingDetails>builder()
            .value(proceedingDetails).build();

        caseDataWithParties = CaseData.builder()
            .applicants(partyList)
            .respondents(partyList)
            .othersToNotify(partyList)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            //type of application
            .ordersApplyingFor(Collections.singletonList(OrderTypeEnum.childArrangementsOrder))
            .typeOfChildArrangementsOrder(ChildArrangementOrderTypeEnum.spendTimeWithOrder)
            .natureOfOrder("Test nature of order")
            .applicationPermissionRequired(PermissionRequiredEnum.yes)
            .applicationPermissionRequiredReason("Some xyz reason")
            // hearing urgency
            .isCaseUrgent(Yes)
            .caseUrgencyTimeAndReason("Test String")
            .doYouRequireAHearingWithReducedNotice(No)
            //allegations of harm overview
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .allegationsOfHarmYesNo(Yes)
                                  .allegationsOfHarmDomesticAbuseYesNo(Yes)
                                  .allegationsOfHarmChildAbductionYesNo(Yes).build())
            //miam
            .miamDetails(MiamDetails.builder()
                             .applicantAttendedMiam(Yes)
                             .claimingExemptionMiam(No)
                             .familyMediatorMiam(Yes)
                             .miamExemptionsChecklist(Collections.singletonList(MiamExemptionsChecklistEnum.domesticViolence))
                             .miamDomesticViolenceChecklist(Collections.singletonList(
                                 MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_4))
                             .miamUrgencyReasonChecklist(Collections.singletonList(MiamUrgencyReasonChecklistEnum
                                                                                       .miamUrgencyReasonChecklistEnum_Value_1))
                             .miamChildProtectionConcernList(Collections.singletonList(
                                 MiamChildProtectionConcernChecklistEnum
                                     .MIAMChildProtectionConcernChecklistEnum_value_1))
                             .miamPreviousAttendanceChecklist(MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_1)
                             .miamOtherGroundsChecklist(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_2)
                             .build())

            //other proceedings
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.yes)
            .existingProceedings(Collections.singletonList(proceedingDetailsElement))
            //international element
            .habitualResidentInOtherState(Yes)
            .habitualResidentInOtherStateGiveReason("Example reason")
            .requestToForeignAuthority(No)
            //attending the hearing
            .attendHearing(AttendHearing.builder()
                               .isWelshNeeded(Yes)
                               .isDisabilityPresent(No)
                               .adjustmentsRequired("Adjustments String")
                               .build())

            //litigation capacity
            .litigationCapacityFactors("Test")
            .litigationCapacityOtherFactors(Yes)
            //allegations of harm
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .ordersNonMolestation(Yes)
                                  .ordersNonMolestationCurrent(Yes)
                                  .ordersNonMolestationDateIssued(LocalDate.of(1990, 8, 1))
                                  .ordersNonMolestationEndDate(LocalDate.of(1991, 8, 1))
                                  .ordersNonMolestationCourtName("Court name")
                                  .ordersOccupation(Yes)
                                  .ordersOccupationCurrent(Yes)
                                  .ordersOccupationDateIssued(LocalDate.of(1990, 8, 1))
                                  .ordersOccupationEndDate(LocalDate.of(1991, 8, 1))
                                  .ordersOccupationCourtName("Court name")
                                  .ordersForcedMarriageProtection(Yes)
                                  .ordersForcedMarriageProtectionCurrent(Yes)
                                  .ordersForcedMarriageProtectionDateIssued(LocalDate.of(1990, 8, 1))
                                  .ordersForcedMarriageProtectionEndDate(LocalDate.of(1991, 8, 1))
                                  .ordersForcedMarriageProtectionCourtName("Court name")
                                  .ordersRestraining(Yes)
                                  .ordersRestrainingCurrent(Yes)
                                  .ordersRestrainingDateIssued(LocalDate.of(1990, 8, 1))
                                  .ordersRestrainingEndDate(LocalDate.of(1991, 8, 1))
                                  .ordersRestrainingCourtName("Court name")
                                  .ordersOtherInjunctive(Yes)
                                  .ordersOtherInjunctiveCurrent(Yes)
                                  .ordersOtherInjunctiveDateIssued(LocalDate.of(1990, 8, 1))
                                  .ordersOtherInjunctiveEndDate(LocalDate.of(1991, 8, 1))
                                  .ordersOtherInjunctiveCourtName("Court name")
                                  .ordersUndertakingInPlace(Yes)
                                  .ordersUndertakingInPlaceCurrent(Yes)
                                  .ordersUndertakingInPlaceDateIssued(LocalDate.of(1990, 8, 1))
                                  .ordersUndertakingInPlaceEndDate(LocalDate.of(1991, 8, 1))
                                  .ordersUndertakingInPlaceCourtName("Court name")
                                  .physicalAbuseVictim(Collections.singletonList(ApplicantOrChildren.children))
                                  .emotionalAbuseVictim((Collections.singletonList(ApplicantOrChildren.children)))
                                  .psychologicalAbuseVictim((Collections.singletonList(ApplicantOrChildren.children)))
                                  .sexualAbuseVictim((Collections.singletonList(ApplicantOrChildren.children)))
                                  .financialAbuseVictim((Collections.singletonList(ApplicantOrChildren.children)))
                                  .previousAbductionThreats(Yes)
                                  .previousAbductionThreatsDetails("Details")
                                  .abductionPreviousPoliceInvolvement(No)
                                  .allegationsOfHarmOtherConcerns(Yes)
                                  .allegationsOfHarmOtherConcernsDetails("Test String")
                                  .agreeChildUnsupervisedTime(No).build())

            //welsh language requirements
            .welshLanguageRequirement(Yes)
            .languageRequirementApplicationNeedWelsh(No)
            //child details
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test string")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            //solicitor
            .solicitorName("Test Solicitor")
            .build();

        emptyCaseData = CaseData.builder()
            .miamDetails(MiamDetails.builder()
                             .build()).build();
    }

    @Test
    public void testGetGenerators() {
        assertEquals(Collections.emptyList(), applicationsTabService.getGenerators(caseDataWithParties));
    }


    @Test
    public void testApplicantTableMapper() {
        Applicant applicant = Applicant.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender("male") //the new POJOs use strings as the enums are causing errors
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .build();

        Element<Applicant> applicantElement = Element.<Applicant>builder().value(applicant.toBuilder().gender("Male").build()).build();
        List<Element<Applicant>> expectedApplicantList = Collections.singletonList(applicantElement);
        Applicant emptyApplicant = Applicant.builder().build();
        Element<Applicant> emptyApplicantElement = Element.<Applicant>builder().value(emptyApplicant).build();
        List<Element<Applicant>> emptyApplicantList = Collections.singletonList(emptyApplicantElement);

        when(objectMapper.convertValue(partyDetails, Applicant.class)).thenReturn(applicant);
        assertEquals(expectedApplicantList, applicationsTabService.getApplicantsTable(caseDataWithParties));
        assertEquals(emptyApplicantList, applicationsTabService.getApplicantsTable(emptyCaseData));
    }

    @Test
    public void testApplicantTableMapperFl401() {

        Applicant applicant = Applicant.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender("Male") //the new POJOs use strings as the enums are causing errors
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicants(partyList)
            .build();
        Element<Applicant> applicantElement = Element.<Applicant>builder().value(applicant).build();
        List<Element<Applicant>> expectedApplicantList = Collections.singletonList(applicantElement);
        Applicant emptyApplicant = Applicant.builder().build();
        Element<Applicant> emptyApplicantElement = Element.<Applicant>builder().value(emptyApplicant).build();
        List<Element<Applicant>> emptyApplicantList = Collections.singletonList(emptyApplicantElement);

        when(objectMapper.convertValue(partyDetails, Applicant.class)).thenReturn(applicant);
        assertEquals(expectedApplicantList, applicationsTabService.getApplicantsTable(caseData));
        assertEquals(emptyApplicantList, applicationsTabService.getApplicantsTable(emptyCaseData));
    }

    @Test
    public void testApplicantsConfidentialDetails() {
        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(Gender.male)
            .address(Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build())
            .isAddressConfidential(Yes)
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .email(THIS_INFORMATION_IS_CONFIDENTIAL)
            .phoneNumber(THIS_INFORMATION_IS_CONFIDENTIAL)
            .isPhoneNumberConfidential(Yes)
            .build();

        Applicant expectedApplicant = Applicant.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(Gender.male.getDisplayedValue())
            .address(Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build())
            .isAddressConfidential(YesOrNo.Yes)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .email(THIS_INFORMATION_IS_CONFIDENTIAL)
            .phoneNumber(THIS_INFORMATION_IS_CONFIDENTIAL)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .build();

        Element<PartyDetails> applicantElement = Element.<PartyDetails>builder().value(partyDetails).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(applicantElement);
        List<Element<Applicant>> expectedApplicantList = List.of(Element.<Applicant>builder().value(expectedApplicant).build());
        when(objectMapper.convertValue(partyDetails, Applicant.class)).thenReturn(expectedApplicant);
        List<Element<Applicant>> applicantsTable = applicationsTabService.getApplicantsTable(CaseData.builder().applicants(
            applicantList).build());
        List<Element<Applicant>> expectedEmptyApplicantList = List.of(Element.<Applicant>builder().value(Applicant.builder().build()).build());
        assertEquals(1, applicantsTable.size());
        assertEquals(applicantsTable, expectedApplicantList);
        assertEquals(expectedEmptyApplicantList, applicationsTabService.getApplicantsTable(emptyCaseData));
    }

    @Test
    public void testMaskingPartyDetails() {
        PartyDetails partyDetails1 = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(Gender.male)
            .address(address)
            .isAddressConfidential(YesOrNo.Yes)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .email("test@test.com")
            .phoneNumber("1234567890")
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        PartyDetails expectedPartDetails = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(Gender.male)
            .address(Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build())
            .isAddressConfidential(YesOrNo.Yes)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .email(THIS_INFORMATION_IS_CONFIDENTIAL)
            .phoneNumber("1234567890")
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        assertNotNull(applicationsTabService.maskConfidentialDetails(List.of(getElement(partyDetails1))));
    }

    @Test
    public void testChildDetails() {

        Child child = Child.builder().firstName("Test").lastName("Name").gender(Gender.male)
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .applicantsRelationshipToChild(RelationshipsEnum.father)
            .orderAppliedFor(List.of(OrderTypeEnum.childArrangementsOrder, OrderTypeEnum.prohibitedStepsOrder))
            .personWhoLivesWithChild(getOtherPersonList()).build();
        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);


        OtherPersonWhoLivesWithChildDetails confidentilPerson1 = OtherPersonWhoLivesWithChildDetails.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails(THIS_INFORMATION_IS_CONFIDENTIAL)
            .firstName(THIS_INFORMATION_IS_CONFIDENTIAL).lastName(THIS_INFORMATION_IS_CONFIDENTIAL)
            .address(Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build()).build();
        OtherPersonWhoLivesWithChildDetails nonConfidentialPerson = OtherPersonWhoLivesWithChildDetails.builder()
            .isPersonIdentityConfidential(YesOrNo.No).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();
        OtherPersonWhoLivesWithChildDetails confidentilPerson2 = OtherPersonWhoLivesWithChildDetails.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails(THIS_INFORMATION_IS_CONFIDENTIAL)
            .firstName(THIS_INFORMATION_IS_CONFIDENTIAL).lastName(THIS_INFORMATION_IS_CONFIDENTIAL)
            .address(Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build()).build();

        ChildDetails expetcedChildDetails = ChildDetails.builder().firstName("Test")
            .lastName("Name").gender(Gender.male)
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .applicantsRelationshipToChild(RelationshipsEnum.father.getDisplayedValue())
            .orderAppliedFor("Child Arrangements Order, Prohibited Steps Order")
            .cafcassOfficerAdded(No)
            .personWhoLivesWithChild(List.of(
                Element
                    .<OtherPersonWhoLivesWithChildDetails>builder().value(confidentilPerson1).build(),
                Element.<OtherPersonWhoLivesWithChildDetails>builder().value(nonConfidentialPerson).build(),
                Element.<OtherPersonWhoLivesWithChildDetails>builder().value(confidentilPerson2).build()
            )).build();


        assertEquals(
            List.of(Element.<ChildDetails>builder().value(expetcedChildDetails).build()),
            applicationsTabService.getChildDetails(CaseData.builder().children(listOfChildren).build())
        );
    }

    @Test
    public void testWithEmptyChildDetails() {
        ChildDetails child = ChildDetails.builder().build();
        Element<ChildDetails> app = Element.<ChildDetails>builder().value(child).build();
        List<Element<ChildDetails>> childFinalList = new ArrayList<>();
        childFinalList.add(app);
        assertEquals(applicationsTabService.getChildDetails(CaseData.builder().build()), childFinalList);
    }

    @Test
    public void testUpdateTab() {
        when(objectMapper.convertValue(partyDetails, Applicant.class))
            .thenReturn(Applicant.builder().gender("male").build());
        when(objectMapper.convertValue(partyDetails, Respondent.class))
            .thenReturn(Respondent.builder().build());
        when(objectMapper.convertValue(partyDetails, OtherPersonInTheCase.class))
            .thenReturn(OtherPersonInTheCase.builder().build());
        when(objectMapper.convertValue(caseDataWithParties, AllegationsOfHarmOrders.class))
            .thenReturn(allegationsOfHarmOrders);
        when(objectMapper.convertValue(caseDataWithParties, ChildAbductionDetails.class))
            .thenReturn(
                ChildAbductionDetails.builder().build());
        when(objectMapper.convertValue(caseDataWithParties, AllegationsOfHarmOtherConcerns.class))
            .thenReturn(AllegationsOfHarmOtherConcerns.builder().build());

        assertNotNull(applicationsTabService.updateTab(caseDataWithParties));
    }

    @Test
    public void testUpdateTabWithAllegationOfHarmRevised() {

        orderRevised = OrderRevised.builder()
            .dateIssued(LocalDate.of(1990, 8, 1))
            .endDate(LocalDate.of(1991, 8, 1))
            .orderCurrent(YesOrNo.Yes)
            .courtName("Court name")
            .build();

        allegationsOfHarmRevisedOrders = AllegationsOfHarmRevisedOrders.builder()
            .newOrdersNonMolestation(YesOrNo.Yes)
            .nonMolestationOrder(orderRevised)
            .newOrdersOccupation(YesOrNo.Yes)
            .occupationOrder(orderRevised)
            .newOrdersForcedMarriageProtection(YesOrNo.Yes)
            .forcedMarriageProtectionOrder(orderRevised)
            .newOrdersRestraining(YesOrNo.Yes)
            .restrainingOrder(orderRevised)
            .newOrdersOtherInjunctive(YesOrNo.Yes)
            .otherInjunctiveOrder(orderRevised)
            .newOrdersUndertakingInPlace(YesOrNo.Yes)
            .undertakingInPlaceOrder(orderRevised)
            .build();

        DomesticAbuseBehaviours domesticAbuseBehaviours = DomesticAbuseBehaviours.builder().typeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
            .newAbuseNatureDescription("des").newBehavioursApplicantHelpSoughtWho("sought").newBehavioursApplicantSoughtHelp(
                YesOrNo.Yes).build();

        Element<DomesticAbuseBehaviours> domesticAbuseBehavioursElement = Element
            .<DomesticAbuseBehaviours>builder().value(domesticAbuseBehaviours).build();

        ChildAbuse childAbuse = ChildAbuse.builder().abuseNatureDescription("test").typeOfAbuse(ChildAbuseEnum.physicalAbuse)
            .build();

        RevisedChildAbductionDetails revisedChildAbductionDetails = RevisedChildAbductionDetails.builder()
            .newAbductionChildHasPassport(Yes).build();
        CaseData caseData = caseDataWithParties.toBuilder().taskListVersion(TASK_LIST_VERSION_V2)
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder()
                                         .childPassportDetails(ChildPassportDetails.builder().newChildPassportPossession(
                                             List
                                                 .of(NewPassportPossessionEnum.father)).build())
                                         .newAllegationsOfHarmYesNo(Yes)
                                         .newAllegationsOfHarmDomesticAbuseYesNo(Yes)
                                         .domesticBehaviours(List.of(domesticAbuseBehavioursElement))
                                         .newAllegationsOfHarmChildAbuseYesNo(YesOrNo.Yes)
                                         .allChildrenAreRiskPhysicalAbuse(YesOrNo.Yes)
                                         .allChildrenAreRiskPsychologicalAbuse(YesOrNo.Yes)
                                         .allChildrenAreRiskEmotionalAbuse(YesOrNo.Yes)
                                         .allChildrenAreRiskFinancialAbuse(YesOrNo.Yes)
                                         .allChildrenAreRiskSexualAbuse(YesOrNo.Yes)
                                         .childPhysicalAbuse(childAbuse)
                                         .childPsychologicalAbuse(childAbuse)
                                         .childEmotionalAbuse(childAbuse)
                                         .childFinancialAbuse(childAbuse)
                                         .childSexualAbuse(childAbuse).build()).build();

        when(objectMapper.convertValue(caseData, AllegationsOfHarmRevisedOrders.class))
            .thenReturn(allegationsOfHarmRevisedOrders);
        when(objectMapper.convertValue(caseData, RevisedChildAbductionDetails.class))
            .thenReturn(revisedChildAbductionDetails);
        when(objectMapper.convertValue(partyDetails, Applicant.class))
            .thenReturn(Applicant.builder().gender("male").build());
        when(objectMapper.convertValue(partyDetails, Respondent.class))
            .thenReturn(Respondent.builder().build());
        Mockito.lenient().when(allegationOfHarmRevisedService.getIfAllChildrenAreRisk(any(ChildAbuseEnum.class), any(AllegationOfHarmRevised.class)))
            .thenReturn(YesOrNo.Yes);
        Mockito.lenient().when(allegationOfHarmRevisedService.getWhichChildrenAreInRisk(any(ChildAbuseEnum.class),any(AllegationOfHarmRevised.class)))
            .thenReturn(DynamicMultiSelectList
                            .builder().value(List.of(DynamicMultiselectListElement
                                                         .builder().code("test").build())).build());
        assertNotNull(applicationsTabService.updateTab(caseData));
    }

    @Test
    public void testUpdateTabWithTaskListV3() {

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(Gender.male)
            .address(Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build())
            .isAddressConfidential(Yes)
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .email(THIS_INFORMATION_IS_CONFIDENTIAL)
            .phoneNumber(THIS_INFORMATION_IS_CONFIDENTIAL)
            .isPhoneNumberConfidential(Yes)
            .build();

        Element<PartyDetails> applicantElement = Element.<PartyDetails>builder().value(partyDetails).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(applicantElement);

        allegationsOfHarmRevisedOrders = AllegationsOfHarmRevisedOrders.builder()
            .newOrdersNonMolestation(YesOrNo.Yes)
            .nonMolestationOrder(orderRevised)
            .newOrdersOccupation(YesOrNo.Yes)
            .occupationOrder(orderRevised)
            .newOrdersForcedMarriageProtection(YesOrNo.Yes)
            .forcedMarriageProtectionOrder(orderRevised)
            .newOrdersRestraining(YesOrNo.Yes)
            .restrainingOrder(orderRevised)
            .newOrdersOtherInjunctive(YesOrNo.Yes)
            .otherInjunctiveOrder(orderRevised)
            .newOrdersUndertakingInPlace(YesOrNo.Yes)
            .undertakingInPlaceOrder(orderRevised)
            .build();

        RevisedChildAbductionDetails revisedChildAbductionDetails = RevisedChildAbductionDetails.builder()
            .newAbductionChildHasPassport(Yes).build();

        CaseData caseData = caseDataWithParties.toBuilder()
            .taskListVersion(TASK_LIST_VERSION_V3)
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder().build())
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder().build())
            .othersToNotify(applicantList)
            .applicants(applicantList)
            .respondents(applicantList)
            .build();

        when(objectMapper.convertValue(partyDetails, Applicant.class))
            .thenReturn(Applicant.builder().gender("male").build());
        when(objectMapper.convertValue(partyDetails, Respondent.class))
            .thenReturn(Respondent.builder().build());
        when(miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(any(CaseData.class), anyMap()))
            .thenReturn(caseData);
        when(objectMapper.convertValue(caseData, AllegationsOfHarmRevisedOrders.class))
            .thenReturn(allegationsOfHarmRevisedOrders);
        when(objectMapper.convertValue(caseData, RevisedChildAbductionDetails.class))
            .thenReturn(revisedChildAbductionDetails);

        assertNotNull(applicationsTabService.updateTab(caseData));
    }

    @Test
    public void testUpdateTabWithTaskListV3WithDetails() {

        PartyDetails partyDetails = PartyDetails.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender(Gender.male)
            .address(Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build())
            .isAddressConfidential(Yes)
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .email(THIS_INFORMATION_IS_CONFIDENTIAL)
            .phoneNumber(THIS_INFORMATION_IS_CONFIDENTIAL)
            .isPhoneNumberConfidential(Yes)
            .build();

        allegationsOfHarmRevisedOrders = AllegationsOfHarmRevisedOrders.builder()
            .newOrdersNonMolestation(YesOrNo.Yes)
            .nonMolestationOrder(orderRevised)
            .newOrdersOccupation(YesOrNo.Yes)
            .occupationOrder(orderRevised)
            .newOrdersForcedMarriageProtection(YesOrNo.Yes)
            .forcedMarriageProtectionOrder(orderRevised)
            .newOrdersRestraining(YesOrNo.Yes)
            .restrainingOrder(orderRevised)
            .newOrdersOtherInjunctive(YesOrNo.Yes)
            .otherInjunctiveOrder(orderRevised)
            .newOrdersUndertakingInPlace(YesOrNo.Yes)
            .undertakingInPlaceOrder(orderRevised)
            .build();

        RevisedChildAbductionDetails revisedChildAbductionDetails = RevisedChildAbductionDetails.builder()
            .newAbductionChildHasPassport(Yes).build();

        Element<PartyDetails> applicantElement = Element.<PartyDetails>builder().value(partyDetails).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(applicantElement);

        List<uk.gov.hmcts.reform.prl.enums
            .miampolicyupgrade.MiamExemptionsChecklistEnum> miamExemptionsChecklistEnums = new ArrayList<>();
        List<uk.gov.hmcts.reform.prl.enums
            .miampolicyupgrade.MiamDomesticAbuseChecklistEnum> miamExemptionsDomesticChecklistEnums = new ArrayList<>();
        CaseData caseData = caseDataWithParties.toBuilder()
            .taskListVersion(TASK_LIST_VERSION_V3)
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder().build())
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails
                .builder()
                .mpuExemptionReasons(miamExemptionsChecklistEnums)
                .mpuDomesticAbuseEvidences(miamExemptionsDomesticChecklistEnums)
                .mpuUrgencyReason(uk.gov.hmcts.reform.prl.enums.miampolicyupgrade
                    .MiamUrgencyReasonChecklistEnum.miamPolicyUpgradeUrgencyReason_Value_1)
                .mpuPreviousMiamAttendanceReason(uk.gov.hmcts.reform.prl.enums
                    .miampolicyupgrade.MiamPreviousAttendanceChecklistEnum.miamPolicyUpgradePreviousAttendance_Value_1)
                .mpuOtherExemptionReasons(uk.gov.hmcts.reform.prl.enums
                    .miampolicyupgrade.MiamOtherGroundsChecklistEnum.miamPolicyUpgradeOtherGrounds_Value_1)
                .mpuChildProtectionConcernReason(MiamPolicyUpgradeChildProtectionConcernEnum.mpuChildProtectionConcern_value_1)
                .build())
            .othersToNotify(applicantList)
            .applicants(applicantList)
            .respondents(applicantList)
            .build();

        when(objectMapper.convertValue(partyDetails, Applicant.class))
            .thenReturn(Applicant.builder().gender("male").build());
        when(objectMapper.convertValue(partyDetails, Respondent.class))
            .thenReturn(Respondent.builder().build());
        when(miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(any(CaseData.class), anyMap()))
            .thenReturn(caseData);
        when(objectMapper.convertValue(caseData, AllegationsOfHarmRevisedOrders.class))
            .thenReturn(allegationsOfHarmRevisedOrders);
        when(objectMapper.convertValue(caseData, RevisedChildAbductionDetails.class))
            .thenReturn(revisedChildAbductionDetails);

        assertNotNull(applicationsTabService.updateTab(caseData));

    }

    private List<Element<OtherPersonWhoLivesWithChild>> getOtherPersonList() {
        OtherPersonWhoLivesWithChild confidentilPerson1 = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();
        OtherPersonWhoLivesWithChild nonConfidentialPerson = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.No).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();
        OtherPersonWhoLivesWithChild confidentilPerson2 = OtherPersonWhoLivesWithChild.builder()
            .isPersonIdentityConfidential(YesOrNo.Yes).relationshipToChildDetails("test")
            .firstName("test First Name").lastName("test Last Name").address(address).build();

        return List.of(
            Element.<OtherPersonWhoLivesWithChild>builder().value(confidentilPerson1).build(),
            Element.<OtherPersonWhoLivesWithChild>builder().value(nonConfidentialPerson).build(),
            Element.<OtherPersonWhoLivesWithChild>builder().value(confidentilPerson2).build()
        );
    }

    @Test
    public void testRespondentTableMapper() {
        Respondent respondent = Respondent.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender("male") //the new POJOs use strings as the enums are causing errors
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isAtAddressLessThan5YearsWithDontKnow("dontKnow")
            .doTheyHaveLegalRepresentation("dontKnow")
            .email("test@test.com")
            .build();

        Element<Respondent> expectedRespondent = Element.<Respondent>builder().value(
            respondent
                .toBuilder()
                .gender("Male")
                .isAtAddressLessThan5YearsWithDontKnow("Don't know")
                .doTheyHaveLegalRepresentation("Don't know")
                .build())
            .build();

        List<Element<Respondent>> expectedRespondentList = Collections.singletonList(expectedRespondent);
        Respondent emptyRespondent = Respondent.builder().build();
        Element<Respondent> emptyRespondentElement = Element.<Respondent>builder().value(emptyRespondent).build();
        List<Element<Respondent>> emptyRespondentList = Collections.singletonList(emptyRespondentElement);

        when(objectMapper.convertValue(partyDetails, Respondent.class)).thenReturn(respondent);
        assertEquals(expectedRespondentList, applicationsTabService.getRespondentsTable(caseDataWithParties));
        assertEquals(emptyRespondentList, applicationsTabService.getRespondentsTable(emptyCaseData));
    }

    @Test
    public void testDeclarationTable() {
        String solicitor = caseDataWithParties.getSolicitorName();
        Map<String, Object> expectedDeclarationMap = new HashMap<>();
        String declarationText = "I understand that proceedings for contempt of court may be brought"
            + " against anyone who makes, or causes to be made, a false statement in a document verified"
            + " by a statement of truth without an honest belief in its truth. The applicant believes "
            + "that the facts stated in this form and any continuation sheets are true. " + solicitor
            + " is authorised by the applicant to sign this statement.";
        expectedDeclarationMap.put("declarationText", declarationText);
        expectedDeclarationMap.put("agreedBy", solicitor);

        assertEquals(expectedDeclarationMap, applicationsTabService.getDeclarationTable(caseDataWithParties));
    }

    @Test
    public void testHearingUrgencyTableMapper() {
        HearingUrgency hearingUrgency = HearingUrgency.builder()
            .isCaseUrgent(YesOrNo.Yes)
            .caseUrgencyTimeAndReason("Test String")
            .doYouRequireAHearingWithReducedNotice(YesOrNo.No)
            .build();
        Map<String, Object> hearingUrgencyMap = Map.of(
            "isCaseUrgent", "Yes",
            "caseUrgencyTimeAndReason", "Test String",
            "doYouRequireAHearingWithReducedNotice", "No"
        );

        when(objectMapper.convertValue(caseDataWithParties, HearingUrgency.class)).thenReturn(hearingUrgency);
        when(objectMapper.convertValue(hearingUrgency, Map.class)).thenReturn(hearingUrgencyMap);
        assertEquals(hearingUrgencyMap, applicationsTabService.getHearingUrgencyTable(caseDataWithParties));
    }

    @Test
    public void testApplicationTypeTableMapper() {
        TypeOfApplication typeOfApplication = TypeOfApplication.builder()
            .ordersApplyingFor("Child Arrangements Order")
            .typeOfChildArrangementsOrder("Spend time with order")
            .natureOfOrder("Test nature of order")
            .applicationPermissionRequired("Yes")
            .applicationPermissionRequiredReason("Some xyz reason")
            .build();
        Map<String, Object> typeOfApplicationMap = Map.of(
            "ordersApplyingFor", "Child Arrangements Order",
            "typeOfChildArrangementsOrder", "Spend time with order",
            "natureOfOrder", "Test nature of order",
            "applicationPermissionRequired", "Yes",
            "applicationPermissionRequiredReason", "Some xyz reason"
        );

        when(objectMapper.convertValue(typeOfApplication, Map.class)).thenReturn(typeOfApplicationMap);
        assertEquals(typeOfApplicationMap, applicationsTabService.getTypeOfApplicationTable(caseDataWithParties));
        assertEquals(Collections.emptyMap(), applicationsTabService.getTypeOfApplicationTable(emptyCaseData));
    }

    @Test
    public void testAllegationsOfHarmOverview() {
        AllegationsOfHarmOverview allegationsOfHarmOverview = AllegationsOfHarmOverview.builder()
            .allegationsOfHarmYesNo(YesOrNo.Yes)
            .allegationsOfHarmDomesticAbuseYesNo(YesOrNo.Yes)
            .allegationsOfHarmChildAbductionYesNo(YesOrNo.Yes)
            .build();
        Map<String, Object> allegationsOfHarmOverviewMap = Map.of(
            "allegationsOfHarmYesNo", "Yes",
            "allegationsOfHarmDomesticAbuseYesNo", "Yes",
            "allegationsOfHarmChildAbductionYesNo", "Yes"
        );
        when(objectMapper.convertValue(caseDataWithParties, AllegationsOfHarmOverview.class))
            .thenReturn(allegationsOfHarmOverview);
        when(objectMapper.convertValue(allegationsOfHarmOverview, Map.class)).thenReturn(allegationsOfHarmOverviewMap);
        assertEquals(
            allegationsOfHarmOverviewMap,
            applicationsTabService.getAllegationsOfHarmOverviewTable(caseDataWithParties)
        );
    }

    @Test
    public void testMiamTableMapper() {
        Miam miam = Miam.builder()
            .applicantAttendedMiam(YesOrNo.Yes)
            .claimingExemptionMiam(YesOrNo.No)
            .familyMediatorMiam(YesOrNo.Yes)
            .build();
        Map<String, Object> miamMap = Map.of(
            "applicantAttendedMiam", "Yes",
            "claimingExemptionMiam", "No",
            "familyMediatorMiam", "Yes"
        );
        when(objectMapper.convertValue(caseDataWithParties, Miam.class)).thenReturn(miam);
        when(objectMapper.convertValue(miam, Map.class)).thenReturn(miamMap);
        assertEquals(miamMap, applicationsTabService.getMiamTable(caseDataWithParties));
    }

    @Test
    public void testCompleteMiamExemptionsTableMapper() {
        String exemptions = MiamExemptionsChecklistEnum.domesticViolence.getDisplayedValue();
        String domestic = MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_4.getDisplayedValue();
        String urgency = MiamUrgencyReasonChecklistEnum.miamUrgencyReasonChecklistEnum_Value_1.getDisplayedValue();
        String previous = MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_1.getDisplayedValue();
        String child = MiamChildProtectionConcernChecklistEnum.MIAMChildProtectionConcernChecklistEnum_value_1.getDisplayedValue();
        String other = MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_2.getDisplayedValue();

        MiamExemptions miamExemptions = MiamExemptions.builder()
            .reasonsForMiamExemption(exemptions)
            .domesticViolenceEvidence(domestic)
            .urgencyEvidence(urgency)
            .previousAttendenceEvidence(previous)
            .childProtectionEvidence(child)
            .otherGroundsEvidence(other)
            .build();
        Map<String, Object> miamExemptionsMap = Map.of(
            "reasonsForMiamExemption", exemptions,
            "domesticViolenceEvidence", domestic,
            "urgencyEvidence", urgency,
            "previousAttendenceEvidence", previous,
            "childProtectionEvidence", child,
            "otherGroundsEvidence", other
        );
        when(objectMapper.convertValue(miamExemptions, Map.class)).thenReturn(miamExemptionsMap);
        assertEquals(miamExemptionsMap, applicationsTabService.getMiamExemptionsTable(caseDataWithParties));
    }

    @Test
    public void testEmptyMiamExemptionsTableMapper() {
        MiamExemptions miamExemptions = MiamExemptions.builder()
            .reasonsForMiamExemption("")
            .domesticViolenceEvidence("")
            .urgencyEvidence("")
            .previousAttendenceEvidence("")
            .childProtectionEvidence("")
            .otherGroundsEvidence("")
            .build();
        Map<String, Object> miamExemptionsMap = Map.of(
            "reasonsForMiamExemption", "",
            "domesticViolenceEvidence", "",
            "urgencyEvidence", "",
            "previousAttendenceEvidence", "",
            "childProtectionEvidence", "",
            "otherGroundsEvidence", ""
        );
        when(objectMapper.convertValue(miamExemptions, Map.class)).thenReturn(miamExemptionsMap);
        assertEquals(miamExemptionsMap, applicationsTabService.getMiamExemptionsTable(emptyCaseData));
    }

    @Test
    public void testOtherProceedingsOverviewTableMapper() {
        Map<String, Object> completeOverviewMap = Map.of(
            PrlAppsConstants.PREVIOUS_OR_ONGOING_PROCEEDINGS,
            caseDataWithParties.getPreviousOrOngoingProceedingsForChildren()
                .getDisplayedValue()
        );
        Map<String, Object> emptyOverviewMap = Map.of(PrlAppsConstants.PREVIOUS_OR_ONGOING_PROCEEDINGS, "");

        assertEquals(completeOverviewMap, applicationsTabService.getOtherProceedingsTable(caseDataWithParties));
        assertEquals(emptyOverviewMap, applicationsTabService.getOtherProceedingsTable(emptyCaseData));

    }

    @Test
    public void testOtherProceedingsDetailsTableMapper() {
        OtherProceedingsDetails otherProceedingsDetails = OtherProceedingsDetails.builder()
            .previousOrOngoingProceedings(ProceedingsEnum.previous.getDisplayedValue())
            .caseNumber("12345")
            .dateStarted(LocalDate.of(1990, 8, 1))
            .dateEnded(LocalDate.of(1991, 8, 1))
            .typeOfOrder(TypeOfOrderEnum.otherOrder.getDisplayedValue())
            .otherTypeOfOrder("Test Order")
            .nameOfJudge("Test Judge")
            .nameOfCourt("Test Court")
            .nameOfChildrenInvolved("Children")
            .nameOfGuardian("Guardian")
            .build();
        Element<OtherProceedingsDetails> otherProceedingsDetailsElement = Element
            .<OtherProceedingsDetails>builder().value(otherProceedingsDetails).build();

        OtherProceedingsDetails emptyProceeding = OtherProceedingsDetails.builder().build();
        Element<OtherProceedingsDetails> emptyProceedingElement = Element
            .<OtherProceedingsDetails>builder().value(emptyProceeding).build();

        assertEquals(
            Collections.singletonList(otherProceedingsDetailsElement),
            applicationsTabService.getOtherProceedingsDetailsTable(caseDataWithParties)
        );
        assertEquals(Collections.singletonList(emptyProceedingElement), applicationsTabService
            .getOtherProceedingsDetailsTable(emptyCaseData));
    }


    @Test
    public void testInternationalElementTableMapper() {
        InternationalElement internationalElement = InternationalElement.builder()
            .habitualResidentInOtherState(YesOrNo.Yes)
            .habitualResidentInOtherStateGiveReason("Example reason")
            .requestToForeignAuthority(YesOrNo.No)
            .build();

        Map<String, Object> internationalElementMap = Map.of(
            "habitualResidentInOtherState", YesOrNo.Yes,
            "habitualResidentInOtherStateGiveReason", "Example reason",
            "requestToForeignAuthority", YesOrNo.Yes
        );
        when(objectMapper.convertValue(caseDataWithParties, InternationalElement.class))
            .thenReturn(internationalElement);
        when(objectMapper.convertValue(internationalElement, Map.class)).thenReturn(internationalElementMap);
        assertEquals(internationalElementMap, applicationsTabService.getInternationalElementTable(caseDataWithParties));
    }

    @Test
    public void testAttendingTheHearingTableMapper() {
        AttendingTheHearing attendingTheHearing = AttendingTheHearing.builder()
            .isWelshNeeded(YesOrNo.Yes)
            .isDisabilityPresent(YesOrNo.No)
            .adjustmentsRequired("Adjustments String")
            .build();

        Map<String, Object> attendingTheHearingMap = Map.of(
            "isWelshNeeded", YesOrNo.Yes,
            "adjustmentsRequired", "Adjustments String",
            "isDisabilityPresent", YesOrNo.No
        );
        when(objectMapper.convertValue(caseDataWithParties, AttendingTheHearing.class))
            .thenReturn(attendingTheHearing);
        when(objectMapper.convertValue(attendingTheHearing, Map.class)).thenReturn(attendingTheHearingMap);
        assertEquals(attendingTheHearingMap, applicationsTabService.getAttendingTheHearingTable(caseDataWithParties));
    }

    @Test
    public void testLitigationCapacityTableMapper() {
        LitigationCapacity litigationCapacity = LitigationCapacity.builder()
            .litigationCapacityFactors("Test")
            .litigationCapacityOtherFactors(YesOrNo.Yes)
            .build();

        Map<String, Object> litigationCapacityMap = Map.of(
            "litigationCapacityFactors", "Test",
            "litigationCapacityOtherFactors", YesOrNo.Yes
        );
        when(objectMapper.convertValue(caseDataWithParties, LitigationCapacity.class))
            .thenReturn(litigationCapacity);
        when(objectMapper.convertValue(litigationCapacity, Map.class)).thenReturn(litigationCapacityMap);
        assertEquals(litigationCapacityMap, applicationsTabService.getLitigationCapacityDetails(caseDataWithParties));
    }

    @Test
    public void testSpecificOrderMapping() {
        assertEquals(allegationsOfHarmOrders, applicationsTabService
            .getSpecificOrderDetails(emptyAllegationOfHarmOrder, caseDataWithParties));
    }

    @Test
    public void testAllegationsOfHarmOrders() {
        AllegationsOfHarmOrders allegationsOfHarmOrders = AllegationsOfHarmOrders.builder()
            .ordersNonMolestation(YesOrNo.Yes)
            .nonMolestationOrder(Order.builder()
                                     .courtName("non mol test")
                                     .build())
            .build();
        CaseData orderCaseData = CaseData.builder()
            .allegationOfHarm(AllegationOfHarm.builder()
                                  .ordersNonMolestation(YesOrNo.Yes)
                                  .ordersNonMolestationCourtName("non mol test").build())
            .build();
        Map<String, Object> orderMap = Map.of(
            "ordersNonMolestation", "Yes",
            "nonMolestationOrder", Map.of("courtName", "non mol test")
        );
        when(objectMapper.convertValue(
            orderCaseData,
            AllegationsOfHarmOrders.class
        )).thenReturn(allegationsOfHarmOrders);
        when(objectMapper.convertValue(allegationsOfHarmOrders, Map.class)).thenReturn(orderMap);
        assertEquals(orderMap, applicationsTabService.getAllegationsOfHarmOrdersTable(orderCaseData));
    }

    @Test
    public void testAllegationsOfHarmDomesticAbuseTableMapper() {
        String abuseVictim = ApplicantOrChildren.children.getDisplayedValue();
        DomesticAbuseVictim domesticAbuseVictim = DomesticAbuseVictim.builder()
            .physicalAbuseVictim(abuseVictim)
            .emotionalAbuseVictim(abuseVictim)
            .psychologicalAbuseVictim(abuseVictim)
            .sexualAbuseVictim(abuseVictim)
            .financialAbuseVictim(abuseVictim)
            .build();
        Map<String, Object> abuseMap = Map.of(
            "physicalAbuseVictim", abuseVictim,
            "emotionalAbuseVictim", abuseVictim,
            "psychologicalAbuseVictim", abuseVictim,
            "sexualAbuseVictim", abuseVictim,
            "financialAbuseVictim", abuseVictim
        );

        when(objectMapper.convertValue(domesticAbuseVictim, Map.class)).thenReturn(abuseMap);
        assertEquals(abuseMap, applicationsTabService.getDomesticAbuseTable(caseDataWithParties));
    }

    @Test
    public void testWelshLanguageTableMapper() {
        WelshLanguageRequirements welshLanguageRequirements = WelshLanguageRequirements.builder()
            .welshLanguageRequirement(YesOrNo.Yes)
            .languageRequirementApplicationNeedWelsh(YesOrNo.No)
            .build();

        Map<String, Object> welshMap = Map.of(
            "welshLanguageRequirement", YesOrNo.Yes,
            "languageRequirementApplicationNeedWelsh", YesOrNo.No
        );
        when(objectMapper.convertValue(caseDataWithParties, WelshLanguageRequirements.class))
            .thenReturn(welshLanguageRequirements);
        when(objectMapper.convertValue(welshLanguageRequirements, Map.class)).thenReturn(welshMap);
        assertEquals(welshMap, applicationsTabService.getWelshLanguageRequirementsTable(caseDataWithParties));

    }

    @Test
    public void testChildAbuductionTableMapper() {
        ChildAbductionDetails childAbductionDetails = ChildAbductionDetails.builder()
            .previousAbductionThreats(YesOrNo.Yes)
            .previousAbductionThreatsDetails("Details")
            .abductionPreviousPoliceInvolvement(YesOrNo.No)
            .build();
        Map<String, Object> abductionMap = Map.of(
            "previousAbductionThreats", YesOrNo.Yes,
            "previousAbductionThreatsDetails", "Details",
            "abductionPreviousPoliceInvolvement", YesOrNo.No
        );

        when(objectMapper.convertValue(caseDataWithParties, ChildAbductionDetails.class))
            .thenReturn(childAbductionDetails);
        when(objectMapper.convertValue(childAbductionDetails, Map.class)).thenReturn(abductionMap);
        assertEquals(abductionMap, applicationsTabService.getChildAbductionTable(caseDataWithParties));
    }

    @Test
    public void testAllegationsOfHarmOtherConcernsMapper() {
        AllegationsOfHarmOtherConcerns allegationsOfHarmOtherConcerns = AllegationsOfHarmOtherConcerns.builder()
            .allegationsOfHarmOtherConcerns(YesOrNo.Yes)
            .allegationsOfHarmOtherConcernsDetails("Test String")
            .agreeChildUnsupervisedTime(YesOrNo.No)
            .build();
        Map<String, Object> concernMap = Map.of(
            "allegationsOfHarmOtherConcerns", YesOrNo.Yes,
            "allegationsOfHarmOtherConcernsDetails", "Test String",
            "agreeChildUnsupervisedTime", YesOrNo.No
        );

        when(objectMapper.convertValue(caseDataWithParties, AllegationsOfHarmOtherConcerns.class))
            .thenReturn(allegationsOfHarmOtherConcerns);
        when(objectMapper.convertValue(allegationsOfHarmOtherConcerns, Map.class)).thenReturn(concernMap);
        assertEquals(concernMap, applicationsTabService.getAllegationsOfHarmOtherConcerns(caseDataWithParties));
    }

    @Test
    public void testOtherPeopleInTheCaseMapper() {
        OtherPersonInTheCase otherPerson = OtherPersonInTheCase.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender("male") //the new POJOs use strings as the enums are causing errors
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .build();

        List<Element<OtherPersonRelationshipToChild>> expectedRelationship = List.of(Element.<OtherPersonRelationshipToChild>builder().value(
            OtherPersonRelationshipToChild.builder().personRelationshipToChild("Bro").build()).build());

        Element<OtherPersonInTheCase> otherPersonElement = Element.<OtherPersonInTheCase>builder()
            .value(otherPerson.toBuilder().gender("Male").relationshipToChild(expectedRelationship).build()).build();
        List<Element<OtherPersonInTheCase>> expectedList = Collections.singletonList(otherPersonElement);
        OtherPersonInTheCase emptyOtherPerson = OtherPersonInTheCase.builder().build();
        Element<OtherPersonInTheCase> emptyOtherElement = Element.<OtherPersonInTheCase>builder()
            .value(emptyOtherPerson).build();
        List<Element<OtherPersonInTheCase>> expectedEmptyList = Collections.singletonList(emptyOtherElement);

        when(objectMapper.convertValue(partyDetails, OtherPersonInTheCase.class)).thenReturn(otherPerson);
        assertEquals(expectedList, applicationsTabService.getOtherPeopleInTheCaseTable(caseDataWithParties));
        assertEquals(expectedEmptyList, applicationsTabService.getOtherPeopleInTheCaseTable(emptyCaseData));
    }

    @Test
    public void testOtherChildFieldsMapper() {
        Map<String, Object> extraMap = Map.of(
            "childrenKnownToLocalAuthority", YesNoDontKnow.yes.getDisplayedValue(),
            "childrenKnownToLocalAuthorityTextArea", "Test string",
            "childrenSubjectOfChildProtectionPlan", YesNoDontKnow.yes.getDisplayedValue()

        );
        assertEquals(extraMap, applicationsTabService.getExtraChildDetailsTable(caseDataWithParties));
    }

    @Test
    public void testGetFL401TypeOfApplicationTable() {
        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(TypeOfApplicationOrders.builder().orderType(Collections.singletonList(
                FL401OrderTypeEnum.occupationOrder)).build())
            .typeOfApplicationLinkToCA(LinkToCA.builder().linkToCaApplication(Yes).caApplicationNumber("123").build())
            .build();
        Map<String, Object> expected = Map.of("ordersApplyingFor", FL401OrderTypeEnum.occupationOrder,
                                              "isLinkedToChildArrangementApplication", Yes,
                                              "caCaseNumber", "123"
        );

        when(objectMapper.convertValue(Fl401TypeOfApplication.builder().ordersApplyingFor(FL401OrderTypeEnum.occupationOrder.getDisplayedValue())
                                           .isLinkedToChildArrangementApplication(
                                               Yes).caCaseNumber("123").build(), Map.class)).thenReturn(expected);

        Map<String, Object> result = applicationsTabService.getFL401TypeOfApplicationTable(caseData);
        assertEquals(expected, result);

    }

    @Test
    public void testGetFL401TypeOfApplicationTableNoCase() {
        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(TypeOfApplicationOrders.builder().orderType(Collections.singletonList(
                FL401OrderTypeEnum.occupationOrder)).build())
            .build();
        Map<String, Object> expected = Map.of("ordersApplyingFor", FL401OrderTypeEnum.occupationOrder);
        when(objectMapper.convertValue(Fl401TypeOfApplication.builder().ordersApplyingFor(FL401OrderTypeEnum.occupationOrder.getDisplayedValue())
                                           .build(), Map.class)).thenReturn(expected);
        Map<String, Object> result = applicationsTabService.getFL401TypeOfApplicationTable(caseData);
        assertEquals(expected, result);

    }

    @Test
    public void testGetWithoutNoticeOrder() {
        CaseData caseData = CaseData.builder().orderWithoutGivingNoticeToRespondent(WithoutNoticeOrderDetails.builder().orderWithoutGivingNotice(
            Yes).build()).reasonForOrderWithoutGivingNotice(ReasonForWithoutNoticeOrder.builder().reasonForOrderWithoutGivingNotice(
            Collections.singletonList(ReasonForOrderWithoutGivingNoticeEnum.harmToApplicantOrChild)).futherDetails(
            "details").build()).bailDetails(
            RespondentBailConditionDetails.builder().isRespondentAlreadyInBailCondition(YesNoDontKnow.yes).bailConditionEndDate(
                LocalDate.of(2021, 11, 30)).build()).anyOtherDtailsForWithoutNoticeOrder(
            OtherDetailsOfWithoutNoticeOrder.builder().otherDetails("otherDetails").build()).build();

        Map<String, Object> expected = Map.of("orderWithoutGivingNotice",
                                              "Yes",
                                              "reasonForOrderWithoutGivingNotice",
                                              "There is risk of significant harm to the applicant or a relevant child, "
                                                  + "attributable to conduct of the respondent, "
                                                  + "if the order is not made immediately",
                                              "futherDetails",
                                              "details",
                                              "isRespondentAlreadyInBailCondition",
                                              "yes",
                                              "bailConditionEndDate",
                                              "2021-11-30",
                                              "anyOtherDtailsForWithoutNoticeOrder",
                                              "otherDetails"
        );
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Map.class))).thenReturn(expected);
        assertEquals(expected, applicationsTabService.getWithoutNoticeOrder(caseData));
    }

    @Test
    public void testGetFl401ApplicantsTable() {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(PartyDetails.builder()
                                 .firstName("testUser")
                                 .lastName("last test")
                                 .gender(Gender.male)
                                 .solicitorEmail("testing@courtadmin.com")
                                 .canYouProvideEmailAddress(YesOrNo.Yes)
                                 .isEmailAddressConfidential(YesOrNo.Yes)
                                 .isPhoneNumberConfidential(YesOrNo.No)
                                 .isAddressConfidential(YesOrNo.Yes)
                                 .build())
            .build();

        FL401Applicant expectedApplicant = FL401Applicant.builder()
            .firstName("testUser")
            .lastName("last test")
            .gender("Male")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.No)
            .isAddressConfidential(YesOrNo.Yes)
            .build();

        Map<String, Object> expected = Map.of("isPhoneNumberConfidential", THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "isEmailAddressConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "isAddressConfidential", THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "gender", "Male"
        );
        when(objectMapper.convertValue(applicationsTabService.maskFl401ConfidentialDetails(caseData.getApplicantsFL401()), FL401Applicant.class))
            .thenReturn(expectedApplicant);
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Map.class))).thenReturn(expected);
        Map<String, Object> result = applicationsTabService.getFl401ApplicantsTable(caseData);
        assertEquals(expected, result);

    }


    @Test
    public void testGetFl401ApplicantsSolictorDetailsTable() {

        partyDetails = PartyDetails.builder()
            .representativeFirstName("testUser")
            .representativeLastName("test test")
            .build();

        caseDataWithParties = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(partyDetails)
            .build();

        Map<String, Object> expected = Map.of(
            "representativeFirstName",
            "testUser",
            "representativeLastName",
            "test test"
        );
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Map.class))).thenReturn(expected);
        Map<String, Object> result = applicationsTabService.getFl401ApplicantsSolictorDetailsTable(caseDataWithParties);
        assertEquals(expected, result);
    }

    @Test
    public void testGetFl401RespondentTable() {

        partyDetails = PartyDetails.builder()
            .firstName("testUser")
            .lastName("last test")
            .solicitorEmail("testing@courtadmin.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(Yes)
            .isAddressConfidential(YesOrNo.Yes)
            .build();

        caseDataWithParties = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .respondentsFL401(partyDetails)
            .build();

        Map<String, Object> expected = Map.of("firstName",
                                              "testUser",
                                              "lastName",
                                              "test test",
                                              "isPhoneNumberConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "isEmailAddressConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "isAddressConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL
        );
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Map.class))).thenReturn(expected);
        Map<String, Object> result = applicationsTabService.getFl401RespondentTable(caseDataWithParties);
        assertEquals(expected, result);
    }

    @Test
    public void testGetFl401RespondentBehaviourTable() {


        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoingToChild(Collections.singletonList(
                applicantStopFromRespondentDoingToChildEnum_Value_1))
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();

        caseDataWithParties = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .respondentBehaviourData(respondentBehaviour)
            .build();

        Map<String, Object> expected = Map.of("otherReasonApplicantWantToStopFromRespondentDoing",
                                              "Test data",
                                              "applicantWantToStopFromRespondentDoingToChild",
                                              "Being violent or threatening towards their child or children",
                                              "isPhoneNumberConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "isEmailAddressConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "applicantWantToStopFromRespondentDoing",
                                              "Being violent or threatening towards them"
        );
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Map.class))).thenReturn(expected);
        Map<String, Object> result = applicationsTabService.getFl401RespondentBehaviourTable(caseDataWithParties);
        assertEquals(expected, result);
    }

    @Test
    public void testGetFl401RespondentBehaviourTableWithNoBehaviourTowardsChildren() {


        RespondentBehaviour respondentBehaviour = RespondentBehaviour.builder()
            .otherReasonApplicantWantToStopFromRespondentDoing("Test data")
            .applicantWantToStopFromRespondentDoingToChild(null)
            .applicantWantToStopFromRespondentDoing(Collections.singletonList(applicantStopFromRespondentEnum_Value_1)).build();

        caseDataWithParties = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .respondentBehaviourData(respondentBehaviour)
            .build();

        Map<String, Object> expected = Map.of("otherReasonApplicantWantToStopFromRespondentDoing",
                                              "Test data",
                                              "isPhoneNumberConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "isEmailAddressConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "applicantWantToStopFromRespondentDoing",
                                              "Being violent or threatening towards them"
        );
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Map.class))).thenReturn(expected);
        Map<String, Object> result = applicationsTabService.getFl401RespondentBehaviourTable(caseDataWithParties);
        assertEquals(expected, result);
    }

    @Test
    public void testGetFl401RelationshipToRespondentTable() {


        CaseData caseData = CaseData.builder()
            .respondentRelationObject(RespondentRelationObjectType.builder()
                                          .applicantRelationship(ApplicantRelationshipEnum.noneOfTheAbove)
                                          .build())
            .respondentRelationOptions(RespondentRelationOptionsInfo.builder()
                                           .applicantRelationshipOptions(ApplicantRelationshipOptionsEnum.aunt)
                                           .build())
            .respondentRelationDateInfoObject(RespondentRelationDateInfo.builder().applicantRelationshipDate(LocalDate.now())
                                                  .relationStartAndEndComplexType(
                                                      RelationshipDateComplex.builder().relationshipDateComplexEndDate(
                                                              LocalDate.now()).relationshipDateComplexEndDate(LocalDate.now())
                                                          .build()).build())
            .build();

        Map<String, Object> expected = Map.of("otherReasonApplicantWantToStopFromRespondentDoing",
                                              "Test data",
                                              "applicantWantToStopFromRespondentDoingToChild",
                                              "Being violent or threatening towards their child or children",
                                              "isPhoneNumberConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "isEmailAddressConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "applicantWantToStopFromRespondentDoing",
                                              "Being violent or threatening towards them"
        );
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Map.class))).thenReturn(expected);
        Map<String, Object> result = applicationsTabService.getFl401RelationshipToRespondentTable(caseData);
        assertEquals(expected, result);
    }


    @Test
    public void tesGetHomeDetails() {

        ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
            .keepChildrenInfoConfidential(YesOrNo.Yes)
            .childFullName("child")
            .childsAge("12")
            .isRespondentResponsibleForChild(YesOrNo.Yes)
            .build();

        Home home = Home.builder()
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(No)
            .children(List.of(Element.<ChildrenLiveAtAddress>builder().value(childrenLiveAtAddress).build()))
            .doAnyChildrenLiveAtAddress(No)
            .isPropertyRented(No)
            .isThereMortgageOnProperty(No)
            .isPropertyAdapted(No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .mortgages(Mortgage.builder().address(Address.builder().addressLine1("123").build()).mortgageLenderName(
                    "wer")
                           .mortgageNumber("1234").mortgageNamedAfter(Collections.singletonList(MortgageNamedAfterEnum.applicant)).build())
            .landlords(Landlord.builder().landlordName("test")
                           .mortgageNamedAfterList(Collections.singletonList(MortgageNamedAfterEnum.applicant)).address(
                    Address.builder().addressLine1("123").build()).build())
            .build();
        CaseData caseData = CaseData.builder().home(home).build();

        Map<String, Object> expected = Map.of("otherReasonApplicantWantToStopFromRespondentDoing",
                                              "Test data",
                                              "applicantWantToStopFromRespondentDoingToChild",
                                              "Being violent or threatening towards their child or children",
                                              "isPhoneNumberConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "isEmailAddressConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "applicantWantToStopFromRespondentDoing",
                                              "Being violent or threatening towards them"
        );
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Map.class))).thenReturn(expected);
        Map<String, Object> result = applicationsTabService.getHomeDetails(caseData);
        assertEquals(expected, result);
    }

    @Test
    public void tesGetHomeDetailsWithNoChild() {

        ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
            .keepChildrenInfoConfidential(YesOrNo.Yes)
            .childFullName("child")
            .childsAge("12")
            .isRespondentResponsibleForChild(YesOrNo.Yes)
            .build();

        Home home = Home.builder()
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(No)
            .doAnyChildrenLiveAtAddress(No)
            .isPropertyRented(No)
            .isThereMortgageOnProperty(No)
            .isPropertyAdapted(No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .mortgages(Mortgage.builder().address(Address.builder().addressLine1("123").build()).mortgageLenderName(
                    "wer")
                           .mortgageNumber("1234").mortgageNamedAfter(Collections.singletonList(MortgageNamedAfterEnum.applicant)).build())
            .landlords(Landlord.builder().landlordName("test")
                           .mortgageNamedAfterList(Collections.singletonList(MortgageNamedAfterEnum.applicant)).address(
                    Address.builder().addressLine1("123").build()).build())
            .build();
        CaseData caseData = CaseData.builder().home(home).build();

        Map<String, Object> expected = Map.of("otherReasonApplicantWantToStopFromRespondentDoing",
                                              "Test data",
                                              "applicantWantToStopFromRespondentDoingToChild",
                                              "Being violent or threatening towards their child or children",
                                              "isPhoneNumberConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "isEmailAddressConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "applicantWantToStopFromRespondentDoing",
                                              "Being violent or threatening towards them"
        );
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Map.class))).thenReturn(expected);
        Map<String, Object> result = applicationsTabService.getHomeDetails(caseData);
        assertEquals(expected, result);
    }


    @Test
    public void tesGetHomeDetailsWithLandordAsNull() {

        ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
            .keepChildrenInfoConfidential(YesOrNo.Yes)
            .childFullName("child")
            .childsAge("12")
            .isRespondentResponsibleForChild(YesOrNo.Yes)
            .build();

        Home home = Home.builder()
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(No)
            .children(List.of(Element.<ChildrenLiveAtAddress>builder().value(childrenLiveAtAddress).build()))
            .doAnyChildrenLiveAtAddress(No)
            .isPropertyRented(No)
            .isThereMortgageOnProperty(Yes)
            .isPropertyAdapted(No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .mortgages(Mortgage.builder().address(Address.builder().addressLine1("123").build()).mortgageLenderName(
                    "wer")
                           .mortgageNumber("1234").mortgageNamedAfter(Collections.singletonList(MortgageNamedAfterEnum.applicant)).build())
            .landlords(Landlord.builder().landlordName(null)
                           .mortgageNamedAfterList(null).address(
                    null).build())
            .build();
        CaseData caseData = CaseData.builder().home(home).build();

        Map<String, Object> expected = Map.of(
            "otherReasonApplicantWantToStopFromRespondentDoing",
            "Test data",
            "applicantWantToStopFromRespondentDoingToChild",
            "Being violent or threatening towards their child or children",
            "isPhoneNumberConfidential",
            THIS_INFORMATION_IS_CONFIDENTIAL,
            "isEmailAddressConfidential",
            THIS_INFORMATION_IS_CONFIDENTIAL,
            "applicantWantToStopFromRespondentDoing",
            "Being violent or threatening towards them"
        );
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Map.class))).thenReturn(expected);
        Map<String, Object> result = applicationsTabService.getHomeDetails(caseData);
        assertEquals(expected, result);
    }

    @Test
    public void tesGetHomeDetailsWithMortagageAsNull() {

        ChildrenLiveAtAddress childrenLiveAtAddress = ChildrenLiveAtAddress.builder()
            .keepChildrenInfoConfidential(YesOrNo.Yes)
            .childFullName("child")
            .childsAge("12")
            .isRespondentResponsibleForChild(YesOrNo.Yes)
            .build();

        Home home = Home.builder()
            .everLivedAtTheAddress(YesNoBothEnum.yesApplicant)
            .doesApplicantHaveHomeRights(No)
            .children(List.of(Element.<ChildrenLiveAtAddress>builder().value(childrenLiveAtAddress).build()))
            .doAnyChildrenLiveAtAddress(No)
            .isPropertyRented(No)
            .isThereMortgageOnProperty(No)
            .isPropertyAdapted(No)
            .peopleLivingAtThisAddress(List.of(PeopleLivingAtThisAddressEnum.applicant))
            .familyHome(List.of(FamilyHomeEnum.payForRepairs))
            .livingSituation(List.of(LivingSituationEnum.awayFromHome))
            .mortgages(Mortgage.builder().address(null)
                           .mortgageLenderName(null)
                           .mortgageNumber(null)
                           .mortgageNamedAfter(null).build())
            .landlords(Landlord.builder().landlordName(null)
                           .mortgageNamedAfterList(null).address(
                    null).build())
            .build();
        CaseData caseData = CaseData.builder().home(home).build();

        Map<String, Object> expected = Map.of(
            "otherReasonApplicantWantToStopFromRespondentDoing",
            "Test data",
            "applicantWantToStopFromRespondentDoingToChild",
            "Being violent or threatening towards their child or children",
            "isPhoneNumberConfidential",
            THIS_INFORMATION_IS_CONFIDENTIAL,
            "isEmailAddressConfidential",
            THIS_INFORMATION_IS_CONFIDENTIAL,
            "applicantWantToStopFromRespondentDoing",
            "Being violent or threatening towards them"
        );
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Map.class))).thenReturn(expected);
        Map<String, Object> result = applicationsTabService.getHomeDetails(caseData);
        assertEquals(expected, result);
    }

    @Test
    public void testGetApplicantsFamilyDetails() {
        ApplicantChild applicantChild = ApplicantChild.builder()
            .fullName("Testing Child")
            .applicantChildRelationship("Testing")
            .build();

        Element<ApplicantChild> wrappedApplicantChild = Element.<ApplicantChild>builder().value(applicantChild).build();
        List<Element<ApplicantChild>> listOfApplicantChild = Collections.singletonList(wrappedApplicantChild);

        ApplicantFamilyDetails applicantFamilyDetails = ApplicantFamilyDetails.builder()
            .doesApplicantHaveChildren(Yes)
            .build();

        CaseData caseData = CaseData.builder()
            .applicantFamilyDetails(applicantFamilyDetails)
            .applicantChildDetails(listOfApplicantChild)
            .build();


        Map<String, Object> expected = Map.of("otherReasonApplicantWantToStopFromRespondentDoing",
                                              "Test data",
                                              "applicantWantToStopFromRespondentDoingToChild",
                                              "Being violent or threatening towards their child or children",
                                              "isPhoneNumberConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "isEmailAddressConfidential",
                                              THIS_INFORMATION_IS_CONFIDENTIAL,
                                              "applicantWantToStopFromRespondentDoing",
                                              "Being violent or threatening towards them"
        );
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Map.class))).thenReturn(expected);
        Map<String, Object> result = applicationsTabService.getApplicantsFamilyDetails(caseData);
        assertEquals(expected, result);
    }

    @Test
    public void testGetFL401OtherProceedingsTable() {

        CaseData caseData = CaseData.builder().fl401OtherProceedingDetails(
                FL401OtherProceedingDetails.builder().hasPrevOrOngoingOtherProceeding(YesNoDontKnow.yes).build())
            .build();

        Map<String, Object> expected = Map.of(PrlAppsConstants.PREVIOUS_OR_ONGOING_PROCEEDINGS, "Yes");
        Map<String, Object> result = applicationsTabService.getFL401OtherProceedingsTable(caseData);
        assertEquals(expected, result);
    }

    @Test
    public void testGetFl401OtherProceedingsDetailsTable() {

        FL401Proceedings proceedingDetails = FL401Proceedings.builder()
            .anyOtherDetails("test")
            .nameOfCourt("court")
            .typeOfCase("")
            .build();
        Element<FL401Proceedings> wrappedProceedings = Element.<FL401Proceedings>builder()
            .value(proceedingDetails).build();
        List<Element<FL401Proceedings>> listOfProceedings = Collections.singletonList(wrappedProceedings);


        CaseData caseData = CaseData.builder()
            .fl401OtherProceedingDetails(FL401OtherProceedingDetails.builder()
                                             .hasPrevOrOngoingOtherProceeding(YesNoDontKnow.yes)
                                             .fl401OtherProceedings(listOfProceedings)
                                             .build())
            .build();
        List<Element<Fl401OtherProceedingsDetails>> result = applicationsTabService.getFl401OtherProceedingsDetailsTable(
            caseData);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetFl401OtherProceedingsDetailsTableEmptyOtherProceedings() {

        FL401Proceedings proceedingDetails = FL401Proceedings.builder()
            .anyOtherDetails("test")
            .nameOfCourt("court")
            .typeOfCase("")
            .build();
        Element<FL401Proceedings> wrappedProceedings = Element.<FL401Proceedings>builder()
            .value(proceedingDetails).build();
        List<Element<FL401Proceedings>> listOfProceedings = Collections.singletonList(wrappedProceedings);


        CaseData caseData = CaseData.builder()
            .fl401OtherProceedingDetails(FL401OtherProceedingDetails.builder()
                                             .hasPrevOrOngoingOtherProceeding(YesNoDontKnow.yes)
                                             //.fl401OtherProceedings(listOfProceedings)
                                             .build())
            .build();
        List<Element<Fl401OtherProceedingsDetails>> result = applicationsTabService.getFl401OtherProceedingsDetailsTable(
            caseData);
        assertFalse(result.isEmpty());
    }


    @Test
    public void testUpdateTabFL401() {

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .build();

        List<String> expected = List.of(
            "welshLanguageRequirementsTable", "homeDetailsTable", "applicantFamilyTable", "internationalElementTable",
            "respondentBehaviourTable", "relationshipToRespondentTable", "otherProceedingsTable", "fl401ApplicantTable",
            "fl401OtherProceedingsDetailsTable", "declarationTable", "fl401SolicitorDetailsTable",
            "fl401TypeOfApplicationTable",
            "attendingTheHearingTable", "withoutNoticeOrderTable", "fl401RespondentTable", "isHomeEntered"
        );
        Map<String, Object> result = applicationsTabService.updateTab(caseData);
        assertTrue(expected.containsAll(result.keySet()));
    }


    @Test
    public void testUpdateTabWithChildren() {
        List<Element<Child>> children = new ArrayList<>();
        Child child = Child.builder()
            .firstName("test")
            .lastName("test")
            .build();

        Element<Child> childElement = element(UUID.fromString("1accfb1e-2574-4084-b97e-1cd53fd14815"), child);
        children.add(childElement);

        caseDataWithParties = caseDataWithParties.toBuilder()
            .children(children)
            .build();

        when(objectMapper.convertValue(partyDetails, OtherPersonInTheCase.class))
            .thenReturn(OtherPersonInTheCase.builder().build());
        when(objectMapper.convertValue(caseDataWithParties, AllegationsOfHarmOrders.class))
            .thenReturn(allegationsOfHarmOrders);
        when(objectMapper.convertValue(caseDataWithParties, ChildAbductionDetails.class))
            .thenReturn(
                ChildAbductionDetails.builder().build());
        when(objectMapper.convertValue(caseDataWithParties, AllegationsOfHarmOtherConcerns.class))
            .thenReturn(AllegationsOfHarmOtherConcerns.builder().build());
        when(objectMapper.convertValue(partyDetails, Applicant.class))
            .thenReturn(Applicant.builder().gender("male").build());
        when(objectMapper.convertValue(partyDetails, Respondent.class))
            .thenReturn(Respondent.builder().build());

        assertNotNull(applicationsTabService.updateTab(caseDataWithParties));
    }

    private Element<PartyDetails> getElement(PartyDetails partyDetails) {
        return Element.<PartyDetails>builder()
            .id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
            .value(partyDetails)
            .build();
    }

}
