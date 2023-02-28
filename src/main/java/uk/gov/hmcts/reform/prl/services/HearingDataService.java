package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.CaseLinksElement;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caselink.CaseLink;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGCHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_N;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_Y;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TELEPHONEPLATFORM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TELEPHONESUBCHANNELS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.VIDEOPLATFORM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.VIDEOSUBCHANNELS;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getCaseData;

@Slf4j
@Service
public class HearingDataService {

    @Autowired
    RefDataUserService refDataUserService;

    @Autowired
    HearingService hearingService;

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AllocatedJudgeService allocatedJudgeService;

    public List<DynamicListElement> prePopulateHearingType(String authorisation) {
        try {
            log.info("Prepopulate HearingType call in HearingDataService");
            CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
                authorisation,
                HEARINGTYPE,
                IS_HEARINGCHILDREQUIRED_N
            );
            return refDataUserService.filterCategoryValuesByCategoryId(commonDataResponse, HEARINGTYPE);
        } catch (Exception e) {
            log.error("Category Values look up failed - " + e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());
    }

    public List<Element<HearingData>> mapHearingData(List<Element<HearingData>> hearingDatas, DynamicList hearingTypesDynamicList,
                                                     DynamicList hearingDatesDynamicList, DynamicList retrievedHearingChannels,
                                                     DynamicList retrievedRadioHearingChannels, DynamicList retrievedVideoSubChannels,
                                                     DynamicList retrievedTelephoneSubChannels, DynamicList retrievedCourtLocations,
                                                     DynamicList hearingListedLinkedCases, DynamicList applicantHearingChannel,
                                                     DynamicList applicantSolicitorHearingChannel, DynamicList respondentHearingChannel,
                                                     DynamicList respondentSolicitorHearingChannel, DynamicList cafcassHearingChannel,
                                                     DynamicList cafcassCymruHearingChannel, DynamicList localAuthorityHearingChannel
    ) {
        hearingDatas.stream().parallel().forEach(hearingDataElement -> {
            HearingData hearingData = hearingDataElement.getValue();
            hearingData.getHearingTypes().setListItems(null != hearingTypesDynamicList
                                                           ? hearingTypesDynamicList.getListItems() : null);
            if (hearingData.getConfirmedHearingDates() != null) {
                hearingData.getConfirmedHearingDates().setListItems(null != hearingDatesDynamicList
                                                                        ? hearingDatesDynamicList.getListItems() : null);
            }
            hearingData.getHearingChannels().setListItems(null != retrievedHearingChannels
                                                              ? retrievedHearingChannels.getListItems() : null);
            hearingData.getHearingChannelDynamicRadioList().setListItems(null != retrievedRadioHearingChannels
                                                                             ? retrievedRadioHearingChannels.getListItems() : null);
            hearingData.getHearingVideoChannels().setListItems(null != retrievedVideoSubChannels
                                                                   ? retrievedVideoSubChannels.getListItems() : null);
            hearingData.getHearingTelephoneChannels().setListItems(null != retrievedTelephoneSubChannels
                                                                       ? retrievedTelephoneSubChannels.getListItems() : null);
            hearingData.getCourtList().setListItems(null != retrievedCourtLocations
                                                        ? retrievedCourtLocations.getListItems() : null);
            hearingData.getHearingListedLinkedCases().setListItems(null != hearingListedLinkedCases
                                                                       ? hearingListedLinkedCases.getListItems() : null);
            hearingData.getApplicantHearingChannel().setListItems(null != applicantHearingChannel
                                                                      ? applicantHearingChannel.getListItems() : null);
            hearingData.getApplicantSolicitorHearingChannel().setListItems(null != applicantSolicitorHearingChannel
                                                                               ? applicantSolicitorHearingChannel.getListItems() : null);
            hearingData.getRespondentHearingChannel().setListItems(null != respondentHearingChannel
                                                                       ? respondentHearingChannel.getListItems() : null);
            hearingData.getRespondentSolicitorHearingChannel().setListItems(null != respondentSolicitorHearingChannel
                                                                                ? respondentSolicitorHearingChannel.getListItems() : null);
            hearingData.getCafcassHearingChannel().setListItems(null != cafcassHearingChannel
                                                                    ? cafcassHearingChannel.getListItems() : null);
            hearingData.getCafcassCymruHearingChannel().setListItems(null != cafcassCymruHearingChannel
                                                                         ? cafcassCymruHearingChannel.getListItems() : null);
            hearingData.getLocalAuthorityHearingChannel().setListItems(null != localAuthorityHearingChannel
                                                                           ? localAuthorityHearingChannel.getListItems() : null);
            if (hearingData.getHearingJudgeNameAndEmail() != null) {
                List<JudicialUsersApiResponse> judgeResponse = judgeMapping(hearingData.getHearingJudgeNameAndEmail());
                if (null != judgeResponse) {
                    hearingData.toBuilder().hearingJudgeLastName(judgeResponse.get(0).getSurname())
                        .hearingJudgeEmailAddress(judgeResponse.get(0).getEmailId())
                        .hearingJudgePersonalCode(judgeResponse.get(0).getPersonalCode()).build();
                }
            }


        });
        return hearingDatas;
    }

