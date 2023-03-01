package uk.gov.hmcts.reform.prl.mapper.hearingrequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingRequestDataMapper {

    public void mapHearingData(HearingData hearingData, HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {

        boolean isHearingDynamicListItemsNullifyReq = (null != hearingDataPrePopulatedDynamicLists) ? false  : true;
        mapDynamicListItems(hearingData.getHearingTypes(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingTypes());
        mapDynamicListItems(hearingData.getConfirmedHearingDates(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingDates());
        mapDynamicListItems(hearingData.getHearingChannels(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        mapDynamicListItems(hearingData.getHearingChannelDynamicRadioList(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        mapDynamicListItems(hearingData.getHearingVideoChannels(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedVideoSubChannels());
        mapDynamicListItems(hearingData.getHearingTelephoneChannels(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedTelephoneSubChannels());
        mapDynamicListItems(hearingData.getCourtList(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedCourtLocations());
        mapDynamicListItems(hearingData.getHearingListedLinkedCases(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getHearingListedLinkedCases());
        mapOtherPartyHearingChannelsMapping(hearingData);
    }

    private void mapOtherPartyHearingChannelsMapping(HearingData hearingData) {
        mapDynamicListItems(hearingData.getApplicantHearingChannel(),hearingData.getHearingChannels());
        mapDynamicListItems(hearingData.getApplicantSolicitorHearingChannel(),hearingData.getHearingChannels());
        mapDynamicListItems(hearingData.getRespondentHearingChannel(),hearingData.getHearingChannels());
        mapDynamicListItems(hearingData.getRespondentSolicitorHearingChannel(),hearingData.getHearingChannels());
        mapDynamicListItems(hearingData.getCafcassHearingChannel(),hearingData.getHearingChannels());
        mapDynamicListItems(hearingData.getCafcassHearingChannel(),hearingData.getHearingChannels());
        mapDynamicListItems(hearingData.getCafcassCymruHearingChannel(),hearingData.getHearingChannels());
        mapDynamicListItems(hearingData.getLocalAuthorityHearingChannel(),hearingData.getHearingChannels());
    }

    private void mapDynamicListItems(DynamicList existingHearingDynamicList, DynamicList requiredHearingDynamicList) {
        if (null != existingHearingDynamicList) {
            existingHearingDynamicList.setListItems(null != requiredHearingDynamicList
                ? requiredHearingDynamicList.getListItems() : null);
        }
    }
}
