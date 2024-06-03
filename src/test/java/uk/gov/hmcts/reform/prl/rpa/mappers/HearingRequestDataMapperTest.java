package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioBeforeAEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.mapper.hearingrequest.HearingRequestDataMapper;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@RunWith(MockitoJUnitRunner.class)
public class HearingRequestDataMapperTest {

    @InjectMocks
    HearingRequestDataMapper hearingRequestDataMapper;


    @Test
    public void testHearingUrgencyMapperWithAllFields() {
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("test")
            .label("test")
            .build();
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        dynamicListElements.add(dynamicListElement);
        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList3 = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        DynamicList dynamicList = DynamicList.builder()
            .listItems(dynamicListElements)
            .build();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            HearingDataPrePopulatedDynamicLists.builder()
                .retrievedHearingTypes(dynamicList)
                .hearingListedLinkedCases(dynamicList)
                .retrievedHearingDates(dynamicList)
                .retrievedHearingChannels(dynamicList3)
                .retrievedCourtLocations(dynamicList)
                .retrievedVideoSubChannels(dynamicList)
                .retrievedTelephoneSubChannels(dynamicList)
                .retrievedCourtLocations(dynamicList)
                .hearingListedLinkedCases(dynamicList)
                .build();
        LocalDateTime localDateTime = LocalDateTime.now();
        List<LocalDateTime> localDateTimes = new ArrayList<>();
        localDateTimes.add(localDateTime);
        JudicialUser judicialUser = JudicialUser.builder()
            .idamId("test")
            .personalCode("Test")
            .build();
        DynamicListElement dynamicListElement4 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList4 = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement4);
        DynamicList dynamicList1 = DynamicList.builder()
            .listItems(dynamicListElementsList4)
            .value(dynamicListElement4)
            .build();
        HearingData hearingData = HearingData.builder()
            .hearingTypes(dynamicList1)
            .confirmedHearingDates(dynamicList1)
            .hearingChannels(dynamicList1)
            .hearingVideoChannels(dynamicList1)
            .hearingTelephoneChannels(dynamicList1)
            .courtList(dynamicList1)
            .localAuthorityHearingChannel(dynamicList1)
            .hearingListedLinkedCases(dynamicList1)
            .applicantSolicitorHearingChannel(dynamicList1)
            .respondentHearingChannel(dynamicList1)
            .respondentSolicitorHearingChannel(dynamicList1)
            .cafcassHearingChannel(dynamicList1)
            .cafcassCymruHearingChannel(dynamicList1)
            .applicantHearingChannel(dynamicList1)
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours("5")
            .hearingEstimatedMinutes("40")
            .hearingEstimatedDays("15")
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .hearingJudgeNameAndEmail(judicialUser)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .applicantHearingChannel1(dynamicList1)
            .applicantSolicitorHearingChannel1(dynamicList1)
            .applicantHearingChannel2(dynamicList3)
            .applicantSolicitorHearingChannel2(null)
            .respondentHearingChannel1(dynamicList1)
            .respondentSolicitorHearingChannel1(dynamicList3)
            .build();
        PartyDetails applicant = PartyDetails.builder()
            .firstName("appF")
            .firstName("appL")
            .representativeFirstName("appSolF")
            .representativeLastName("appSolL")
            .build();
        PartyDetails respondent = PartyDetails.builder()
            .firstName("respF")
            .firstName("respL")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("respSolF")
            .representativeLastName("respSolL")
            .build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(applicant), element(applicant), element(applicant), element(applicant), element(applicant)))
            .respondents(List.of(element(respondent), element(respondent), element(respondent), element(respondent), element(respondent)))
            .createSelectOrderOptions(CreateSelectOrderOptionsEnum.amendDischargedVaried)
            .build();
        hearingRequestDataMapper.mapHearingData(hearingData, hearingDataPrePopulatedDynamicLists, caseData);
        assertEquals("INTER",hearingData.getHearingTypes().getListItems().get(0).getCode());
    }



    @Test
    public void testHearingUrgencyMapperWithNull() {
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("test")
            .label("test")
            .build();
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        dynamicListElements.add(dynamicListElement);
        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList3 = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        DynamicList dynamicList = DynamicList.builder()
            .listItems(dynamicListElements)
            .build();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists = null;
        LocalDateTime localDateTime = LocalDateTime.now();
        List<LocalDateTime> localDateTimes = new ArrayList<>();
        localDateTimes.add(localDateTime);
        JudicialUser judicialUser = JudicialUser.builder()
            .idamId("test")
            .personalCode("Test")
            .build();
        DynamicList dynamicList1 = DynamicList.builder()
            .build();
        HearingData hearingData = HearingData.builder()
            .hearingTypes(dynamicList1)
            .confirmedHearingDates(dynamicList1)
            .hearingChannels(dynamicList1)
            .hearingVideoChannels(dynamicList1)
            .hearingTelephoneChannels(dynamicList1)
            .courtList(dynamicList1)
            .localAuthorityHearingChannel(dynamicList1)
            .hearingListedLinkedCases(dynamicList1)
            .applicantSolicitorHearingChannel(dynamicList1)
            .respondentHearingChannel(dynamicList1)
            .respondentSolicitorHearingChannel(dynamicList1)
            .cafcassHearingChannel(dynamicList1)
            .cafcassCymruHearingChannel(dynamicList1)
            .applicantHearingChannel(dynamicList1)
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours("5")
            .hearingEstimatedMinutes("40")
            .hearingEstimatedDays("15")
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .hearingJudgeNameAndEmail(judicialUser)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .build();
        hearingRequestDataMapper.mapHearingData(hearingData, hearingDataPrePopulatedDynamicLists, caseData);
        assertNotNull(hearingData);
    }




    @Test
    public void testHearingUrgencyMapperWithAllFieldsWithHearingTypes() {
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("test")
            .label("test")
            .build();
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        dynamicListElements.add(dynamicListElement);
        DynamicListElement dynamicListElement2 = DynamicListElement.builder()
            .code("INTER")
            .label("In Person")
            .build();
        List<DynamicListElement> dynamicListElementsList = new ArrayList<>();
        dynamicListElementsList.add(dynamicListElement2);
        DynamicList dynamicList3 = DynamicList.builder()
            .listItems(dynamicListElementsList)
            .build();
        DynamicList dynamicList = DynamicList.builder()
            .listItems(dynamicListElements)
            .value(dynamicListElement)
            .build();
        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists =
            HearingDataPrePopulatedDynamicLists.builder()
                .retrievedHearingTypes(dynamicList)
                .hearingListedLinkedCases(dynamicList)
                .retrievedHearingDates(dynamicList)
                .retrievedHearingChannels(dynamicList)
                .retrievedCourtLocations(dynamicList)
                .retrievedVideoSubChannels(dynamicList)
                .retrievedTelephoneSubChannels(dynamicList)
                .retrievedCourtLocations(dynamicList)
                .hearingListedLinkedCases(dynamicList)
                .build();
        LocalDateTime localDateTime = LocalDateTime.now();
        List<LocalDateTime> localDateTimes = new ArrayList<>();
        localDateTimes.add(localDateTime);
        JudicialUser judicialUser = JudicialUser.builder()
            .idamId("test")
            .personalCode("Test")
            .build();
        DynamicList dynamicList1 = DynamicList.builder()
            .build();
        HearingData hearingData = HearingData.builder()
            //.hearingTypes(dynamicList)
            .confirmedHearingDates(dynamicList1)
            .hearingChannels(dynamicList1)
            //.hearingVideoChannels(dynamicList1)
            //.hearingTelephoneChannels(dynamicList1)
            //.courtList(dynamicList1)
            .localAuthorityHearingChannel(dynamicList1)
            .hearingListedLinkedCases(dynamicList1)
            .applicantSolicitorHearingChannel(dynamicList1)
            .respondentHearingChannel(dynamicList1)
            .respondentSolicitorHearingChannel(dynamicList1)
            .cafcassHearingChannel(dynamicList1)
            .cafcassCymruHearingChannel(dynamicList1)
            .applicantHearingChannel(dynamicList1)
            .hearingDateConfirmOptionEnum(HearingDateConfirmOptionEnum.dateReservedWithListAssit)
            .additionalHearingDetails("Test")
            .instructionsForRemoteHearing("Test")
            .hearingEstimatedHours("5")
            .hearingEstimatedMinutes("40")
            .hearingEstimatedDays("15")
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .hearingJudgeNameAndEmail(judicialUser)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .applicantHearingChannel1(DynamicList.builder().value(DynamicListElement.builder().build()).build())
            .build();

        PartyDetails partyDetails = PartyDetails.builder()
            .representativeFirstName("testF")
            .representativeLastName("testL")
            .build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .build();
        hearingRequestDataMapper.mapHearingData(hearingData, hearingDataPrePopulatedDynamicLists, caseData);
        assertEquals("test",hearingData.getHearingTypes().getListItems().get(0).getCode());
        assertEquals("test",hearingData.getCourtList().getListItems().get(0).getCode());
    }

}
