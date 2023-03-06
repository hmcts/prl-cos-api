package uk.gov.hmcts.reform.prl.rpa.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.dio.DioBeforeAEnum;
import uk.gov.hmcts.reform.prl.mapper.hearingrequest.HearingRequestDataMapper;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


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
            .hearingEstimatedHours(5)
            .hearingEstimatedMinutes(40)
            .hearingEstimatedDays(15)
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .hearingJudgeNameAndEmail(judicialUser)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .build();
        hearingRequestDataMapper.mapHearingData(hearingData, hearingDataPrePopulatedDynamicLists);
        assertEquals(hearingData.getHearingTypes().getListItems().get(0).getCode(),"test");
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
            .hearingEstimatedHours(5)
            .hearingEstimatedMinutes(40)
            .hearingEstimatedDays(15)
            .allPartiesAttendHearingSameWayYesOrNo(YesOrNo.Yes)
            .hearingAuthority(DioBeforeAEnum.circuitJudge)
            .hearingJudgeNameAndEmail(judicialUser)
            .hearingJudgePersonalCode("test")
            .hearingJudgeLastName("test")
            .hearingJudgeEmailAddress("Test")
            .applicantName("Test")
            .build();
        hearingRequestDataMapper.mapHearingData(hearingData, hearingDataPrePopulatedDynamicLists);
        assertEquals(hearingData.getHearingTypes().getListItems().get(0).getCode(),"test");
    }

}
