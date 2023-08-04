package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.enums.HearingChannelsEnum;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
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
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingDataFromTabToDocmosis;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Attendee;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALL_PARTIES_ATTEND_HEARING_IN_THE_SAME_WAY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANT_SOLICITOR_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_CYMRU_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONFIRMED_HEARING_DATES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_LIST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CUSTOM_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_CONFIRMED_IN_HEARINGS_TAB;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EARLIEST_HEARING_DATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FIRST_DATE_OF_THE_HEARING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGCHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_AUTHORITY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_CHANNELS_ENUM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_DATE_CONFIRM_OPTION_ENUM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_DATE_TIMES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_ESTIMATED_DAYS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_ESTIMATED_HOURS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_ESTIMATED_MINUTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_JUDGE_NAME_AND_EMAIL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_LISTED_LINKED_CASES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_MUST_TAKE_PLACE_AT_HOUR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_MUST_TAKE_PLACE_AT_MINUTE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_PRIORITY_TYPE_ENUM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_SPECIFIC_DATES_OPTIONS_ENUM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_TELEPHONE_CHANNELS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_VIDEO_CHANNELS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_N;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_HEARINGCHILDREQUIRED_Y;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LATEST_HEARING_DATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LOCAL_AUTHORITY_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENT_SOLICITOR_HEARING_CHANNEL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TELEPHONEPLATFORM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TELEPHONESUBCHANNELS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.VIDEOPLATFORM;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.VIDEOSUBCHANNELS;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

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

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    DateTimeFormatter customDateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");


    public HearingDataPrePopulatedDynamicLists populateHearingDynamicLists(String authorisation, String caseReferenceNumber,
                                                                           CaseData caseData, Hearings hearings) {
        Map<String, List<DynamicListElement>> hearingChannelsDetails = prePopulateHearingChannel(authorisation);
        return HearingDataPrePopulatedDynamicLists.builder().retrievedHearingTypes(getDynamicList(prePopulateHearingType(authorisation)))
            .retrievedHearingDates(getDynamicList(getHearingStartDate(caseReferenceNumber, hearings)))
            .retrievedHearingChannels(getDynamicList(hearingChannelsDetails.get(HEARINGCHANNEL)))
            .retrievedVideoSubChannels(getDynamicList(hearingChannelsDetails.get(VIDEOSUBCHANNELS)))
            .retrievedTelephoneSubChannels(getDynamicList(hearingChannelsDetails.get(TELEPHONESUBCHANNELS)))
            .retrievedCourtLocations(getDynamicList(locationRefDataService.getCourtLocations(authorisation)))
            .hearingListedLinkedCases(getDynamicList(getLinkedCases(authorisation, caseData)))
            .build();
    }

    public List<DynamicListElement> prePopulateHearingType(String authorisation) {
        try {
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

    public List<DynamicListElement> getHearingStartDate(String caseReferenceNumber,Hearings hearingDetails) {
        try {
            log.info("Hearing Details from hmc for the case id:{}",caseReferenceNumber);
            if (null != hearingDetails && null != hearingDetails.getCaseHearings()) {
                List<DynamicListElement> dynamicListElements = new ArrayList<>();
                for (CaseHearing caseHearing: hearingDetails.getCaseHearings()) {
                    if (LISTED.equalsIgnoreCase(caseHearing.getHmcStatus())) {
                        dynamicListElements.add(DynamicListElement.builder()
                                                    .code(String.valueOf(caseHearing.getHearingID()))
                                                    .label(caseHearing.getHearingTypeValue() + " - "
                                                               + caseHearing.getNextHearingDate().format(customDateTimeFormatter))
                                                    .build());
                    }
                }
                return dynamicListElements;
            }
        } catch (Exception e) {
            log.error("List of Hearing Start Date Values look up failed - {} {} ", e.getMessage(), e);
        }
        return List.of(DynamicListElement.builder().build());
    }

    public Map<String, List<DynamicListElement>> prePopulateHearingChannel(String authorisation) {
        try {
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
            return values;
        } catch (Exception e) {
            log.error("Category Values look up failed - " + e.getMessage(), e);
        }
        return new HashMap<>();

    }

    public List<DynamicListElement> getLinkedCases(String authorisation, CaseData caseData) {
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        try {
            log.info("Linked case method ", caseData.getId());
            CaseLinkedRequest caseLinkedRequest = CaseLinkedRequest.caseLinkedRequestWith()
                .caseReference(String.valueOf(caseData.getId())).build();
            Optional<List<CaseLinkedData>> caseLinkedDataList = ofNullable(hearingService.getCaseLinkedData(authorisation, caseLinkedRequest));
            if (caseLinkedDataList.isPresent()) {
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
            log.error("Exception occured in Linked case method for hmc api calls ", e.getMessage());
        }
        return dynamicListElements;
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
            .applicantSolicitorHearingChannel(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentHearingChannel(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentSolicitorHearingChannel(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                                                   ? null : hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .cafcassHearingChannel(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .cafcassCymruHearingChannel(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .localAuthorityHearingChannel(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            //We need to handle c100 details here ternary condition
            .applicantName(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? caseData.getApplicantName() : "")
            .applicantSolicitor(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())
                                    ? caseData.getApplicantsFL401().getRepresentativeFirstName()
                + "," + caseData.getApplicantsFL401().getRepresentativeLastName()  : "")
            .respondentName(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? caseData.getRespondentName() : "")
            .respondentSolicitor("")
            .fillingFormRenderingInfo(CommonUtils.renderCollapsible())
            .applicantName1(0 < numberOfApplicant ? applicantNames.get(0) : "INVALID_DATA")
            .applicantName2(1 < numberOfApplicant ? applicantNames.get(1) : "INVALID_DATA")
            .applicantName3(2 < numberOfApplicant ? applicantNames.get(2) : "INVALID_DATA")
            .applicantName4(3 < numberOfApplicant ? applicantNames.get(3) : "INVALID_DATA")
            .applicantName5(4 < numberOfApplicant ? applicantNames.get(4) : "INVALID_DATA")
            .applicantSolicitor1(0 < numberOfApplicantSolicitors ? applicantSolicitorNames.get(0) : "INVALID_DATA")
            .applicantSolicitor2(1 < numberOfApplicantSolicitors ? applicantSolicitorNames.get(1) : "INVALID_DATA")
            .applicantSolicitor3(2 < numberOfApplicantSolicitors ? applicantSolicitorNames.get(2) : "INVALID_DATA")
            .applicantSolicitor4(3 < numberOfApplicantSolicitors ? applicantSolicitorNames.get(3) : "INVALID_DATA")
            .applicantSolicitor5(4 < numberOfApplicantSolicitors ? applicantSolicitorNames.get(4) : "INVALID_DATA")
            .respondentName1(0 < numberOfRespondents ? respondentNames.get(0) : "INVALID_DATA")
            .respondentName2(1 < numberOfRespondents ? respondentNames.get(1) : "INVALID_DATA")
            .respondentName3(2 < numberOfRespondents ? respondentNames.get(2) : "INVALID_DATA")
            .respondentName4(3 < numberOfRespondents ? respondentNames.get(3) : "INVALID_DATA")
            .respondentName5(4 < numberOfRespondents ? respondentNames.get(4) : "INVALID_DATA")
            .respondentSolicitor1(0 < numberOfRespondentSolicitors ? respondentSolicitorNames.get(0) : "INVALID_DATA")
            .respondentSolicitor2(1 < numberOfRespondentSolicitors ? respondentSolicitorNames.get(1) : "INVALID_DATA")
            .respondentSolicitor3(2 < numberOfRespondentSolicitors ? respondentSolicitorNames.get(2) : "INVALID_DATA")
            .respondentSolicitor4(3 < numberOfRespondentSolicitors ? respondentSolicitorNames.get(3) : "INVALID_DATA")
            .respondentSolicitor5(4 < numberOfRespondentSolicitors ? respondentSolicitorNames.get(4) : "INVALID_DATA")
            .applicantHearingChannel1(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantHearingChannel2(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantHearingChannel3(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantHearingChannel4(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantHearingChannel5(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantSolicitorHearingChannel1(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantSolicitorHearingChannel2(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantSolicitorHearingChannel3(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantSolicitorHearingChannel4(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .applicantSolicitorHearingChannel5(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentHearingChannel1(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentHearingChannel2(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentHearingChannel3(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentHearingChannel4(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentHearingChannel5(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentSolicitorHearingChannel1(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentSolicitorHearingChannel2(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentSolicitorHearingChannel3(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentSolicitorHearingChannel4(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .respondentSolicitorHearingChannel5(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .build();
    }

    public List<Element<HearingData>> getHearingData(List<Element<HearingData>> hearingDatas,
                                                     HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists,CaseData caseData) {
        hearingDatas.stream().parallel().forEach(hearingDataElement -> {
            HearingData hearingData = hearingDataElement.getValue();
            hearingRequestDataMapper.mapHearingData(hearingData,hearingDataPrePopulatedDynamicLists,caseData);
            Optional<JudicialUser> judgeDetailsSelected = ofNullable(hearingData.getHearingJudgeNameAndEmail());
            log.info("judgeDetailsSelected ---> {}", judgeDetailsSelected);
            if (judgeDetailsSelected.isPresent() && judgeDetailsSelected.get().getPersonalCode() != null
                && !judgeDetailsSelected.get().getPersonalCode().isEmpty()) {
                Optional<List<JudicialUsersApiResponse>> judgeApiResponse = ofNullable(getJudgeDetails(hearingData.getHearingJudgeNameAndEmail()));
                log.info("JudgeAPI response {}", judgeApiResponse);
                if (!judgeApiResponse.get().isEmpty()) {
                    hearingData.setHearingJudgeLastName(judgeApiResponse.get().stream().findFirst().get().getSurname());
                    hearingData.setHearingJudgeEmailAddress(judgeApiResponse.get().stream().findFirst().get().getEmailId());
                    hearingData.setHearingJudgePersonalCode(judgeApiResponse.get().stream().findFirst().get().getPersonalCode());
                }
            }
        });
        return hearingDatas;
    }

    private List<JudicialUsersApiResponse> getJudgeDetails(JudicialUser hearingJudgeNameAndEmail) {

        String[] judgePersonalCode = allocatedJudgeService.getPersonalCode(hearingJudgeNameAndEmail);
        return refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder()
                                                                .personalCode(judgePersonalCode).build());
    }

    public DynamicList getDynamicList(List<DynamicListElement> listItems) {
        return DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(listItems).build();
    }

    public void nullifyUnncessaryFieldsPopulated(Object listWithoutNoticeHeardetailsObj) {
        //Note: When we add new fields , we need to add those fields in respective if else blocks to nullify to handle the data clearing issue from UI
        if (null != listWithoutNoticeHeardetailsObj) {
            List<Object> list = (List) listWithoutNoticeHeardetailsObj;
            if (list.size() > 0) {
                list.parallelStream().forEach(i -> {
                    LinkedHashMap<String,Object> hearingDataFromMap = (LinkedHashMap) (((LinkedHashMap) i).get("value"));
                    if (null != hearingDataFromMap) {
                        if (!(DATE_CONFIRMED_IN_HEARINGS_TAB.equals(hearingDataFromMap.get(HEARING_DATE_CONFIRM_OPTION_ENUM)))) {
                            hearingDataFromMap.put(CONFIRMED_HEARING_DATES, null);
                        } else {
                            hearingDataFromMap.put(APPLICANT_HEARING_CHANNEL, null);
                            hearingDataFromMap.put(APPLICANT_SOLICITOR_HEARING_CHANNEL, null);
                            hearingDataFromMap.put(RESPONDENT_HEARING_CHANNEL, null);
                            hearingDataFromMap.put(RESPONDENT_SOLICITOR_HEARING_CHANNEL, null);
                            hearingDataFromMap.put(CAFCASS_HEARING_CHANNEL, null);
                            hearingDataFromMap.put(CAFCASS_CYMRU_HEARING_CHANNEL, null);
                            hearingDataFromMap.put(HEARING_LISTED_LINKED_CASES, null);
                            hearingDataFromMap.put(LOCAL_AUTHORITY_HEARING_CHANNEL, null);
                            hearingDataFromMap.put(COURT_LIST, null);
                            hearingDataFromMap.put(HEARING_VIDEO_CHANNELS, null);
                            hearingDataFromMap.put(HEARING_TELEPHONE_CHANNELS, null);
                            hearingDataFromMap.put(HEARING_DATE_TIMES, null);
                            hearingDataFromMap.put(HEARING_ESTIMATED_HOURS, 0);
                            hearingDataFromMap.put(HEARING_ESTIMATED_MINUTES, 0);
                            hearingDataFromMap.put(HEARING_ESTIMATED_DAYS, 0);
                            hearingDataFromMap.put(ALL_PARTIES_ATTEND_HEARING_IN_THE_SAME_WAY, null);
                            hearingDataFromMap.put(HEARING_AUTHORITY, null);
                            hearingDataFromMap.put(HEARING_CHANNELS_ENUM, null);
                            hearingDataFromMap.put(HEARING_JUDGE_NAME_AND_EMAIL, null);
                            hearingDataFromMap.put(HEARING_SPECIFIC_DATES_OPTIONS_ENUM, null);
                            hearingDataFromMap.put(FIRST_DATE_OF_THE_HEARING, null);
                            hearingDataFromMap.put(HEARING_MUST_TAKE_PLACE_AT_HOUR, 0);
                            hearingDataFromMap.put(HEARING_MUST_TAKE_PLACE_AT_MINUTE, 0);
                            hearingDataFromMap.put(EARLIEST_HEARING_DATE, null);
                            hearingDataFromMap.put(LATEST_HEARING_DATE, null);
                            hearingDataFromMap.put(HEARING_PRIORITY_TYPE_ENUM, null);
                            hearingDataFromMap.put(CUSTOM_DETAILS, null);
                        }
                    }
                });
            }
        }
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

    List<DynamicListElement> getLinkedCasesDynamicList(String authorisation, String caseId) {
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        try {
            log.info("getLinkedCasesDynamicList case method ", caseId);
            CaseLinkedRequest caseLinkedRequest = CaseLinkedRequest.caseLinkedRequestWith()
                .caseReference(caseId).build();
            Optional<List<CaseLinkedData>> caseLinkedDataList = ofNullable(hearingService.getCaseLinkedData(authorisation, caseLinkedRequest));

            if (caseLinkedDataList.isPresent()) {
                return caseLinkedDataList.get().stream()
                    .map(cData -> DynamicListElement.builder()
                        .code(cData.getCaseReference()).label(cData.getCaseReference()).build()).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Exception occured in getLinkedCasesDynamicList {}", e.getMessage());
        }
        log.info("Dynamic case linking ----> {}", dynamicListElements);
        return dynamicListElements;
    }

    public List<Element<HearingData>> getHearingDataForSelectedHearing(CaseData caseData, Hearings hearings) {
        return caseData.getManageOrders().getOrdersHearingDetails().stream().parallel().map(hearingDataElement -> {
            HearingData hearingData = hearingDataElement.getValue();
            if (HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab.equals(hearingData.getHearingDateConfirmOptionEnum())) {
                Optional<CaseHearing> caseHearing = getHearingFromId(hearingData.getConfirmedHearingDates().getValue().getCode(), hearings);
                if (caseHearing.isPresent()) {
                    log.info("*** Case hearing : {}", caseHearing.get());
                    List<HearingDaySchedule> hearingDaySchedules = caseHearing.get().getHearingDaySchedule();
                    hearingDaySchedules.sort(Comparator.comparing(HearingDaySchedule::getHearingStartDateTime));
                    hearingData = hearingData.toBuilder()
                        .hearingdataFromHearingTab(populateHearingScheduleForDocmosis(hearingDaySchedules, caseData))
                        .build();
                }
            }
            return Element.<HearingData>builder().id(hearingDataElement.getId())
                .value(hearingData).build();
        }).collect(Collectors.toList());
    }

    private List<Element<HearingDataFromTabToDocmosis>> populateHearingScheduleForDocmosis(List<HearingDaySchedule> hearingDaySchedules,
                                                                                           CaseData caseData) {
        return hearingDaySchedules.stream().map(hearingDaySchedule -> element(HearingDataFromTabToDocmosis.builder()
            .hearingEstimatedDuration(getHearingDuration(hearingDaySchedule.getHearingStartDateTime(),
                                                         hearingDaySchedule.getHearingEndDateTime()))
            .hearingDate(hearingDaySchedule.getHearingStartDateTime().format(dateTimeFormatter))
            .hearingLocation(hearingDaySchedule.getHearingVenueName() + ", " + hearingDaySchedule.getHearingVenueAddress())
            .hearingTime(CaseUtils.convertLocalDateTimeToAmOrPmTime(hearingDaySchedule.getHearingStartDateTime()))
            .hearingArrangementsFromHmc(getHearingArrangementsData(hearingDaySchedules, caseData))
            .build())).collect(Collectors.toList());
    }

    private String getHearingDuration(LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start.toLocalTime(), end.toLocalTime()).toMinutes();
        return (minutes / 60) + " hours, " + (minutes % 60) + " minutes";
    }

    public Optional<CaseHearing> getHearingFromId(String hearingId, Hearings hearings) {
        return hearings.getCaseHearings().stream().filter(hearing -> hearingId.equalsIgnoreCase(String.valueOf(hearing.getHearingID())))
            .findFirst();
    }

    private DynamicList getHearingArrangementsData(List<HearingDaySchedule> hearingDaySchedules, CaseData caseData) {
        DynamicList dynamicList = DynamicList.builder().build();
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        for (Attendee attendee: hearingDaySchedules.get(0).getAttendees()) {
            Element<PartyDetails> partyDetailsElement = CaseUtils.getPartyFromPartyId(attendee.getPartyID(), caseData);
            if (partyDetailsElement != null) {
                dynamicListElements.add(DynamicListElement.builder().code(partyDetailsElement.getValue().getLabelForDynamicList())
                    .label(getHearingSubChannel(attendee.getHearingSubChannel()))
                                            .build());
            } else {
                dynamicListElements.add(DynamicListElement.builder().code(attendee.getPartyID())
                                            .label(getHearingSubChannel(attendee.getHearingSubChannel()))
                                            .build());
            }
        }
        log.info("*** Dynamic list *** {}", dynamicListElements);
        dynamicList.setListItems(dynamicListElements);
        log.info("*** Dynamic list *** {}", dynamicList);
        return dynamicList;
    }

    private String getHearingSubChannel(String channelCode) {
        return switch (channelCode) {
            case "TELOTHER" -> HearingChannelsEnum.TEL.getDisplayedValue();
            case "INTER" -> HearingChannelsEnum.INTER.getDisplayedValue();
            case "VIDTEAMS" -> HearingChannelsEnum.VID.getDisplayedValue();
            default -> "";
        };
    }
}
