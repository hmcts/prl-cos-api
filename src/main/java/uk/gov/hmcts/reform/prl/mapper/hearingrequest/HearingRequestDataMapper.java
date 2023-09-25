package uk.gov.hmcts.reform.prl.mapper.hearingrequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        mapLocalAuthorityHearingChannelListItems(hearingData, isHearingDynamicListItemsNullifyReq,hearingDataPrePopulatedDynamicLists);

        hearingData.setFillingFormRenderingInfo(CommonUtils.renderCollapsible());
        hearingData.setApplicantName(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? caseData.getApplicantName() : "");
        hearingData.setApplicantSolicitor(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                                              ? caseData.getApplicantsFL401().getRepresentativeFirstName()
            + "," + caseData.getApplicantsFL401().getRepresentativeLastName()  : "");
        hearingData.setRespondentName(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? caseData.getRespondentName() : "");
        hearingData.setRespondentSolicitor(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                                               ? caseData.getRespondentsFL401().getRepresentativeFirstName()
            + "," + caseData.getRespondentsFL401().getRepresentativeLastName()  : "");
        hearingData.setApplicantName(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? caseData.getApplicantName() : "");
        hearingData.setApplicantSolicitor(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                                              ? caseData.getApplicantsFL401().getRepresentativeFirstName()
            + "," + caseData.getApplicantsFL401().getRepresentativeLastName()  : "");

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
        if (null != hearingData.getApplicantSolicitorHearingChannel1() && null != hearingData.getApplicantSolicitorHearingChannel1().getValue()) {
            mapDynamicListItems(hearingData.getApplicantSolicitorHearingChannel1(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setApplicantSolicitorHearingChannel1(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getApplicantSolicitorHearingChannel1(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getApplicantSolicitorHearingChannel2() && null != hearingData.getApplicantSolicitorHearingChannel2().getValue()) {
            mapDynamicListItems(hearingData.getApplicantSolicitorHearingChannel2(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setApplicantSolicitorHearingChannel2(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getApplicantSolicitorHearingChannel2(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getApplicantSolicitorHearingChannel3() && null != hearingData.getApplicantSolicitorHearingChannel3().getValue()) {
            mapDynamicListItems(hearingData.getApplicantSolicitorHearingChannel3(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setApplicantSolicitorHearingChannel3(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getApplicantSolicitorHearingChannel3(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getApplicantSolicitorHearingChannel4() && null != hearingData.getApplicantSolicitorHearingChannel4().getValue()) {
            mapDynamicListItems(hearingData.getApplicantSolicitorHearingChannel4(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setApplicantSolicitorHearingChannel4(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getApplicantSolicitorHearingChannel4(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getApplicantSolicitorHearingChannel5() && null != hearingData.getApplicantSolicitorHearingChannel5().getValue()) {
            mapDynamicListItems(hearingData.getApplicantSolicitorHearingChannel5(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setApplicantSolicitorHearingChannel5(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getApplicantSolicitorHearingChannel5(),
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
        if (null != hearingData.getApplicantHearingChannel1() && null != hearingData.getApplicantHearingChannel1().getValue()) {
            mapDynamicListItems(hearingData.getApplicantHearingChannel1(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setApplicantHearingChannel1(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getApplicantHearingChannel1(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getApplicantHearingChannel2() && null != hearingData.getApplicantHearingChannel2().getValue()) {
            mapDynamicListItems(hearingData.getApplicantHearingChannel2(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setApplicantHearingChannel2(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getApplicantHearingChannel2(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getApplicantHearingChannel3() && null != hearingData.getApplicantHearingChannel3().getValue()) {
            mapDynamicListItems(hearingData.getApplicantHearingChannel3(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setApplicantHearingChannel3(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getApplicantHearingChannel3(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getApplicantHearingChannel4() && null != hearingData.getApplicantHearingChannel4().getValue()) {
            mapDynamicListItems(hearingData.getApplicantHearingChannel4(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setApplicantHearingChannel4(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getApplicantHearingChannel4(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getApplicantHearingChannel5() && null != hearingData.getApplicantHearingChannel5().getValue()) {
            mapDynamicListItems(hearingData.getApplicantHearingChannel5(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setApplicantHearingChannel5(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getApplicantHearingChannel5(),
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
        if (null != hearingData.getRespondentHearingChannel1() && null != hearingData.getRespondentHearingChannel1().getValue()) {
            mapDynamicListItems(hearingData.getRespondentHearingChannel1(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setRespondentHearingChannel1(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getRespondentHearingChannel1(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getRespondentHearingChannel2() && null != hearingData.getRespondentHearingChannel2().getValue()) {
            mapDynamicListItems(hearingData.getRespondentHearingChannel2(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setRespondentHearingChannel2(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getRespondentHearingChannel2(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getRespondentHearingChannel3() && null != hearingData.getRespondentHearingChannel3().getValue()) {
            mapDynamicListItems(hearingData.getRespondentHearingChannel3(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setRespondentHearingChannel3(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getRespondentHearingChannel3(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getRespondentHearingChannel4() && null != hearingData.getRespondentHearingChannel4().getValue()) {
            mapDynamicListItems(hearingData.getRespondentHearingChannel4(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setRespondentHearingChannel4(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getRespondentHearingChannel4(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getRespondentHearingChannel5() && null != hearingData.getRespondentHearingChannel5().getValue()) {
            mapDynamicListItems(hearingData.getRespondentHearingChannel5(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setRespondentHearingChannel5(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getRespondentHearingChannel5(),
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
        if (null != hearingData.getRespondentSolicitorHearingChannel1() && null != hearingData.getRespondentSolicitorHearingChannel1().getValue()) {
            mapDynamicListItems(hearingData.getRespondentSolicitorHearingChannel1(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setRespondentSolicitorHearingChannel1(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getRespondentSolicitorHearingChannel1(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getRespondentSolicitorHearingChannel2() && null != hearingData.getRespondentSolicitorHearingChannel2().getValue()) {
            mapDynamicListItems(hearingData.getRespondentSolicitorHearingChannel2(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setRespondentSolicitorHearingChannel2(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getRespondentSolicitorHearingChannel2(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getRespondentSolicitorHearingChannel3() && null != hearingData.getRespondentSolicitorHearingChannel3().getValue()) {
            mapDynamicListItems(hearingData.getRespondentSolicitorHearingChannel3(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setRespondentSolicitorHearingChannel3(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getRespondentSolicitorHearingChannel3(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getRespondentSolicitorHearingChannel4() && null != hearingData.getRespondentSolicitorHearingChannel4().getValue()) {
            mapDynamicListItems(hearingData.getRespondentSolicitorHearingChannel4(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setRespondentSolicitorHearingChannel4(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getRespondentSolicitorHearingChannel4(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        }
        if (null != hearingData.getRespondentSolicitorHearingChannel5() && null != hearingData.getRespondentSolicitorHearingChannel5().getValue()) {
            mapDynamicListItems(hearingData.getRespondentSolicitorHearingChannel5(),
                                isHearingDynamicListItemsNullifyReq ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels());
        } else {
            hearingData.setRespondentSolicitorHearingChannel5(DynamicList.builder().build());
            mapDynamicListItems(hearingData.getRespondentSolicitorHearingChannel5(),
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

}
