package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.mapper.hearingrequest.HearingRequestDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingDataPrePopulatedDynamicLists;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGCHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_N;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_Y;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TELEPHONEPLATFORM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TELEPHONESUBCHANNELS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.VIDEOPLATFORM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.VIDEOSUBCHANNELS;

@Slf4j
@Service
public class HearingDataService {

    @Autowired
    RefDataUserService refDataUserService;

    @Autowired
    HearingService hearingService;

    @Autowired
    LocationRefDataService locationRefDataService;

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AllocatedJudgeService allocatedJudgeService;

    @Autowired
    HearingRequestDataMapper hearingRequestDataMapper;

    public HearingDataPrePopulatedDynamicLists populateHearingDynamicLists(String authorisation, String caseReferenceNumber, CaseData caseData) {
        Map<String, List<DynamicListElement>> hearingChannelsDetails = prePopulateHearingChannel(authorisation);
        return HearingDataPrePopulatedDynamicLists.builder().retrievedHearingTypes(getDynamicList(prePopulateHearingType(authorisation)))
            .retrievedHearingDates(getDynamicList(getHearingStartDate(authorisation, caseData)))
            .retrievedHearingChannels(getDynamicList(hearingChannelsDetails.get(HEARINGCHANNEL)))
            .retrievedVideoSubChannels(getDynamicList(hearingChannelsDetails.get(VIDEOSUBCHANNELS)))
            .retrievedTelephoneSubChannels(getDynamicList(hearingChannelsDetails.get(TELEPHONESUBCHANNELS)))
            .retrievedCourtLocations(getDynamicList(locationRefDataService.getCourtLocations(authorisation)))
            .hearingListedLinkedCases(getDynamicList(getLinkedCases(authorisation, caseData)))
            .build();
    }

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

