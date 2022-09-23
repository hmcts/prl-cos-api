package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.bothLiveWithAndSpendTimeWithOrder;
import static uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum.liveWithOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.prohibitedStepsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.specificIssueOrder;
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
}
