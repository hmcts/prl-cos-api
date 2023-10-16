package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.enums.HearingChannelsEnum;
import uk.gov.hmcts.reform.prl.enums.HearingDateConfirmOptionEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.hearingrequest.HearingRequestDataMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.HearingDateTimeOption;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.logging.log4j.util.Strings.concat;
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
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getApplicantSolicitorNameList;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getFL401SolicitorName;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getPartyNameList;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getRespondentSolicitorNameList;
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
                    log.info("** Status {}", caseHearing.getHmcStatus());
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
        List<String> applicantNames  = getPartyNameList(caseData.getApplicants());
        List<String> respondentNames = getPartyNameList(caseData.getRespondents());
        List<String> applicantSolicitorNames = getApplicantSolicitorNameList(caseData.getApplicants());
        List<String> respondentSolicitorNames = getRespondentSolicitorNameList(caseData.getRespondents());
        int numberOfApplicant = applicantNames.size();
        int numberOfRespondents = respondentNames.size();
        int numberOfApplicantSolicitors = applicantSolicitorNames.size();
        int numberOfRespondentSolicitors  = respondentSolicitorNames.size();
        //default to CAFCASS England if CaseManagementLocation is null
        boolean isCafcassCymru = null == caseData.getCaseManagementLocation()
            || YesOrNo.No.equals(CaseUtils.cafcassFlag(caseData.getCaseManagementLocation().getRegion()));
        boolean isFL401Case = FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication());
        String applicantSolicitor = getFL401SolicitorName(caseData.getApplicantsFL401());
        String respondentSolicitor = getFL401SolicitorName(caseData.getRespondentsFL401());
        return HearingData.builder()
            .hearingTypes(hearingDataPrePopulatedDynamicLists.getRetrievedHearingTypes())
            .confirmedHearingDates(hearingDataPrePopulatedDynamicLists.getRetrievedHearingDates())
            .hearingChannels(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            .courtList(hearingDataPrePopulatedDynamicLists.getRetrievedCourtLocations())
            .hearingListedLinkedCases(hearingDataPrePopulatedDynamicLists.getHearingListedLinkedCases())
            .applicantHearingChannel(isFL401Case ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .applicantSolicitorHearingChannel(isFL401Case ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .respondentHearingChannel(isFL401Case ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .respondentSolicitorHearingChannel(isFL401Case ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .cafcassHearingChannel(!isCafcassCymru ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .cafcassCymruHearingChannel(isCafcassCymru ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .localAuthorityHearingChannel(hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels())
            //We need to handle c100 details here ternary condition
            .applicantName(isFL401Case ? concat(caseData.getApplicantName(), " (Applicant)") : null)
            .applicantSolicitor(isFL401Case && null != applicantSolicitor
                                    ? concat(applicantSolicitor, " (Applicant solicitor)")  : null)
            .respondentName(isFL401Case ? concat(caseData.getRespondentName(), " (Respondent)") : null)
            .respondentSolicitor(isFL401Case && null != respondentSolicitor
                                     ? concat(respondentSolicitor, " (Respondent solicitor)") : null)
            .fillingFormRenderingInfo(CommonUtils.renderCollapsible())
            .applicantName1(0 < numberOfApplicant ? concat(applicantNames.get(0), " (Applicant1)") : null)
            .applicantName2(1 < numberOfApplicant ? concat(applicantNames.get(1), " (Applicant2)") : null)
            .applicantName3(2 < numberOfApplicant ? concat(applicantNames.get(2), " (Applicant3)") : null)
            .applicantName4(3 < numberOfApplicant ? concat(applicantNames.get(3), " (Applicant4)") : null)
            .applicantName5(4 < numberOfApplicant ? concat(applicantNames.get(4), " (Applicant5)") : null)
            .applicantSolicitor1(0 < numberOfApplicantSolicitors ? concat(applicantSolicitorNames.get(0), " (Applicant1 solicitor)") : null)
            .applicantSolicitor2(1 < numberOfApplicantSolicitors ? concat(applicantSolicitorNames.get(1), " (Applicant2 solicitor)") : null)
            .applicantSolicitor3(2 < numberOfApplicantSolicitors ? concat(applicantSolicitorNames.get(2), " (Applicant3 solicitor)") : null)
            .applicantSolicitor4(3 < numberOfApplicantSolicitors ? concat(applicantSolicitorNames.get(3), " (Applicant4 solicitor)") : null)
            .applicantSolicitor5(4 < numberOfApplicantSolicitors ? concat(applicantSolicitorNames.get(4), " (Applicant5 solicitor)") : null)
            .respondentName1(0 < numberOfRespondents ? concat(respondentNames.get(0), " (Respondent1)") : null)
            .respondentName2(1 < numberOfRespondents ? concat(respondentNames.get(1), " (Respondent2)") : null)
            .respondentName3(2 < numberOfRespondents ? concat(respondentNames.get(2), " (Respondent3)") : null)
            .respondentName4(3 < numberOfRespondents ? concat(respondentNames.get(3), " (Respondent4)") : null)
            .respondentName5(4 < numberOfRespondents ? concat(respondentNames.get(4), " (Respondent5)") : null)
            .respondentSolicitor1(0 < numberOfRespondentSolicitors ? concat(respondentSolicitorNames.get(0), " (Respondent1 solicitor)") : null)
            .respondentSolicitor2(1 < numberOfRespondentSolicitors ? concat(respondentSolicitorNames.get(1), " (Respondent2 solicitor)") : null)
            .respondentSolicitor3(2 < numberOfRespondentSolicitors ? concat(respondentSolicitorNames.get(2), " (Respondent3 solicitor)") : null)
            .respondentSolicitor4(3 < numberOfRespondentSolicitors ? concat(respondentSolicitorNames.get(3), " (Respondent4 solicitor)") : null)
            .respondentSolicitor5(4 < numberOfRespondentSolicitors ? concat(respondentSolicitorNames.get(4), " (Respondent5 solicitor)") : null)
            .applicantHearingChannel1(0 < numberOfApplicant ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .applicantHearingChannel2(1 < numberOfApplicant ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .applicantHearingChannel3(2 < numberOfApplicant ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .applicantHearingChannel4(3 < numberOfApplicant ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .applicantHearingChannel5(4 < numberOfApplicant ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .applicantSolicitorHearingChannel1(0 < numberOfApplicantSolicitors
                                                   ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .applicantSolicitorHearingChannel2(1 < numberOfApplicantSolicitors
                                                   ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .applicantSolicitorHearingChannel3(2 < numberOfApplicantSolicitors
                                                   ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .applicantSolicitorHearingChannel4(3 < numberOfApplicantSolicitors
                                                   ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .applicantSolicitorHearingChannel5(4 < numberOfApplicantSolicitors
                                                   ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .respondentHearingChannel1(0 < numberOfRespondents ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .respondentHearingChannel2(1 < numberOfRespondents ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .respondentHearingChannel3(2 < numberOfRespondents ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .respondentHearingChannel4(3 < numberOfRespondents ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .respondentHearingChannel5(4 < numberOfRespondents ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .respondentSolicitorHearingChannel1(0 < numberOfRespondentSolicitors
                                                    ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .respondentSolicitorHearingChannel2(1 < numberOfRespondentSolicitors
                                                    ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .respondentSolicitorHearingChannel3(2 < numberOfRespondentSolicitors
                                                    ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .respondentSolicitorHearingChannel4(3 < numberOfRespondentSolicitors
                                                    ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            .respondentSolicitorHearingChannel5(4 < numberOfRespondentSolicitors
                                                    ? hearingDataPrePopulatedDynamicLists.getRetrievedHearingChannels() : null)
            //PRL-4260 - preload date picker field
            .hearingDateTimes(Arrays.asList(element(HearingDateTimeOption.builder().build())))
            .isCafcassCymru(isCafcassCymru ? YesOrNo.Yes : YesOrNo.No)
            .build();
    }

    private HearingData populateApplicantRespondentNames(HearingData hearingData, CaseData caseData) {
        List<String> applicantNames  = getPartyNameList(caseData.getApplicants());
        List<String> respondentNames = getPartyNameList(caseData.getRespondents());
        List<String> applicantSolicitorNames = getApplicantSolicitorNameList(caseData.getApplicants());
        List<String> respondentSolicitorNames = getRespondentSolicitorNameList(caseData.getRespondents());
        int numberOfApplicant = applicantNames.size();
        int numberOfRespondents = respondentNames.size();
        int numberOfApplicantSolicitors = applicantSolicitorNames.size();
        int numberOfRespondentSolicitors  = respondentSolicitorNames.size();
        hearingData = hearingData.toBuilder()
            .applicantName1(0 < numberOfApplicant ? concat(applicantNames.get(0), " (Applicant1)") : null)
            .applicantName2(1 < numberOfApplicant ? concat(applicantNames.get(1), " (Applicant2)") : null)
            .applicantName3(2 < numberOfApplicant ? concat(applicantNames.get(2), " (Applicant3)") : null)
            .applicantName4(3 < numberOfApplicant ? concat(applicantNames.get(3), " (Applicant4)") : null)
            .applicantName5(4 < numberOfApplicant ? concat(applicantNames.get(4), " (Applicant5)") : null)
            .applicantSolicitor1(0 < numberOfApplicantSolicitors ? concat(applicantSolicitorNames.get(0), " (Applicant1 solicitor)") : null)
            .applicantSolicitor2(1 < numberOfApplicantSolicitors ? concat(applicantSolicitorNames.get(1), " (Applicant2 solicitor)") : null)
            .applicantSolicitor3(2 < numberOfApplicantSolicitors ? concat(applicantSolicitorNames.get(2), " (Applicant3 solicitor)") : null)
            .applicantSolicitor4(3 < numberOfApplicantSolicitors ? concat(applicantSolicitorNames.get(3), " (Applicant4 solicitor)") : null)
            .applicantSolicitor5(4 < numberOfApplicantSolicitors ? concat(applicantSolicitorNames.get(4), " (Applicant5 solicitor)") : null)
            .respondentName1(0 < numberOfRespondents ? concat(respondentNames.get(0), " (Respondent1)") : null)
            .respondentName2(1 < numberOfRespondents ? concat(respondentNames.get(1), " (Respondent2)") : null)
            .respondentName3(2 < numberOfRespondents ? concat(respondentNames.get(2), " (Respondent3)") : null)
            .respondentName4(3 < numberOfRespondents ? concat(respondentNames.get(3), " (Respondent4)") : null)
            .respondentName5(4 < numberOfRespondents ? concat(respondentNames.get(4), " (Respondent5)") : null)
            .respondentSolicitor1(0 < numberOfRespondentSolicitors ? concat(respondentSolicitorNames.get(0), " (Respondent1 solicitor)") : null)
            .respondentSolicitor2(1 < numberOfRespondentSolicitors ? concat(respondentSolicitorNames.get(1), " (Respondent2 solicitor)") : null)
            .respondentSolicitor3(2 < numberOfRespondentSolicitors ? concat(respondentSolicitorNames.get(2), " (Respondent3 solicitor)") : null)
            .respondentSolicitor4(3 < numberOfRespondentSolicitors ? concat(respondentSolicitorNames.get(3), " (Respondent4 solicitor)") : null)
            .respondentSolicitor5(4 < numberOfRespondentSolicitors ? concat(respondentSolicitorNames.get(4), " (Respondent5 solicitor)") : null)
            .build();

        return hearingData;
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

    public HearingData getHearingDataForSdo(HearingData hearingData,
                                            HearingDataPrePopulatedDynamicLists hearingDataPrePopulatedDynamicLists, CaseData caseData) {
        hearingRequestDataMapper.mapHearingData(hearingData, hearingDataPrePopulatedDynamicLists, caseData);
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
        populateApplicantRespondentNames(hearingData, caseData);
        return hearingData;
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
            if (!list.isEmpty()) {
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
        return dynamicListElements;
    }

    public List<Element<HearingData>> getHearingDataForSelectedHearing(CaseData caseData, Hearings hearings) {
        List<Element<HearingData>> hearingDetails = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseData.getManageOrders().getOrdersHearingDetails())) {
            hearingDetails = caseData.getManageOrders().getOrdersHearingDetails();
        } else if (CollectionUtils.isNotEmpty(caseData.getManageOrders().getSolicitorOrdersHearingDetails())) {
            hearingDetails = caseData.getManageOrders().getSolicitorOrdersHearingDetails();
        }
        return hearingDetails.stream().parallel().map(hearingDataElement -> {
            HearingData hearingData = hearingDataElement.getValue();
            if (HearingDateConfirmOptionEnum.dateConfirmedInHearingsTab.equals(hearingData.getHearingDateConfirmOptionEnum())
                && null != hearingData.getConfirmedHearingDates().getValue()) {
                Optional<CaseHearing> caseHearing = getHearingFromId(hearingData.getConfirmedHearingDates().getValue().getCode(), hearings);
                if (caseHearing.isPresent()) {
                    List<HearingDaySchedule> hearingDaySchedules = new ArrayList<>(caseHearing.get().getHearingDaySchedule());
                    hearingDaySchedules.sort(Comparator.comparing(HearingDaySchedule::getHearingStartDateTime));
                    hearingData = hearingData.toBuilder()
                        .hearingdataFromHearingTab(populateHearingScheduleForDocmosis(hearingDaySchedules, caseData))
                        .build();
                }
            }
            return Element.<HearingData>builder().id(hearingDataElement.getId())
                .value(hearingData).build();
        }).toList();
    }

    private List<Element<HearingDataFromTabToDocmosis>> populateHearingScheduleForDocmosis(List<HearingDaySchedule> hearingDaySchedules,
                                                                                           CaseData caseData) {
        return hearingDaySchedules.stream().map(hearingDaySchedule -> {
            log.info("hearing start date time received from hmc {} for case id - {}", hearingDaySchedule
                .getHearingStartDateTime(), caseData.getId());

            LocalDateTime ldt = CaseUtils.convertUtcToBst(hearingDaySchedule
                                                              .getHearingStartDateTime());
            log.info("hearing start date time after converting to bst - {}", ldt);

            return element(HearingDataFromTabToDocmosis.builder()
                               .hearingEstimatedDuration(getHearingDuration(
                                   hearingDaySchedule.getHearingStartDateTime(),
                                   hearingDaySchedule.getHearingEndDateTime()
                               ))
                               .hearingDate(hearingDaySchedule.getHearingStartDateTime().format(dateTimeFormatter))
                               .hearingLocation(hearingDaySchedule.getHearingVenueName() + ", " + hearingDaySchedule.getHearingVenueAddress())
                               .hearingTime(CaseUtils.convertLocalDateTimeToAmOrPmTime(ldt))
                               .hearingArrangementsFromHmc(getHearingArrangementsData(hearingDaySchedules, caseData))
                               .build());
        }).toList();
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
            String partyName = CaseUtils.getPartyFromPartyId(attendee.getPartyID(), caseData);
            if (!partyName.isBlank() && null != attendee.getHearingSubChannel()) {
                dynamicListElements.add(DynamicListElement.builder().code(partyName)
                    .label(HearingChannelsEnum.getValue(attendee.getHearingSubChannel()).getDisplayedValue())
                                            .build());
            }
        }
        dynamicList.setListItems(dynamicListElements);
        return dynamicList;
    }
}