    protected List<DynamicListElement> getLinkedCases(String authorisation, CaseData caseData) {
        try {
            log.info("Linked case method ", caseData.getId());
            List<DynamicListElement> dynamicListElements = new ArrayList<>();
            CaseLinkedRequest caseLinkedRequest = CaseLinkedRequest.caseLinkedRequestWith()
                .caseReference(String.valueOf(caseData.getId())).build();
            Optional<List<CaseLinkedData>> caseLinkedDataList = ofNullable(hearingService.getCaseLinkedData(authorisation, caseLinkedRequest));
            if (ofNullable(caseLinkedDataList).isPresent()) {
                for (CaseLinkedData caseLinkedData : caseLinkedDataList.get()) {
                    Hearings hearingDetails = hearingService.getHearings(authorisation, caseLinkedData.getCaseReference());
                    if (!ofNullable(hearingDetails).isEmpty() && !ofNullable(hearingDetails.getCaseHearings()).isEmpty()) {
                        List<CaseHearing> caseHearingsList = hearingDetails.getCaseHearings().stream()
                            .filter(caseHearing -> LISTED.equalsIgnoreCase(caseHearing.getHmcStatus())).collect(Collectors.toList());
                        if (ofNullable(caseHearingsList).isPresent()) {
                            dynamicListElements.add(DynamicListElement.builder().code(caseLinkedData.getCaseReference())
                                .label(caseLinkedData.getCaseName()).build());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info("Exception occured in Linked case method for hmc api calls ", caseData.getId());
        }
        //TODO: need to ensure this hardcoded values has to be removed while merging into release branch. Its added to test in preview/aat environment
        return List.of(DynamicListElement.builder().code(String.valueOf("1677767515750127")).label("CaseName-Test10").build());
    }


    public HearingData generateHearingData(HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists,CaseData caseData) {
        List<String> applicantNames  = getApplicantNameList(caseData);
        List<String> respondentNames = getRespondentNameList(caseData);
        List<String> applicantSolicitorNames = getApplicantSolicitorNameList(caseData);
        List<String> respondentSolicitorNames = getRespondentSolicitorNameList(caseData);
        int numberOfApplicant = applicantNames.size();
        int numberOfRespondents = respondentNames.size();
        int numberOfApplicantSolicitors = applicantSolicitorNames.size();
        int numberOfRespondentSolicitors  = respondentSolicitorNames.size();
        return HearingData.builder()
            .hearingTypes(hearingDataPrePopulatedDynamicLists.getRetrievedHearingTypes())
            .confirmedHearingDates(hearingDataPrePopulatedDynamicLists.getRetrievedHearingDates())
            .hearingChannels(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .hearingVideoChannels(hearingDataPrePopulatedDynamicLists.getRetrievedVideoSubChannels())
            .hearingTelephoneChannels(hearingDataPrePopulatedDynamicLists.getRetrievedTelephoneSubChannels())
            .courtList(hearingDataPrePopulatedDynamicLists.getRetrievedCourtLocations())
            .hearingListedLinkedCases(hearingDataPrePopulatedDynamicLists.getHearingListedLinkedCases())
            .applicantHearingChannel(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantHearingChannel1(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantHearingChannel2(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantHearingChannel3(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantHearingChannel4(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantHearingChannel5(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantSolicitorHearingChannel(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantSolicitorHearingChannel1(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantSolicitorHearingChannel2(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantSolicitorHearingChannel3(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantSolicitorHearingChannel4(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantSolicitorHearingChannel5(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentHearingChannel(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentHearingChannel1(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentHearingChannel2(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentHearingChannel3(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentHearingChannel4(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentHearingChannel5(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentSolicitorHearingChannel(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                                                   ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentSolicitorHearingChannel1(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentSolicitorHearingChannel2(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentSolicitorHearingChannel3(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentSolicitorHearingChannel4(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentSolicitorHearingChannel5(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .cafcassHearingChannel(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .cafcassCymruHearingChannel(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .localAuthorityHearingChannel(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantName(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? caseData.getApplicantName() : "")
            .applicantName1(0 < numberOfApplicant ? applicantNames.get(0) : "")
            .applicantName2(1 < numberOfApplicant ? applicantNames.get(1) : "")
            .applicantName3(2 < numberOfApplicant ? applicantNames.get(2) : "")
            .applicantName4(3 < numberOfApplicant ? applicantNames.get(3) : "")
            .applicantName5(4 < numberOfApplicant ? applicantNames.get(4) : "")
            .applicantSolicitor(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                                    ? caseData.getApplicantsFL401().getRepresentativeFirstName()
                + "," + caseData.getApplicantsFL401().getRepresentativeLastName()  : "")
            .applicantSolicitor1(0 < numberOfApplicantSolicitors ? applicantSolicitorNames.get(0) : "")
            .applicantSolicitor2(1 < numberOfApplicantSolicitors ? applicantSolicitorNames.get(1) : "")
            .applicantSolicitor3(2 < numberOfApplicantSolicitors ? applicantSolicitorNames.get(2) : "")
            .applicantSolicitor4(3 < numberOfApplicantSolicitors ? applicantSolicitorNames.get(3) : "")
            .applicantSolicitor5(4 < numberOfApplicantSolicitors ? applicantSolicitorNames.get(4) : "")
            .respondentName(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? caseData.getRespondentName() : "")
            .respondentName1(0 < numberOfRespondents ? respondentNames.get(0) : "")
            .respondentName2(1 < numberOfRespondents ? respondentNames.get(1) : "")
            .respondentName3(2 < numberOfRespondents ? respondentNames.get(2) : "")
            .respondentName4(3 < numberOfRespondents ? respondentNames.get(3) : "")
            .respondentName5(4 < numberOfRespondents ? respondentNames.get(4) : "")
            .respondentSolicitor(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? "" : "")
            .respondentSolicitor1(0 < numberOfRespondentSolicitors ? respondentSolicitorNames.get(0) : "")
            .respondentSolicitor2(1 < numberOfRespondentSolicitors ? respondentSolicitorNames.get(1) : "")
            .respondentSolicitor3(2 < numberOfRespondentSolicitors ? respondentSolicitorNames.get(2) : "")
            .respondentSolicitor4(3 < numberOfRespondentSolicitors ? respondentSolicitorNames.get(3) : "")
            .respondentSolicitor5(4 < numberOfRespondentSolicitors ? respondentSolicitorNames.get(4) : "")
            .fillingFormRenderingInfo(CommonUtils.renderCollapsible())
            .build();
    }

    public List<Element<HearingData>> getHearingData(List<Element<HearingData>> hearingDatas,
                                                     HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists) {
        hearingDatas.stream().parallel().forEach(hearingDataElement -> {
            HearingData hearingData = hearingDataElement.getValue();
            hearingRequestDataMapper.mapHearingData(hearingData,hearingDataPrePopulatedDynamicLists);
            Optional<JudicialUser> judgeDetailsSelected = ofNullable(hearingData.getHearingJudgeNameAndEmail());
            if (judgeDetailsSelected.isPresent() && !judgeDetailsSelected.get().getPersonalCode().isEmpty()) {
                Optional<List<JudicialUsersApiResponse>> judgeApiResponse = ofNullable(getJudgeDetails(hearingData.getHearingJudgeNameAndEmail()));
                if (!judgeApiResponse.get().isEmpty()) {
                    hearingData.setHearingJudgeLastName(judgeApiResponse.get().stream().findFirst().get().getSurname());
                    hearingData.setHearingJudgeEmailAddress(judgeApiResponse.get().stream().findFirst().get().getEmailId());
                    hearingData.setHearingJudgePersonalCode(judgeApiResponse.get().stream().findFirst().get().getPersonalCode());
                }
            }
            log.info("Inside hearing data service getHearingData method hearing data  {}", hearingData);
        });
        return hearingDatas;
    }

    private List<JudicialUsersApiResponse> getJudgeDetails(JudicialUser hearingJudgeNameAndEmail) {

        String[] judgePersonalCode = allocatedJudgeService.getPersonalCode(hearingJudgeNameAndEmail);
        return refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder()
            .personalCode(judgePersonalCode).build());

    }

    private DynamicList getDynamicList(List<DynamicListElement> listItems) {
        return DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(listItems).build();
    }


    private List<String> getApplicantNameList(CaseData caseData) {
        List<String> applicantList = new ArrayList<>();

        if (caseData.getApplicants() != null) {
            applicantList = caseData.getApplicants().stream()
                .map(Element::getValue)
                .map(PartyDetails::getLabelForDynamicList)
                .collect(Collectors.toList());
        }

        return applicantList;

    }

    private List<String> getRespondentNameList(CaseData caseData) {
        List<String> respondentList  =  new ArrayList<>();

        if (caseData.getRespondents() != null) {
            respondentList = caseData.getRespondents().stream()
                .map(Element::getValue)
                .map(PartyDetails::getLabelForDynamicList)
                .collect(Collectors.toList());
        }
        return respondentList;

    }

    private List<String> getApplicantSolicitorNameList(CaseData caseData) {
        List<String> applicantSolicitorList = new ArrayList<>();

        if (caseData.getApplicants() != null) {
            applicantSolicitorList = caseData.getApplicants().stream()
                .map(Element::getValue)
                .map(element -> element.getRepresentativeFirstName() + " " + element.getRepresentativeLastName())
                .collect(Collectors.toList());
        }
        return applicantSolicitorList;

    }

    private List<String> getRespondentSolicitorNameList(CaseData caseData) {
        List<String> respondentSolicitorList = new ArrayList<>();

        if (caseData.getRespondents() != null) {
            caseData.getRespondents().stream()
                .map(Element::getValue)
                .filter(partyDetails -> YesNoDontKnow.yes.equals(partyDetails.getDoTheyHaveLegalRepresentation()))
                .map(element -> element.getRepresentativeFirstName() + " " + element.getRepresentativeLastName())
                .collect(Collectors.toList());
        }

        return respondentSolicitorList;

    }

}