    private List<JudicialUsersApiResponse> judgeMapping(JudicialUser hearingJudgeNameAndEmail) {

        String[] judgePersonalCode = allocatedJudgeService.getPersonalCode(hearingJudgeNameAndEmail);
        return refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder()
                                                                .personalCode(judgePersonalCode).build());

    }


    public List<DynamicListElement> getHearingStartDate(String authorization, CaseData caseData) {
        try {
            String caseReferenceNumber = String.valueOf(caseData.getId());
            Hearings hearingDetails = hearingService.getHearings(authorization, caseReferenceNumber);
            log.info("Hearing Details from hmc for the case id:{}", caseReferenceNumber);
            if (null != hearingDetails && null != hearingDetails.getCaseHearings()) {
                return hearingDetails.getCaseHearings().stream()
                    .filter(caseHearing -> LISTED.equalsIgnoreCase(caseHearing.getHmcStatus()))
                    .map(CaseHearing::getHearingDaySchedule).collect(Collectors.toList()).stream()
                    .flatMap(Collection::stream)
                    .map(this::displayEntry)
                    .collect(Collectors.toList());

            }
        } catch (Exception e) {
            log.error("List of Hearing Start Date Values look up failed - " + e.getMessage(), e);
        }
        //TODO: need to ensure this hardcoded values has to be removed while merging into release branch. Its added to test in preview/aat environment
        return List.of(DynamicListElement.builder().code(String.valueOf(LocalDateTime.now())).label(String.valueOf(
            LocalDateTime.now())).build());
    }

    private DynamicListElement displayEntry(HearingDaySchedule hearingDaySchedule) {
        LocalDateTime hearingStartDateTime = hearingDaySchedule.getHearingStartDateTime();
        return DynamicListElement.builder().code(String.valueOf(hearingStartDateTime)).label(String.valueOf(
            hearingStartDateTime)).build();
    }


    public Map<String, List<DynamicListElement>> prePopulateHearingChannel(String authorisation) {
        try {
            log.info("Prepopulate HearingChannel call in HearingDataService");
            CommonDataResponse commonDataResponse = refDataUserService.retrieveCategoryValues(
                authorisation,
                HEARINGCHANNEL,
                IS_HEARINGCHILDREQUIRED_Y
            );
            Map<String, List<DynamicListElement>> values = new HashMap<>();
            values.put(HEARINGCHANNEL, refDataUserService.filterCategoryValuesByCategoryId(
                commonDataResponse, HEARINGCHANNEL));
            values.put(VIDEOSUBCHANNELS, refDataUserService.filterCategorySubValuesByCategoryId(
                commonDataResponse, VIDEOPLATFORM));
            values.put(TELEPHONESUBCHANNELS, refDataUserService.filterCategorySubValuesByCategoryId(
                commonDataResponse, TELEPHONEPLATFORM));
            log.info("***Hearing Channels***", values);
            return values;
        } catch (Exception e) {
            log.error("Category Values look up failed - " + e.getMessage(), e);
        }
        return null;

    }

    public List<DynamicListElement> getLinkedCase(String authorisation, String caseId) {
        try {
            log.info("Case Id {} for the linked case ", caseId);
            CaseDetails caseDetails = coreCaseDataApi.getCase(authorisation, authTokenGenerator.generate(),
                                                              caseId
            );
            return linkedCase(caseDetails);

        } catch (Exception e) {
            log.error("Linked Case Values look up failed - " + e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());

    }

    private List<DynamicListElement> linkedCase(CaseDetails caseDetails) {
        log.info("Linked case method ", caseDetails.getId());
        CaseData caseData = getCaseData(caseDetails, objectMapper);
        List<CaseLinksElement<CaseLink>> caseLinkDataList = caseData.getCaseLinks();
        if (caseLinkDataList != null) {
            return caseLinkDataList.stream().parallel()
                .map(response -> DynamicListElement.builder().code(response.getValue().getCaseReference())
                    .label(response.getValue().getCaseReference())
                    .build()).collect(Collectors.toList());
        }

        return List.of(DynamicListElement.builder().build());
    }


}
