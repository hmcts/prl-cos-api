package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.ProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChildDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Applicant;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.AttendingTheHearing;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.ChildDetails;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.THIS_INFORMATION_IS_CONFIDENTIAL;

@RunWith(MockitoJUnitRunner.class)
public class ApplicantTabServiceTest {

    @InjectMocks
    ApplicationsTabService applicationsTabService;

    @Mock
    ObjectMapper objectMapper;

    CaseData caseDataWithParties;
    CaseData emptyCaseData;
    Address address;
    List<Element<PartyDetails>> partyList;
    PartyDetails partyDetails;
    Order order;
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
            //.forcedMarriageOrder(order)
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
            //type of application
            .ordersApplyingFor(Collections.singletonList(OrderTypeEnum.childArrangementsOrder))
            .typeOfChildArrangementsOrder(ChildArrangementOrderTypeEnum.spendTimeWithOrder)
            .natureOfOrder("Test nature of order")
            // hearing urgency
            .isCaseUrgent(YesOrNo.Yes)
            .caseUrgencyTimeAndReason("Test String")
            .doYouRequireAHearingWithReducedNotice(YesOrNo.No)
            //allegations of harm overview
            .allegationsOfHarmYesNo(YesOrNo.Yes)
            .allegationsOfHarmDomesticAbuseYesNo(YesOrNo.Yes)
            .allegationsOfHarmChildAbductionYesNo(YesOrNo.Yes)
            //miam
            .applicantAttendedMiam(YesOrNo.Yes)
            .claimingExemptionMiam(YesOrNo.No)
            .familyMediatorMiam(YesOrNo.Yes)
            .miamExemptionsChecklist(Collections.singletonList(MiamExemptionsChecklistEnum.domesticViolence))
            .miamDomesticViolenceChecklist(Collections.singletonList(
                MiamDomesticViolenceChecklistEnum.miamDomesticViolenceChecklistEnum_Value_4))
            .miamUrgencyReasonChecklist(Collections.singletonList(MiamUrgencyReasonChecklistEnum
                                                                      .miamUrgencyReasonChecklistEnum_Value_1))
            .miamChildProtectionConcernList(Collections.singletonList(MiamChildProtectionConcernChecklistEnum
                                                                          .MIAMChildProtectionConcernChecklistEnum_value_1))
            .miamPreviousAttendanceChecklist(MiamPreviousAttendanceChecklistEnum.miamPreviousAttendanceChecklistEnum_Value_1)
            .miamOtherGroundsChecklist(MiamOtherGroundsChecklistEnum.miamOtherGroundsChecklistEnum_Value_2)
            //other proceedings
            .previousOrOngoingProceedingsForChildren(YesNoDontKnow.yes)
            .existingProceedings(Collections.singletonList(proceedingDetailsElement))
            //international element
            .habitualResidentInOtherState(YesOrNo.Yes)
            .habitualResidentInOtherStateGiveReason("Example reason")
            .requestToForeignAuthority(YesOrNo.No)
            //attending the hearing
            .isWelshNeeded(YesOrNo.Yes)
            .isDisabilityPresent(YesOrNo.No)
            .adjustmentsRequired("Adjustments String")
            //litigation capacity
            .litigationCapacityFactors("Test")
            .litigationCapacityOtherFactors(YesOrNo.Yes)
            //allegations of harm
            .ordersNonMolestation(YesOrNo.Yes)
            .ordersNonMolestationCurrent(YesOrNo.Yes)
            .ordersNonMolestationDateIssued(LocalDate.of(1990, 8, 1))
            .ordersNonMolestationEndDate(LocalDate.of(1991, 8, 1))
            .ordersNonMolestationCourtName("Court name")
            .ordersOccupation(YesOrNo.Yes)
            .ordersOccupationCurrent(YesOrNo.Yes)
            .ordersOccupationDateIssued(LocalDate.of(1990, 8, 1))
            .ordersOccupationEndDate(LocalDate.of(1991, 8, 1))
            .ordersOccupationCourtName("Court name")
            .ordersForcedMarriageProtection(YesOrNo.Yes)
            .ordersForcedMarriageProtectionCurrent(YesOrNo.Yes)
            .ordersForcedMarriageProtectionDateIssued(LocalDate.of(1990, 8, 1))
            .ordersForcedMarriageProtectionEndDate(LocalDate.of(1991, 8, 1))
            .ordersForcedMarriageProtectionCourtName("Court name")
            .ordersRestraining(YesOrNo.Yes)
            .ordersRestrainingCurrent(YesOrNo.Yes)
            .ordersRestrainingDateIssued(LocalDate.of(1990, 8, 1))
            .ordersRestrainingEndDate(LocalDate.of(1991, 8, 1))
            .ordersRestrainingCourtName("Court name")
            .ordersOtherInjunctive(YesOrNo.Yes)
            .ordersOtherInjunctiveCurrent(YesOrNo.Yes)
            .ordersOtherInjunctiveDateIssued(LocalDate.of(1990, 8, 1))
            .ordersOtherInjunctiveEndDate(LocalDate.of(1991, 8, 1))
            .ordersOtherInjunctiveCourtName("Court name")
            .ordersUndertakingInPlace(YesOrNo.Yes)
            .ordersUndertakingInPlaceCurrent(YesOrNo.Yes)
            .ordersUndertakingInPlaceDateIssued(LocalDate.of(1990, 8, 1))
            .ordersUndertakingInPlaceEndDate(LocalDate.of(1991, 8, 1))
            .ordersUndertakingInPlaceCourtName("Court name")
            .physicalAbuseVictim(Collections.singletonList(ApplicantOrChildren.children))
            .emotionalAbuseVictim((Collections.singletonList(ApplicantOrChildren.children)))
            .psychologicalAbuseVictim((Collections.singletonList(ApplicantOrChildren.children)))
            .sexualAbuseVictim((Collections.singletonList(ApplicantOrChildren.children)))
            .financialAbuseVictim((Collections.singletonList(ApplicantOrChildren.children)))
            .previousAbductionThreats(YesOrNo.Yes)
            .previousAbductionThreatsDetails("Details")
            .abductionPreviousPoliceInvolvement(YesOrNo.No)
            .allegationsOfHarmOtherConcerns(YesOrNo.Yes)
            .allegationsOfHarmOtherConcernsDetails("Test String")
            .agreeChildUnsupervisedTime(YesOrNo.No)
            //welsh language requirements
            .welshLanguageRequirement(YesOrNo.Yes)
            .languageRequirementApplicationNeedWelsh(YesOrNo.No)
            //child details
            .childrenKnownToLocalAuthority(YesNoDontKnow.yes)
            .childrenKnownToLocalAuthorityTextArea("Test string")
            .childrenSubjectOfChildProtectionPlan(YesNoDontKnow.yes)
            //solicitor
            .solicitorName("Test Solicitor")
            .build();

