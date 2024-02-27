package uk.gov.hmcts.reform.prl.mapper.hearingrequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getApplicantSolicitorNameList;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getPartyNameList;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getRespondentSolicitorNameList;

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
        mapCafcassHearingChannelListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapCafcassCymruHearingChannelListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);
        mapLocalAuthorityHearingChannelListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);

        hearingData.setFillingFormRenderingInfo(CommonUtils.renderCollapsible());

        mapHearingDataForFL401Cases(hearingData, hearingDataPrePopulatedDynamicLists, caseData, isHearingDynamicListItemsNullifyReq);

        //PRL-4301 - map party & solicitor hearing channels
        mapHearingDataForC100Cases(hearingData, hearingDataPrePopulatedDynamicLists, caseData, isHearingDynamicListItemsNullifyReq);
    }

    private void mapHearingDataForC100Cases(HearingData hearingData,
                                            HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists,
                                            CaseData caseData,
                                            boolean isHearingDynamicListItemsNullifyReq) {
        boolean isC100Case = C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication());
        if (isC100Case) {
            int numberOfApplicant = getPartyNameList(caseData.getApplicants()).size();
            setHearingDataForApplicants(
                hearingData,
                hearingDataPrePopulatedDynamicLists,
                isHearingDynamicListItemsNullifyReq,
                numberOfApplicant
            );
            int numberOfApplicantSolicitors = getApplicantSolicitorNameList(caseData.getApplicants()).size();
            setHearingDataForSolicitors(
                hearingData,
                hearingDataPrePopulatedDynamicLists,
                isHearingDynamicListItemsNullifyReq,
                numberOfApplicantSolicitors
            );
            int numberOfRespondents = getPartyNameList(caseData.getRespondents()).size();
            setHearingDataForRespondents(
                hearingData,
                hearingDataPrePopulatedDynamicLists,
                isHearingDynamicListItemsNullifyReq,
                numberOfRespondents
            );
            int numberOfRespondentSolicitors  = getRespondentSolicitorNameList(caseData.getRespondents()).size();
            setHearingDataForRespondentSolicitor(
                hearingData,
                hearingDataPrePopulatedDynamicLists,
                isHearingDynamicListItemsNullifyReq,
                numberOfRespondentSolicitors
            );
        }
    }

    private void setHearingDataForRespondentSolicitor(HearingData hearingData,
                                                      HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists,
                                                      boolean isHearingDynamicListItemsNullifyReq,
                                                      int numberOfRespondentSolicitors) {
        hearingData.setRespondentSolicitorHearingChannel1(0 < numberOfRespondentSolicitors
                                                              ? mapHearingChannel(
            hearingData.getRespondentSolicitorHearingChannel1(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setRespondentSolicitorHearingChannel2(1 < numberOfRespondentSolicitors
                                                              ? mapHearingChannel(
            hearingData.getRespondentSolicitorHearingChannel2(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setRespondentSolicitorHearingChannel3(2 < numberOfRespondentSolicitors
                                                              ? mapHearingChannel(
            hearingData.getRespondentSolicitorHearingChannel3(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setRespondentSolicitorHearingChannel4(3 < numberOfRespondentSolicitors
                                                              ? mapHearingChannel(
            hearingData.getRespondentSolicitorHearingChannel4(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setRespondentSolicitorHearingChannel5(4 < numberOfRespondentSolicitors
                                                              ? mapHearingChannel(
            hearingData.getRespondentSolicitorHearingChannel5(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
    }

    private void setHearingDataForRespondents(HearingData hearingData,
                                              HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists,
                                              boolean isHearingDynamicListItemsNullifyReq,
                                              int numberOfRespondents) {
        hearingData.setRespondentHearingChannel1(0 < numberOfRespondents
                                                     ? mapHearingChannel(
            hearingData.getRespondentHearingChannel1(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setRespondentHearingChannel2(1 < numberOfRespondents
                                                     ? mapHearingChannel(
            hearingData.getRespondentHearingChannel2(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setRespondentHearingChannel3(2 < numberOfRespondents
                                                     ? mapHearingChannel(
            hearingData.getRespondentHearingChannel3(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setRespondentHearingChannel4(3 < numberOfRespondents
                                                     ? mapHearingChannel(
            hearingData.getRespondentHearingChannel4(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setRespondentHearingChannel5(4 < numberOfRespondents
                                                     ? mapHearingChannel(
            hearingData.getRespondentHearingChannel5(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
    }

    private void setHearingDataForSolicitors(HearingData hearingData,
                                             HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists,
                                             boolean isHearingDynamicListItemsNullifyReq,
                                             int numberOfApplicantSolicitors) {
        hearingData.setApplicantSolicitorHearingChannel1(0 < numberOfApplicantSolicitors
                                                             ? mapHearingChannel(
            hearingData.getApplicantSolicitorHearingChannel1(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setApplicantSolicitorHearingChannel2(1 < numberOfApplicantSolicitors
                                                             ? mapHearingChannel(
            hearingData.getApplicantSolicitorHearingChannel2(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setApplicantSolicitorHearingChannel3(2 < numberOfApplicantSolicitors
                                                             ? mapHearingChannel(
            hearingData.getApplicantSolicitorHearingChannel3(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setApplicantSolicitorHearingChannel4(3 < numberOfApplicantSolicitors
                                                             ? mapHearingChannel(
            hearingData.getApplicantSolicitorHearingChannel4(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setApplicantSolicitorHearingChannel5(4 < numberOfApplicantSolicitors
                                                             ? mapHearingChannel(
            hearingData.getApplicantSolicitorHearingChannel5(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
    }

    private void setHearingDataForApplicants(HearingData hearingData,
                                             HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists,
                                             boolean isHearingDynamicListItemsNullifyReq,
                                             int numberOfApplicant) {
        hearingData.setApplicantHearingChannel1(0 < numberOfApplicant
                                                    ? mapHearingChannel(
            hearingData.getApplicantHearingChannel1(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setApplicantHearingChannel2(1 < numberOfApplicant
                                                    ? mapHearingChannel(
            hearingData.getApplicantHearingChannel2(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setApplicantHearingChannel3(2 < numberOfApplicant
                                                    ? mapHearingChannel(
            hearingData.getApplicantHearingChannel3(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setApplicantHearingChannel4(3 < numberOfApplicant
                                                    ? mapHearingChannel(
            hearingData.getApplicantHearingChannel4(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
        hearingData.setApplicantHearingChannel5(4 < numberOfApplicant
                                                    ? mapHearingChannel(
            hearingData.getApplicantHearingChannel5(),
            isHearingDynamicListItemsNullifyReq,
            hearingDataPrePopulatedDynamicLists
        ) : null);
    }

    private void mapHearingDataForFL401Cases(HearingData hearingData,
                                             HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists,
                                             CaseData caseData,
                                             boolean isHearingDynamicListItemsNullifyReq) {
        boolean isFL401Case = FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication());
        if (isFL401Case) {
            hearingData.setApplicantHearingChannel(mapHearingChannel(
                hearingData.getApplicantHearingChannel(),
                isHearingDynamicListItemsNullifyReq,
                hearingDataPrePopulatedDynamicLists
            ));
            hearingData.setApplicantSolicitorHearingChannel(mapHearingChannel(
                hearingData.getApplicantSolicitorHearingChannel(),
                isHearingDynamicListItemsNullifyReq,
                hearingDataPrePopulatedDynamicLists
            ));
            hearingData.setRespondentHearingChannel(mapHearingChannel(
                hearingData.getRespondentHearingChannel(),
                isHearingDynamicListItemsNullifyReq,
                hearingDataPrePopulatedDynamicLists
            ));
            hearingData.setRespondentSolicitorHearingChannel(mapHearingChannel(
                hearingData.getRespondentSolicitorHearingChannel(),
                isHearingDynamicListItemsNullifyReq,
                hearingDataPrePopulatedDynamicLists
            ));
            hearingData.setApplicantName(ObjectUtils.isNotEmpty(caseData.getApplicantName()) ? caseData.getApplicantName() : "");
            hearingData.setApplicantSolicitor(null != caseData.getApplicantsFL401()
                                                  ? caseData.getApplicantsFL401().getRepresentativeFirstName()
                + "," + caseData.getApplicantsFL401().getRepresentativeLastName()  : "");
            hearingData.setRespondentName(ObjectUtils.isNotEmpty(caseData.getRespondentName()) ? caseData.getRespondentName() : "");
            hearingData.setRespondentSolicitor(null != caseData.getRespondentsFL401()
                                                   ? caseData.getRespondentsFL401().getRepresentativeFirstName()
                + "," + caseData.getRespondentsFL401().getRepresentativeLastName()  : "");
        }
    }

    private DynamicList mapHearingChannel(DynamicList hearingChannel,
                                                    boolean isHearingDynamicListItemsNullifyReq,
                                                    HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        if (null != hearingChannel && null != hearingChannel.getValue()) {
            mapDynamicListItems(
                hearingChannel,
                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels()
            );
        } else {
            hearingChannel = DynamicList.builder().build();
            mapDynamicListItems(
                hearingChannel,
                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels()
            );
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
            hearingData.setConfirmedHearingDates(DynamicList.builder().value(DynamicListElement.EMPTY).build());
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
