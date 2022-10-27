package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.controllers.citizen.mapper.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.bothLiveWithAndSpendTimeWithOrder;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.liveWithOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.prohibitedStepsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.specificIssueOrder;
import static uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum.careOrder;
import static uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum.superviosionOrder;
import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataMapperTest {

    private static final String CASE_TYPE = "C100";

    @InjectMocks
    private CaseDataMapper caseDataMapper;

    private CaseData caseData;

    @Before
    public void setUp() {
        caseData = CaseData.builder()
                .id(1234567891234567L)
                .caseTypeOfApplication(CASE_TYPE)
                .c100RebuildInternationalElements("{\"internationalStart\":\"Yes\",\"internationalParents\":\"Yes\""
                        + ",\"provideDetailsParents\":\"Child'sParentslifeoutsideUK\",\"internationalJurisdiction\":\"Yes\""
                        + ",\"provideDetailsJurisdiction\":\"AnotherpersoncanapplyoutsideUK\",\"internationalRequest\":\"Yes\""
                        + ",\"provideDetailsRequest\":\"Anothercountryorjurisdictiondetails\"}")
                .c100RebuildHearingWithoutNotice("{\"hearingPart1\":\"No\",\"reasonsForApplicationWithoutNotice\":\"WNH Details\""
                        + "," + "\"doYouNeedAWithoutNoticeHearing\":\"Yes\",\"doYouNeedAWithoutNoticeHearingDetails\":"
                        + "\"Other people will do something\",\"doYouRequireAHearingWithReducedNotice\":\"Yes\","
                        + "\"doYouRequireAHearingWithReducedNoticeDetails\":\""
                        + "No time to give notice\"}")
                .c100RebuildTypeOfOrder("{\"courtOrder\":[\"whoChildLiveWith\",\"childTimeSpent\","
                        + "\"stopOtherPeopleDoingSomething\"" + ",\"resolveSpecificIssue\"],\"stopOtherPeopleDoingSomethingSubField"
                        + "\":[\"changeChildrenNameSurname\",\"allowMedicalTreatment\",\"takingChildOnHoliday\","
                        + "\"relocateChildrenDifferentUkArea\",\"relocateChildrenOutsideUk\"],\"resolveSpecificIssueSubField"
                        + "\":[\"specificHoliday\",\"whatSchoolChildrenWillGoTo\",\"religiousIssue\",\"changeChildrenNameSurnameA"
                        + "\",\"medicalTreatment\",\"relocateChildrenDifferentUkAreaA\",\"relocateChildrenOutsideUkA\","
                        + "\"returningChildrenToYourCare\"],\"shortStatement\":\"shortStatementFieldValue\"}")
                .c100RebuildOtherProceedings("{\n   \"op_childrenInvolvedCourtCase\": \"No\",\n\"op_courtOrderProtection\": "
                        + "\"Yes\",\n   \"op_courtProceedingsOrders\": [\n\"childArrangementOrder\",\n\"emergencyProtectionOrder"
                        + "\",\n\"supervisionOrder\",\n\"careOrder\",\n\"childAbductionOrder\",\n\"contactOrderForDivorce\","
                        + "\n\"contactOrderForAdoption\",\n\"childMaintenanceOrder\",\n\"financialOrder\",\n\"nonMolestationOrder"
                        + "\",\n\"occupationOrder\",\n\"forcedMarriageProtectionOrder\",\n\"restrainingOrder\","
                        + "\n\"otherInjuctionOrder\",\n\"undertakingOrder\",\n\"otherOrder\"\n],\n   \"op_otherProceedings\": "
                        + "{\n\"order\": {\n\"childArrangementOrders\": [\n{\n\"id\": \"1\",\n\"orderDetail\": \"CO1\","
                        + "\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},"
                        + "\n\"currentOrder\": \"Yes\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},"
                        + "\n\"orderCopy\": \"Yes\",\n\"orderDocument\": {\n\"id\": \"36c5d12c-22ed-4a62-8625-b8b102b8d4a2\","
                        + "\n\"url\": \"http://dm-store-aat.service.core-compute-aat.internal/documents/36c5d12c-22ed"
                        + "-4a62-8625-b8b102b8d4a2\",\n\"filename\": \"applicant__supervision_order__10102022.pdf\","
                        + "\n\"binaryUrl\": \"http://dm-store-aat.service.core-compute-aat.internal/documents"
                        + "/36c5d12c-22ed-4a62-8625-b8b102b8d4a2/binary\"\n}\n}\n],\n\"emergencyProtectionOrders\": [\n{\n\"id\": "
                        + "\"1\",\n\"orderDetail\": \"EO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"No\"\n}\n],\n\"supervisionOrders\": [\n{\n\"id\": \"1\","
                        + "\n\"orderDetail\": \"SO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": \"\","
                        + "\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": \"\","
                        + "\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"careOrders\": [\n{\n\"id\": \"1\",\n\"orderDetail\": "
                        + "\"CO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"2021\",\n\"month\": \"11\",\n\"day\": "
                        + "\"11\"\n},\n\"currentOrder\": \"Yes\",\n\"orderEndDate\": {\n\"year\": \"2021\",\n\"month\": \"12\","
                        + "\n\"day\": \"11\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"childAbductionOrders\": [\n{\n\"id\": \"1\","
                        + "\n\"orderDetail\": \"AO\",\n\"caseNo\": \"BS19F99999\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"contactOrdersForDivorce\": [\n{\n\"id\": "
                        + "\"1\",\n\"orderDetail\": \"COD\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"contactOrdersForAdoption\": [\n{\n\"id\": "
                        + "\"1\",\n\"orderDetail\": \"COA\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"childMaintenanceOrders\": [\n{\n\"id\": "
                        + "\"1\",\n\"orderDetail\": \"CMO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"financialOrders\": [\n{\n\"id\": "
                        + "\"1\",\n\"orderDetail\": \"FO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"nonMolestationOrders\": [\n{\n\"id\": "
                        + "\"1\",\n\"orderDetail\": \"NMO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"occupationOrders\": [\n{\n\"id\": \"1\","
                        + "\n\"orderDetail\": \"OO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": \"\","
                        + "\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": "
                        + "\"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],\n\"forcedMarriageProtectionOrders\": "
                        + "[\n{\n\"id\": \"1\",\n\"orderDetail\": \"FMPO\",\n\"caseNo\": \"\",\n\"orderDate\": {\n\"year\": "
                        + "\"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\",\n\"orderEndDate\": "
                        + "{\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"orderCopy\": \"\"\n}\n],"
                        + "\n\"restrainingOrders\": [\n{\n\"id\": \"1\",\n\"orderDetail\": \"RO\",\n\"caseNo\": \"\","
                        + "\n\"orderDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"currentOrder\": "
                        + "\"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"orderCopy\": "
                        + "\"\"\n}\n],\n\"otherInjuctionOrders\": [\n{\n\"id\": \"1\",\n\"orderDetail\": \"OIO\",\n\"caseNo\": "
                        + "\"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"currentOrder\": "
                        + "\"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"orderCopy\": "
                        + "\"\"\n}\n],\n\"undertakingOrders\": [\n{\n\"id\": \"1\",\n\"orderDetail\": \"UO\",\n\"caseNo\": "
                        + "\"\",\n\"orderDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"currentOrder\": "
                        + "\"\",\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"orderCopy\": "
                        + "\"\"\n}\n],\n\"otherOrders\": [\n{\n\"id\": \"1\",\n\"orderDetail\": \"OTO\",\n\"caseNo\": \"\","
                        + "\n\"orderDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"currentOrder\": \"\","
                        + "\n\"orderEndDate\": {\n\"year\": \"\",\n\"month\": \"\",\n\"day\": \"\"\n},\n\"orderCopy\": "
                        + "\"\"\n}\n]\n}\n}\n},\n   }\n}")
            .c100RebuildChildDetails("{\"cd_children\":[{\"id\":\"6c2505da-dae5-4541-9df5-5f4045f0ad4a\",\"firstName\":\""
                         + "c1\",\"lastName\":\"c11\",\"personalDetails\":{\"dateOfBirth\":{\"year\":\"2021\",\"month\":\""
                         + "10\",\"day\":\"10\"},\"isDateOfBirthUnknown\":\"\",\"approxDateOfBirth\":{\"day\":\"\",\"month"
                         + "\":\"\",\"year\":\"\"},\"gender\":\"Female\",\"otherGenderDetails\":\"\"},\"childMatters\":{\""
                         + "needsResolution\":[\"whoChildLiveWith\"]},\"parentialResponsibility\":{\"statement\":\"test11\""
                         + "}},{\"id\":\"ce9a93c4-8d7d-4aeb-8ac5-619de4d91a8c\",\"firstName\":\"c2\",\"lastName\":\"c22\","
                         + "\"personalDetails\":{\"dateOfBirth\":{\"year\":\"\",\"month\":\"\",\"day\":\"\"},\""
                         + "isDateOfBirthUnknown\":\"Yes\",\"approxDateOfBirth\":{\"year\":\"2000\",\"month\":\"10\",\"day\""
                         + ":\"20\"},\"gender\":\"Other\",\"otherGenderDetails\":\"TestOther\"},\"childMatters\":{"
                         + "\"needsResolution\":[\"childTimeSpent\"]},\"parentialResponsibility\":{\"statement\":\"test22"
                        + "\"}}],\"cd_childrenKnownToSocialServices\":\"Yes\",\"cd_childrenKnownToSocialServicesDetails\""
                        + ":\"Testchild\",\"cd_childrenSubjectOfProtectionPlan\":\"Dontknow\"}")
                .build();
    }

    @Test
    public void testCaseDataMapper() throws JsonProcessingException {

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData);

        //Then
        assertNotNull(updatedCaseData);
        assertEquals(CASE_TYPE, updatedCaseData.getCaseTypeOfApplication());
        assertEquals(List.of(childArrangementsOrder, prohibitedStepsOrder, specificIssueOrder),
                updatedCaseData.getOrdersApplyingFor());
        assertEquals(bothLiveWithAndSpendTimeWithOrder, updatedCaseData.getTypeOfChildArrangementsOrder());
        assertEquals("Changing the children's names or surname, Allowing medical treatment to be carried "
                        + "out on the children, Taking the children on holiday, Relocating the children to a different "
                        + "area in England and Wales, Relocating the children outside of England and Wales (including Scotland "
                        + "and Northern Ireland), A specific holiday or arrangement, What school the children will go to, "
                        + "A religious issue, Changing the children's names or surname, Medical treatment, Relocating the "
                        + "children to a different area in England and Wales, Relocating the children outside of England and "
                        + "Wales (including Scotland and Northern Ireland), "
                        + "Returning the children to your care, Short Statement Information - shortStatementFieldValue",
                updatedCaseData.getNatureOfOrder());
        assertEquals(No, updatedCaseData.getDoYouNeedAWithoutNoticeHearing());
        assertEquals("WNH Details, Details of without notice hearing because the other "
                        + "person or people may do something that would obstruct the order - Other people will do something",
                updatedCaseData.getReasonsForApplicationWithoutNotice());
        assertEquals(Yes, updatedCaseData.getDoYouRequireAHearingWithReducedNotice());
        assertEquals("No time to give notice", updatedCaseData.getSetOutReasonsBelow());
        assertEquals(Yes, updatedCaseData.getHabitualResidentInOtherState());
        assertEquals("Child'sParentslifeoutsideUK", updatedCaseData.getHabitualResidentInOtherStateGiveReason());
        assertEquals(Yes, updatedCaseData.getJurisdictionIssue());
        assertEquals("AnotherpersoncanapplyoutsideUK", updatedCaseData.getJurisdictionIssueGiveReason());
        assertEquals(Yes, updatedCaseData.getRequestToForeignAuthority());
        assertEquals("Anothercountryorjurisdictiondetails", updatedCaseData.getRequestToForeignAuthorityGiveReason());
        assertEquals(yes, updatedCaseData.getPreviousOrOngoingProceedingsForChildren());
        List<Element<ProceedingDetails>> proceedingDetails = updatedCaseData.getExistingProceedings();
        assertEquals(16, proceedingDetails.size());
        assertEquals(List.of(superviosionOrder), proceedingDetails.get(0).getValue().getTypeOfOrder());
        assertEquals(List.of(careOrder), proceedingDetails.get(1).getValue().getTypeOfOrder());
    }

    @Test
    public void testCaseDataMapperForOrderTypeExtraFields() throws JsonProcessingException {

        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildTypeOfOrder("{\"courtOrder\":[\"whoChildLiveWith\","
                        + "\"stopOtherPeopleDoingSomething\"" + ",\"resolveSpecificIssue\"],\"stopOtherPeopleDoingSomethingSubField"
                        + "\":[\"changeChildrenNameSurname\",\"allowMedicalTreatment\",\"takingChildOnHoliday\","
                        + "\"relocateChildrenDifferentUkArea\",\"relocateChildrenOutsideUk\"],\"resolveSpecificIssueSubField"
                        + "\":[\"specificHoliday\",\"whatSchoolChildrenWillGoTo\",\"religiousIssue\",\"changeChildrenNameSurnameA"
                        + "\",\"medicalTreatment\",\"relocateChildrenDifferentUkAreaA\",\"relocateChildrenOutsideUkA\","
                        + "\"returningChildrenToYourCare\"]}")
                .build();
        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData);
        assertEquals(CASE_TYPE, updatedCaseData.getCaseTypeOfApplication());
        assertEquals(List.of(childArrangementsOrder, prohibitedStepsOrder, specificIssueOrder),
                updatedCaseData.getOrdersApplyingFor());
        assertEquals(liveWithOrder, updatedCaseData.getTypeOfChildArrangementsOrder());
    }

    @Test
    public void testCaseDataMapperWhenNoOtherProceedingOrdersExist() throws JsonProcessingException {

        //Given
        CaseData caseData1 = caseData
                .toBuilder()
                .c100RebuildOtherProceedings("{\n   \"op_childrenInvolvedCourtCase\": \"No\",\n\"op_courtOrderProtection\": "
                        + "\"No\",\n   \"op_courtProceedingsOrders\": []\n}")
                .build();
        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNull(updatedCaseData.getExistingProceedings());
    }

    @Test
    public void testCaseDataMapperForChildDetail() throws JsonProcessingException {
        //Given
        CaseData caseData1 = caseData.toBuilder().c100RebuildChildDetails("{\"cd_children\":"
                  + "[{\"id\":\"6c2505da-dae5-4541-9df5-5f4045f0ad4a\",\"firstName\":\"c1\",\"lastName\":\"c11\","
                 + "\"personalDetails\":{\"dateOfBirth\":{\"year\":\"2021\",\"month\":\"10\",\"day\":\"10\"},\""
                 + "isDateOfBirthUnknown\":\"\",\"approxDateOfBirth\":{\"day\":\"\",\"month\":\"\",\"year\":\"\"},\""
                 + "gender\":\"Female\",\"otherGenderDetails\":\"\"},\"childMatters\":{\"needsResolution\":"
                 + "[\"whoChildLiveWith\"]},\"parentialResponsibility\":{\"statement\":\"test11\"}},{\"id\":\""
                 + "ce9a93c4-8d7d-4aeb-8ac5-619de4d91a8c\",\"firstName\":\"c2\",\"lastName\":\"c22\",\"personalDetails\""
                 + ":{\"dateOfBirth\":{\"year\":\"\",\"month\":\"\",\"day\":\"\"},\"isDateOfBirthUnknown\":\"Yes\","
                  + "\"approxDateOfBirth\":{\"year\":\"2000\",\"month\":\"10\",\"day\":\"20\"},\"gender\":\"Other\",\""
                 + "otherGenderDetails\":\"TestOther\"},\"childMatters\":{\"needsResolution\":[\"childTimeSpent\"]},"
             + "\"parentialResponsibility\":{\"statement\":\"test22\"}}],\"cd_childrenKnownToSocialServices\":\"Yes\","
                 + "\"cd_childrenKnownToSocialServicesDetails\":\"Testchild\",\"cd_childrenSubjectOfProtectionPlan\":\""
                  + "Dontknow\"}").build();

        //When
        CaseData updatedCaseData = caseDataMapper.buildUpdatedCaseData(caseData1);

        //Then
        assertNotNull(updatedCaseData.getChildren());
    }
}
