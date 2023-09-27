package uk.gov.hmcts.reform.prl.mapper.hearingrequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingRequestDataMapper {

    public void mapHearingData(HearingData hearingData, HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists, CaseData caseData) {
        boolean isHearingDynamicListItemsNullifyReq = (null != hearingDataPrePopulatedDynamicLists) ? Boolean.FALSE  : Boolean.TRUE;

        mapHearingTypesListItems(hearingData,isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapConfirmedHearingDatesListItems(hearingData,isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapHearingChannelsListItems(hearingData,isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapHearingCourtLocationsListItems(hearingData, isHearingDynamicListItemsNullifyReq, hearingDataPrePopulatedDynamicLists);
        mapHearingListedLinkedCasesListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        hearingData.setApplicantHearingChannel(mapHearingChannel(hearingData.getApplicantHearingChannel(),
                                                                 isHearingDynamicListItemsNullifyReq,
                                                                 hearingDataPrePopulatedDynamicLists));
        hearingData.setApplicantSolicitorHearingChannel(mapHearingChannel(hearingData.getApplicantSolicitorHearingChannel(),
                                                                          isHearingDynamicListItemsNullifyReq,
                                                                          hearingDataPrePopulatedDynamicLists));
        hearingData.setRespondentHearingChannel(mapHearingChannel(hearingData.getRespondentHearingChannel(),
                                                                  isHearingDynamicListItemsNullifyReq,
                                                                  hearingDataPrePopulatedDynamicLists));
        hearingData.setRespondentSolicitorHearingChannel(mapHearingChannel(hearingData.getRespondentSolicitorHearingChannel(),
                                                             isHearingDynamicListItemsNullifyReq,
                                                             hearingDataPrePopulatedDynamicLists));
        mapCafcassHearingChannelListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapCafcassCymruHearingChannelListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapLocalAuthorityHearingChannelListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);

        hearingData.setFillingFormRenderingInfo(CommonUtils.renderCollapsible());
        boolean isFL401Case = FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication());
        hearingData.setApplicantName(isFL401Case ? caseData.getApplicantName() : "");
        hearingData.setApplicantSolicitor(isFL401Case && null != caseData.getApplicantsFL401()
                                              ? caseData.getApplicantsFL401().getRepresentativeFirstName()
            + "," + caseData.getApplicantsFL401().getRepresentativeLastName()  : "");
        hearingData.setRespondentName(isFL401Case ? caseData.getRespondentName() : "");
        hearingData.setRespondentSolicitor(isFL401Case && null != caseData.getRespondentsFL401()
                                               ? caseData.getRespondentsFL401().getRepresentativeFirstName()
            + "," + caseData.getRespondentsFL401().getRepresentativeLastName()  : "");

        //PRL-4301 - map party & solicitor hearing channels
        hearingData.setApplicantHearingChannel1(mapHearingChannel(hearingData.getApplicantHearingChannel1(),
                                                                  isHearingDynamicListItemsNullifyReq,
                                                                  hearingDataPrePopulatedDynamicLists));
        hearingData.setApplicantHearingChannel2(mapHearingChannel(hearingData.getApplicantHearingChannel2(),
                                                                  isHearingDynamicListItemsNullifyReq,
                                                                  hearingDataPrePopulatedDynamicLists));
        hearingData.setApplicantHearingChannel3(mapHearingChannel(hearingData.getApplicantHearingChannel3(),
                                                                  isHearingDynamicListItemsNullifyReq,
                                                                  hearingDataPrePopulatedDynamicLists));
        hearingData.setApplicantHearingChannel4(mapHearingChannel(hearingData.getApplicantHearingChannel4(),
                                                                  isHearingDynamicListItemsNullifyReq,
                                                                  hearingDataPrePopulatedDynamicLists));
        hearingData.setApplicantHearingChannel5(mapHearingChannel(hearingData.getApplicantHearingChannel5(),
                                                                  isHearingDynamicListItemsNullifyReq,
                                                                  hearingDataPrePopulatedDynamicLists));
        hearingData.setApplicantSolicitorHearingChannel1(mapHearingChannel(hearingData.getApplicantSolicitorHearingChannel1(),
                                                                           isHearingDynamicListItemsNullifyReq,
                                                                           hearingDataPrePopulatedDynamicLists));
        hearingData.setApplicantSolicitorHearingChannel2(mapHearingChannel(hearingData.getApplicantSolicitorHearingChannel2(),
                                                                           isHearingDynamicListItemsNullifyReq,
                                                                           hearingDataPrePopulatedDynamicLists));
        hearingData.setApplicantSolicitorHearingChannel3(mapHearingChannel(hearingData.getApplicantSolicitorHearingChannel3(),
                                                                           isHearingDynamicListItemsNullifyReq,
                                                                           hearingDataPrePopulatedDynamicLists));
        hearingData.setApplicantSolicitorHearingChannel4(mapHearingChannel(hearingData.getApplicantSolicitorHearingChannel4(),
                                                                           isHearingDynamicListItemsNullifyReq,
                                                                           hearingDataPrePopulatedDynamicLists));
        hearingData.setApplicantSolicitorHearingChannel5(mapHearingChannel(hearingData.getApplicantSolicitorHearingChannel5(),
                                                                           isHearingDynamicListItemsNullifyReq,
                                                                           hearingDataPrePopulatedDynamicLists));
        hearingData.setRespondentHearingChannel1(mapHearingChannel(hearingData.getRespondentHearingChannel1(),
                                                                   isHearingDynamicListItemsNullifyReq,
                                                                   hearingDataPrePopulatedDynamicLists));
        hearingData.setRespondentHearingChannel2(mapHearingChannel(hearingData.getRespondentHearingChannel2(),
                                                                   isHearingDynamicListItemsNullifyReq,
                                                                   hearingDataPrePopulatedDynamicLists));
        hearingData.setRespondentHearingChannel3(mapHearingChannel(hearingData.getRespondentHearingChannel3(),
                                                                   isHearingDynamicListItemsNullifyReq,
                                                                   hearingDataPrePopulatedDynamicLists));
        hearingData.setRespondentHearingChannel4(mapHearingChannel(hearingData.getRespondentHearingChannel4(),
                                                                   isHearingDynamicListItemsNullifyReq,
                                                                   hearingDataPrePopulatedDynamicLists));
        hearingData.setRespondentHearingChannel5(mapHearingChannel(hearingData.getRespondentHearingChannel5(),
                                                                   isHearingDynamicListItemsNullifyReq,
                                                                   hearingDataPrePopulatedDynamicLists));
        hearingData.setRespondentSolicitorHearingChannel1(mapHearingChannel(hearingData.getRespondentSolicitorHearingChannel1(),
                                                                            isHearingDynamicListItemsNullifyReq,
                                                                            hearingDataPrePopulatedDynamicLists));
        hearingData.setRespondentSolicitorHearingChannel2(mapHearingChannel(hearingData.getRespondentSolicitorHearingChannel2(),
                                                                            isHearingDynamicListItemsNullifyReq,
                                                                            hearingDataPrePopulatedDynamicLists));
        hearingData.setRespondentSolicitorHearingChannel3(mapHearingChannel(hearingData.getRespondentSolicitorHearingChannel3(),
                                                                            isHearingDynamicListItemsNullifyReq,
                                                                            hearingDataPrePopulatedDynamicLists));
        hearingData.setRespondentSolicitorHearingChannel4(mapHearingChannel(hearingData.getRespondentSolicitorHearingChannel4(),
                                                                            isHearingDynamicListItemsNullifyReq,
                                                                            hearingDataPrePopulatedDynamicLists));
        hearingData.setRespondentSolicitorHearingChannel5(mapHearingChannel(hearingData.getRespondentSolicitorHearingChannel5(),
                                                                            isHearingDynamicListItemsNullifyReq,
                                                                            hearingDataPrePopulatedDynamicLists));
    }

    private DynamicList mapHearingChannel(DynamicList hearingChannel,
                                                    boolean isHearingDynamicListItemsNullifyReq,
                                                    HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingChannel) {
            if (null != hearingChannel.getValue()) {
                mapDynamicListItems(
                    hearingChannel,
                    isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels()
                );
            } else if (CollectionUtils.isNotEmpty(hearingChannel.getListItems())) {
                hearingChannel = DynamicList.builder().build();
                mapDynamicListItems(
                    hearingChannel,
                    isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels()
                );
            }
        }
        return hearingChannel;
    }


    private void mapHearingTypesListItems(HearingData hearingData, boolean isHearingDynamicListItemsNullifyReq,
                                          HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingData.getHearingTypes() && null != hearingData.getHearingTypes().getValue()) {
            mapDynamicListItems(hearingData.getHearingTypes(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingTypes());
        } else {
            hearingData.setHearingTypes(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getHearingTypes(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingTypes());
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

    private void mapDynamicListItems(DynamicList existingHearingDynamicList, DynamicList requiredHearingDynamicList) {
        if (null != existingHearingDynamicList) {
            existingHearingDynamicList.setListItems(null != requiredHearingDynamicList
                                                        ? requiredHearingDynamicList.getListItems() : null);
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

}
