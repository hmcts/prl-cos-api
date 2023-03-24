package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.mapper.hearingrequest.HearingRequestDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
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
        return List.of(DynamicListElement.builder().code(String.valueOf("2023-04-13T09:00")).label(String.valueOf(
            LocalDateTime.parse("2023-04-13T09:00").format(dateTimeFormatter))).build());
    }

    private DynamicListElement displayEntry(HearingDaySchedule hearingDaySchedule) {
        LocalDateTime hearingStartDateTime = hearingDaySchedule.getHearingStartDateTime();
        return DynamicListElement.builder().code(String.valueOf(hearingStartDateTime)).label(String.valueOf(
            hearingStartDateTime.format(dateTimeFormatter))).build();
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
            .respondentSolicitor(FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication()) ? "" : "")
            .fillingFormRenderingInfo(CommonUtils.renderCollapsible())
            .build();
    }

    public List<Element<HearingData>> getHearingData(List<Element<HearingData>> hearingDatas,
                                                     HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists,CaseData caseData) {
        hearingDatas.stream().parallel().forEach(hearingDataElement -> {
            HearingData hearingData = hearingDataElement.getValue();
            hearingRequestDataMapper.mapHearingData(hearingData,hearingDataPrePopulatedDynamicLists,caseData);
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

    public void nullifyUnncessaryFieldsPopulated(Object listWithoutNoticeHeardetailsObj) {
        //Note: When we add new fields , we need to add those fields in respective if else blocks to nullify to handle the data clearing issue from UI
        if (null != listWithoutNoticeHeardetailsObj) {
            log.info("Inside null check for listWithoutNoticeHearingDetails ");
            List list = (List) listWithoutNoticeHeardetailsObj;
            if (null != list && list.size() > 0) {
                list.parallelStream().forEach(i -> {
                    LinkedHashMap hearingDataFromMap = (LinkedHashMap) (((LinkedHashMap) i).get("value"));
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

}
