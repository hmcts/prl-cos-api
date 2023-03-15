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
        if (ofNullable(hearingDataPrePopulatedDynamicLists).isEmpty() && HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab
            .equals(ofNullable(hearingData.getHearingDateConfirmOptionEnum()).get())) {
            hearingData = setEmptyUnnecessaryValues(hearingData);
        }

        boolean isHearingDynamicListItemsNullifyReq = (null != hearingDataPrePopulatedDynamicLists) ? false  : true;
        mapHearingTypesListItems(hearingData,isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapConfirmedHearingDatesListItems(hearingData,isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapHearingChannelsListItems(hearingData,isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapHearingVideoChannelsListItems(hearingData, isHearingDynamicListItemsNullifyReq, hearingDataPrePopulatedDynamicLists);
        mapHearingTelephoneChannelsListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapHearingCourtLocationsListItems(hearingData, isHearingDynamicListItemsNullifyReq, hearingDataPrePopulatedDynamicLists);
        mapHearingListedLinkedCasesListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);

        mapApplicantHearingChannelListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapApplicantSolicitorListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapRespondentHearingChannelListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapRespondentSolicitorHearingChannelListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapCafcassHearingChannelListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapCafcassCymruHearingChannelListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapHearingListedLinkedCasesListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapLocalAuthorityHearingChannelListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);

        hearingData.setFillingFormRenderingInfo(CommonUtils.renderCollapsible());
        hearingData.setApplicantName(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? caseData.getApplicantName() : "");
        hearingData.setApplicantSolicitor(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                                    ? caseData.getApplicantsFL401().getRepresentativeFirstName()
                + "," + caseData.getApplicantsFL401().getRepresentativeLastName()  : "");
        hearingData.setRespondentName(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? caseData.getRespondentName() : "");
        hearingData.setRespondentSolicitor(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? "" : "");
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

    private void mapHearingChannelsListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                          HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getHearingChannels() && null != hearingData.getHearingChannels().getValue()) {
            mapDynamicListItems(hearingData.getHearingChannels(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setHearingChannels(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getHearingChannels(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
    }

    private void mapConfirmedHearingDatesListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                             HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getConfirmedHearingDates() && null != hearingData.getConfirmedHearingDates().getValue()) {
            mapDynamicListItems(hearingData.getConfirmedHearingDates(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingDates());
        } else {
            hearingData.setConfirmedHearingDates(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getConfirmedHearingDates(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingDates());
        }
    }

    private void mapApplicantSolicitorListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                                   HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getApplicantSolicitorHearingChannel() && null != hearingData.getApplicantSolicitorHearingChannel().getValue()) {
            mapDynamicListItems(hearingData.getApplicantSolicitorHearingChannel(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setApplicantSolicitorHearingChannel(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getApplicantSolicitorHearingChannel(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
    }


    private void mapApplicantHearingChannelListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                                HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getApplicantHearingChannel() && null != hearingData.getApplicantHearingChannel().getValue()) {
            mapDynamicListItems(hearingData.getApplicantHearingChannel(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setApplicantHearingChannel(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getApplicantHearingChannel(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
    }

    private void mapRespondentHearingChannelListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                                     HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getRespondentHearingChannel() && null != hearingData.getRespondentHearingChannel().getValue()) {
            mapDynamicListItems(hearingData.getRespondentHearingChannel(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setRespondentHearingChannel(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getRespondentHearingChannel(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
    }

    private void mapRespondentSolicitorHearingChannelListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                                      HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getRespondentSolicitorHearingChannel() && null != hearingData.getRespondentSolicitorHearingChannel().getValue()) {
            mapDynamicListItems(hearingData.getRespondentSolicitorHearingChannel(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setRespondentSolicitorHearingChannel(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getRespondentSolicitorHearingChannel(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
    }


    private void mapCafcassHearingChannelListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                                               HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getCafcassHearingChannel() && null != hearingData.getCafcassHearingChannel().getValue()) {
            mapDynamicListItems(hearingData.getCafcassHearingChannel(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setCafcassHearingChannel(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getCafcassHearingChannel(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
    }


    private void mapCafcassCymruHearingChannelListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                                   HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getCafcassCymruHearingChannel() && null != hearingData.getCafcassCymruHearingChannel().getValue()) {
            mapDynamicListItems(hearingData.getCafcassCymruHearingChannel(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setCafcassCymruHearingChannel(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getCafcassCymruHearingChannel(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
    }

    private void mapHearingListedLinkedCasesListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                                        HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getHearingListedLinkedCases() && null != hearingData.getHearingListedLinkedCases().getValue()) {
            mapDynamicListItems(hearingData.getHearingListedLinkedCases(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getHearingListedLinkedCases());
        } else {
            hearingData.setHearingListedLinkedCases(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getHearingListedLinkedCases(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getHearingListedLinkedCases());
        }
    }


    private void mapLocalAuthorityHearingChannelListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                                      HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getLocalAuthorityHearingChannel() && null != hearingData.getLocalAuthorityHearingChannel().getValue()) {
            mapDynamicListItems(hearingData.getLocalAuthorityHearingChannel(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setLocalAuthorityHearingChannel(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getLocalAuthorityHearingChannel(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
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
