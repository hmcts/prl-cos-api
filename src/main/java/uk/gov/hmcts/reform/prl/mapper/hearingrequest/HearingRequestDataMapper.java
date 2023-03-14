package uk.gov.hmcts.reform.prl.mapper.hearingrequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingRequestDataMapper {

    public void mapHearingData(HearingData hearingData, HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        log.info("Inside Request mapper hearing data****hearingDataPrePopulatedDynamicLists  {}", hearingDataPrePopulatedDynamicLists);
        boolean isHearingDynamicListItemsNullifyReq = (null != hearingDataPrePopulatedDynamicLists) ? false  : true;
        mapHearingTypesListItems(hearingData,isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapDynamicListItems(hearingData.getConfirmedHearingDates(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingDates());
        mapDynamicListItems(hearingData.getHearingChannels(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        mapHearingVideoChannelsListItems(hearingData,
            isHearingDynamicListItemsNullifyReq, hearingDataPrePopulatedDynamicLists);
        mapHearingTelephoneChannelsListItems(hearingData,
            isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapHearingCourtLocationsListItems(hearingData,
            isHearingDynamicListItemsNullifyReq, hearingDataPrePopulatedDynamicLists);
        mapDynamicListItems(hearingData.getHearingListedLinkedCases(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getHearingListedLinkedCases());
        mapOtherPartyHearingChannelsMapping(hearingData, isHearingDynamicListItemsNullifyReq
            ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        log.info("Inside Request mapper hearing data****  {}", hearingData);
    }


    private void mapHearingTypesListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                          HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getHearingTypes() && null != hearingData.getHearingTypes().getValue()) {
            log.info("Inside Request mapper mapHearingTypesListItems() before set ListItems to getHearingTypes in if ****  {}",
                hearingData.getHearingTypes());
            mapDynamicListItems(hearingData.getHearingTypes(),
                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingTypes());
        } else {
            log.info("Inside Request mapper mapHearingTypesListItems() before set ListItems to getHearingTypes in else****  {}",
                hearingData.getHearingTypes());
            hearingData.setHearingTypes(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getHearingTypes(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingTypes());
        }
    }


    private void mapHearingTelephoneChannelsListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                          HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getHearingTelephoneChannels() && null != hearingData.getHearingTelephoneChannels().getValue()) {
            mapDynamicListItems(hearingData.getHearingTelephoneChannels(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedTelephoneSubChannels());
        } else {
            hearingData.setHearingTelephoneChannels(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getHearingTelephoneChannels(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedTelephoneSubChannels());
        }
    }


    private void mapHearingVideoChannelsListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                          HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getHearingVideoChannels() && null != hearingData.getHearingVideoChannels().getValue()) {
            mapDynamicListItems(hearingData.getHearingVideoChannels(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedVideoSubChannels());
        } else {
            hearingData.setHearingVideoChannels(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getHearingVideoChannels(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedVideoSubChannels());
        }
    }


    private void mapHearingCourtLocationsListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                                  HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getCourtList() && null != hearingData.getCourtList().getValue()) {
            mapDynamicListItems(hearingData.getCourtList(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedCourtLocations());
        } else {
            hearingData.setCourtList(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getCourtList(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedCourtLocations());
        }
    }


    private void mapOtherPartyHearingChannelsMapping(HearingData hearingData, DynamicList retrievedHearingChannels) {
        mapDynamicListItems(hearingData.getApplicantHearingChannel(),retrievedHearingChannels);
        mapDynamicListItems(hearingData.getApplicantSolicitorHearingChannel(),retrievedHearingChannels);
        mapDynamicListItems(hearingData.getRespondentHearingChannel(),retrievedHearingChannels);
        mapDynamicListItems(hearingData.getRespondentSolicitorHearingChannel(),retrievedHearingChannels);
        mapDynamicListItems(hearingData.getCafcassHearingChannel(),retrievedHearingChannels);
        mapDynamicListItems(hearingData.getCafcassHearingChannel(),retrievedHearingChannels);
        mapDynamicListItems(hearingData.getCafcassCymruHearingChannel(),retrievedHearingChannels);
        mapDynamicListItems(hearingData.getLocalAuthorityHearingChannel(),retrievedHearingChannels);
        hearingData.setFillingFormRenderingInfo(CommonUtils.renderCollapsible());
    }

    private void mapDynamicListItems(DynamicList existingHearingDynamicList, DynamicList requiredHearingDynamicList) {
        if (null != existingHearingDynamicList) {
            log.info("Inside Request mapper mapDynamicListItems() before set ListItems****  {}", existingHearingDynamicList);
            existingHearingDynamicList.setListItems(null != requiredHearingDynamicList
                ? requiredHearingDynamicList.getListItems() : null);
            log.info("Inside Request mapper mapDynamicListItems() before set ListItems****  {}", existingHearingDynamicList);
        }
    }
}
