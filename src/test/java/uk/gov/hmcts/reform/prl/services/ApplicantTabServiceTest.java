package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.ProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Applicant;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.AttendingTheHearing;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.HearingUrgency;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.InternationalElement;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.LitigationCapacity;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.MiamExemptions;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.OtherProceedingsDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Respondent;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.TypeOfApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.AllegationsOfHarmOverview;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

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

        Element<ProceedingDetails> proceedingDetailsElement = Element.<ProceedingDetails>builder().value(proceedingDetails).build();

        caseDataWithParties = CaseData.builder()
            .applicants(partyList)
            .respondents(partyList)
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
        List<Element<Applicant>> expectedApplicantList =  Collections.singletonList(applicantElement);
        Applicant emptyApplicant = Applicant.builder().build();
        Element<Applicant> emptyApplicantElement = Element.<Applicant>builder().value(emptyApplicant).build();
        List<Element<Applicant>> emptyApplicantList =  Collections.singletonList(emptyApplicantElement);

        when(objectMapper.convertValue(partyDetails, Applicant.class)).thenReturn(applicant);
        assertEquals(expectedApplicantList, applicationsTabService.getApplicantsTable(caseDataWithParties));
        assertEquals(emptyApplicantList, applicationsTabService.getApplicantsTable(emptyCaseData));
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
        List<Element<Respondent>> expectedRespondentList =  Collections.singletonList(respondentElement);
        Respondent emptyRespondent = Respondent.builder().build();
        Element<Respondent> emptyRespondentElement = Element.<Respondent>builder().value(emptyRespondent).build();
        List<Element<Respondent>> emptyRespondentList =  Collections.singletonList(emptyRespondentElement);

        when(objectMapper.convertValue(partyDetails, Respondent.class)).thenReturn(respondent);
        assertEquals(expectedRespondentList, applicationsTabService.getRespondentsTable(caseDataWithParties));
        assertEquals(emptyRespondentList, applicationsTabService.getRespondentsTable(emptyCaseData));
    }

    @Test
    public void testDeclarationTable() {
        Map<String, Object> expectedDeclarationMap = new HashMap<>();
        String declarationText = "I understand that proceedings for contempt of court may be brought"
            + " against anyone who makes, or causes to be made, a false statement in a document verified"
            + " by a statement of truth without an honest belief in its truth. The applicant believes "
            + "that the facts stated in this form and any continuation sheets are true. [Solicitor Name] "
            + "is authorised by the applicant to sign this statement.";
        expectedDeclarationMap.put("declarationText", declarationText);
        expectedDeclarationMap.put("agreedBy", "<Solicitor name>");

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
        assertEquals(allegationsOfHarmOverviewMap,
                     applicationsTabService.getAllegationsOfHarmOverviewTable(caseDataWithParties));
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

//    @Test
//    public void testMiamExemptionsTableMapper() {
//        MiamExemptions miamExemptions = MiamExemptions.builder()
//            .reasonsForMiamExemption(MiamExemptionsChecklistEnum.domesticViolence.getDisplayedValue())
//            .domesticViolenceEvidence(MiamDomesticViolenceChecklistEnum
//                                          .miamDomesticViolenceChecklistEnum_Value_4.getDisplayedValue())
//            .otherGroundsEvidence(MiamOtherGroundsChecklistEnum
//                                      .miamOtherGroundsChecklistEnum_Value_2.getDisplayedValue())
//            .build();
//        Map<String, Object> miamExemptionsMap = Map.of(
//            "reasonsForMiamExemption", MiamExemptionsChecklistEnum.domesticViolence.getDisplayedValue(),
//            "domesticViolenceEvidence", MiamDomesticViolenceChecklistEnum
//                .miamDomesticViolenceChecklistEnum_Value_4.getDisplayedValue(),
//            "otherGroundsEvidence", MiamOtherGroundsChecklistEnum
//                .miamOtherGroundsChecklistEnum_Value_2.getDisplayedValue()
//        );
//
//        when(objectMapper.convertValue(miamExemptions, Map.class)).thenReturn(miamExemptionsMap);
//        assertEquals(miamExemptionsMap, applicationsTabService.getMiamExemptionsTable(caseDataWithParties));
//    }

    @Test
    public void testOtherProceedingsOverviewTableMapper() {
        Map<String, Object> completeOverviewMap = Map.of("previousOrOngoingProceedings",
                                                 caseDataWithParties.getPreviousOrOngoingProceedingsForChildren()
                                                     .getDisplayedValue());
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
        Element<OtherProceedingsDetails> otherProceedingsDetailsElement = Element.
            <OtherProceedingsDetails>builder().value(otherProceedingsDetails).build();

        OtherProceedingsDetails emptyProceeding = OtherProceedingsDetails.builder().build();
        Element<OtherProceedingsDetails> emptyProceedingElement = Element.
            <OtherProceedingsDetails>builder().value(emptyProceeding).build();

        assertEquals(Collections.singletonList(otherProceedingsDetailsElement),
                     applicationsTabService.getOtherProceedingsDetailsTable(caseDataWithParties));
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





}
