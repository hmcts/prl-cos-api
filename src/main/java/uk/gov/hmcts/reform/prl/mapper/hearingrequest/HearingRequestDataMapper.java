package uk.gov.hmcts.reform.prl.mapper.hearingrequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingRequestDataMapper {

    public void mapHearingData(HearingData hearingData, HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists, CaseData caseData) {
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
        hearingData.setFillingFormRenderingInfo(CommonUtils.renderCollapsible());
        hearingData.setApplicantName(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? caseData.getApplicantName() : "");
        hearingData.setApplicantSolicitor(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                                    ? caseData.getApplicantsFL401().getRepresentativeFirstName()
                + "," + caseData.getApplicantsFL401().getRepresentativeLastName()  : "");
        hearingData.setRespondentName(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? caseData.getRespondentName() : "");
        hearingData.setRespondentSolicitor(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? "" : "");
        if (HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab
            .equals(ofNullable(hearingData.getHearingDateConfirmOptionEnum()).get())) {
            hearingData = setEmptyUnnecessaryValues(hearingData);
        }
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
    }

    private void mapDynamicListItems(DynamicList existingHearingDynamicList, DynamicList requiredHearingDynamicList) {
        if (null != existingHearingDynamicList) {
            log.info("Inside Request mapper mapDynamicListItems() before set ListItems****  {}", existingHearingDynamicList);
            existingHearingDynamicList.setListItems(null != requiredHearingDynamicList
                ? requiredHearingDynamicList.getListItems() : null);
            log.info("Inside Request mapper mapDynamicListItems() before set ListItems****  {}", existingHearingDynamicList);
        }
    }

    public HearingData setEmptyUnnecessaryValues(HearingData hearingData) {
        log.info("setEmptyUnnecessaryValues() before: {}",hearingData);
        HearingData hearingDataTemp = HearingData.builder()
                .hearingTypes(hearingData.getHearingTypes())
                .hearingDateConfirmOptionEnum(hearingData.getHearingDateConfirmOptionEnum())
                .confirmedHearingDates(hearingData.getConfirmedHearingDates())
                .additionalHearingDetails(ofNullable(hearingData.getAdditionalHearingDetails()).orElse(""))
                .instructionsForRemoteHearing(ofNullable(hearingData.getInstructionsForRemoteHearing()).orElse(""))
                .hearingChannels(hearingData.getHearingChannels())
                .hearingVideoChannels(hearingData.getHearingVideoChannels())
                .hearingTelephoneChannels(hearingData.getHearingTelephoneChannels())
                .courtList(hearingData.getCourtList())
                .localAuthorityHearingChannel(hearingData.getLocalAuthorityHearingChannel())
                .hearingListedLinkedCases(hearingData.getHearingListedLinkedCases())
                .applicantSolicitorHearingChannel(hearingData.getApplicantSolicitorHearingChannel())
                .respondentHearingChannel(hearingData.getRespondentHearingChannel())
                .respondentSolicitorHearingChannel(hearingData.getRespondentSolicitorHearingChannel())
                .cafcassHearingChannel(hearingData.getCafcassHearingChannel())
                .cafcassCymruHearingChannel(hearingData.getCafcassCymruHearingChannel())
                .applicantHearingChannel(hearingData.getApplicantHearingChannel())
                .hearingEstimatedDays(0)
                .hearingEstimatedMinutes(0)
                .hearingMustTakePlaceAtHour(0)
                .respondentName(hearingData.getRespondentName())
                .applicantName(hearingData.getApplicantName())
                .applicantSolicitor(hearingData.getApplicantSolicitor())
                .build();
        log.info("setEmptyUnnecessaryValues() after map: {}",hearingDataTemp);
        return hearingDataTemp;
    }
}