        emptyCaseData = CaseData.builder().build();
    }

    @Test
    public void testApplicantTableMapper() {
        Applicant applicant = Applicant.builder()
            .firstName("First name")
            .lastName("Last name")
            .dateOfBirth(LocalDate.of(1989, 11, 30))
            .gender("Male") //the new POJOs use strings as the enums are causing errors
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .build();

        Element<Applicant> applicantElement = Element.<Applicant>builder().value(applicant).build();
        List<Element<Applicant>> expectedApplicantList = Collections.singletonList(applicantElement);
        Applicant emptyApplicant = Applicant.builder().build();
        Element<Applicant> emptyApplicantElement = Element.<Applicant>builder().value(emptyApplicant).build();
        List<Element<Applicant>> emptyApplicantList = Collections.singletonList(emptyApplicantElement);

        when(objectMapper.convertValue(partyDetails, Applicant.class)).thenReturn(applicant);
        assertEquals(expectedApplicantList, applicationsTabService.getApplicantsTable(caseDataWithParties));
        assertEquals(emptyApplicantList, applicationsTabService.getApplicantsTable(emptyCaseData));
    }

    @Test
    public void testApplicantsConfidentialDetails() {
        PartyDetails partyDetails = PartyDetails.builder()
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
            .isPhoneNumberConfidential(YesOrNo.Yes)
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
        Assert.assertEquals(applicantsTable, expectedApplicantList);
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


        assertEquals(1, List.of(partyDetails1).size());
        assertEquals(
            List.of(expectedPartDetails),
            applicationsTabService.maskConfidentialDetails(List.of(partyDetails1))
        );
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
            .gender("Male") //the new POJOs use strings as the enums are causing errors
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .build();

        Element<Respondent> respondentElement = Element.<Respondent>builder().value(respondent).build();
        List<Element<Respondent>> expectedRespondentList = Collections.singletonList(respondentElement);
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
            .build();
        Map<String, Object> typeOfApplicationMap = Map.of(
            "ordersApplyingFor", "Child Arrangements Order",
            "typeOfChildArrangementsOrder", "Spend time with order",
            "natureOfOrder", "Test nature of order"
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
            "previousOrOngoingProceedings",
            caseDataWithParties.getPreviousOrOngoingProceedingsForChildren()
                .getDisplayedValue()
        );
        Map<String, Object> emptyOverviewMap = Map.of("previousOrOngoingProceedings", "");

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
            .gender("Male") //the new POJOs use strings as the enums are causing errors
            .address(address)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("test@test.com")
            .build();

        Element<OtherPersonInTheCase> otherPersonElement = Element.<OtherPersonInTheCase>builder()
            .value(otherPerson).build();
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

}
