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
        log.info("Inside Request mapper hearing data****hearingDataPrePopulatedDynamicLists  {}", hearingDataPrePopulatedDynamicLists);
        boolean isHearingDynamicListItemsNullifyReq = (null != hearingDataPrePopulatedDynamicLists) ? false  : true;
        mapHearingTypesListItems(hearingData,isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapDynamicListItems(hearingData.getConfirmedHearingDates(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingDates());
        mapDynamicListItems(hearingData.getHearingChannels(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        mapDynamicListItems(hearingData.getHearingVideoChannels(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedVideoSubChannels());
        mapDynamicListItems(hearingData.getHearingTelephoneChannels(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedTelephoneSubChannels());
        mapDynamicListItems(hearingData.getCourtList(),
            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedCourtLocations());
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
            mapDynamicListItemsForHearingTypesNotSelected(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        }
    }

    private void mapDynamicListItemsForHearingTypesNotSelected(HearingData hearingData,boolean isHearingDynamicListItemsNullifyReq,
                                                               HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {

        //hearingData.builder().hearingTypes(DynamicList.builder().build()).build();
        hearingData.setHearingTypes(DynamicList.builder().build());
        mapDynamicListItems(hearingData.getHearingTypes(),
                            isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingTypes());
        /*   hearingData.toBuilder().hearingTypes(DynamicList.builder()
            .value(DynamicListElement.EMPTY).listItems(isHearingDynamicListItemsNullifyReq
                ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingTypes().getListItems()).build()).build();*/
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
