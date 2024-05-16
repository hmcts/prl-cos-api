package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.CitizenUpdatePartyDataContent;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenPartyDetailsMapper;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.caseflags.request.LanguageSupportCaseNotesRequest;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.services.AddCaseNoteService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_COURT_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.READY_FOR_DELETION_STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.WITHDRAWN_STATE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenCaseUpdateService {

    private final AllTabServiceImpl allTabService;
    private final CitizenPartyDetailsMapper citizenPartyDetailsMapper;
    private final ObjectMapper objectMapper;
    private final AddCaseNoteService addCaseNoteService;
    private final PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    protected static final List<CaseEvent> EVENT_IDS_FOR_ALL_TAB_REFRESHED = Arrays.asList(
        CaseEvent.CONFIRM_YOUR_DETAILS,
        CaseEvent.KEEP_DETAILS_PRIVATE,
        CaseEvent.CITIZEN_CONTACT_PREFERENCE
    );

    public static final String WITHDRAW_APPLICATION_DATA = "withDrawApplicationData";
    public static final String CASE_STATUS = "caseStatus";
    public static final String LANG_SUPPORT_NEED_SUBJECT = "Support needs request";

    public CaseDetails updateCitizenPartyDetails(String authorisation,
                                                 String caseId,
                                                 String eventId,
                                                 CitizenUpdatedCaseData citizenUpdatedCaseData) {
        CaseDetails caseDetails = null;
        try {
            log.info("CitizenUpdatedCaseData is::" + objectMapper.writeValueAsString(citizenUpdatedCaseData));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        CaseEvent caseEvent = CaseEvent.fromValue(eventId);
        log.info("Case event is :: " + eventId);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = allTabService.getStartUpdateForSpecificUserEvent(caseId, eventId, authorisation);
        CaseData dbCaseData = startAllTabsUpdateDataContent.caseData();
        log.info("Case event is triggered:: " + eventId);
        Optional<CitizenUpdatePartyDataContent> citizenUpdatePartyDataContent = Optional.ofNullable(
            citizenPartyDetailsMapper.mapUpdatedPartyDetails(
                dbCaseData, citizenUpdatedCaseData,
                caseEvent,
                startAllTabsUpdateDataContent.authorisation()
            ));
        log.info("Data processing is done, ready to submit to ccd:: " + eventId);
        if (citizenUpdatePartyDataContent.isPresent()) {
            Map<String, Object> caseDataMapToBeUpdated = citizenUpdatePartyDataContent.get().updatedCaseDataMap();
            try {
                log.info("case data updated map is::" + objectMapper.writeValueAsString(caseDataMapToBeUpdated));
            } catch (JsonProcessingException e) {
                log.info("error");
            }

            Iterables.removeIf(caseDataMapToBeUpdated.values(), Objects::isNull);
            try {
                log.info("case data updated map is now::" + objectMapper.writeValueAsString(caseDataMapToBeUpdated));
            } catch (JsonProcessingException e) {
                log.info("error");
            }
            caseDetails = allTabService.submitUpdateForSpecificUserEvent(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                caseDataMapToBeUpdated,
                startAllTabsUpdateDataContent.userDetails()
            );
            log.info("Data processing is completed:: ");
            if (EVENT_IDS_FOR_ALL_TAB_REFRESHED.contains(caseEvent)) {
                log.info("inside all tab refresh loop:: ");
                return allTabService.updateAllTabsIncludingConfTab(caseId);
            }
        }

        return caseDetails;
    }

    public CaseDetails saveDraftCitizenApplication(String caseId, CaseData citizenUpdatedCaseData, String authToken)
            throws JsonProcessingException {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
                allTabService.getStartUpdateForSpecificUserEvent(
                        caseId,
                        CaseEvent.CITIZEN_SAVE_C100_DRAFT_INTERNAL.getValue(),
                        authToken
                );
        Map<String, Object> caseDataMapToBeUpdated = citizenPartyDetailsMapper.getC100RebuildCaseDataMap(citizenUpdatedCaseData);

        return allTabService.submitUpdateForSpecificUserEvent(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                caseDataMapToBeUpdated,
                startAllTabsUpdateDataContent.userDetails()
        );
    }

    public CaseDetails submitCitizenC100Application(String authToken,
                                                    String caseId,
                                                    String eventId,
                                                    CaseData citizenUpdatedCaseData)
            throws JsonProcessingException {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
                allTabService.getStartUpdateForSpecificUserEvent(
                        caseId,
                        CaseEvent.fromValue(eventId).getValue(),
                        authToken
                );

        UserDetails userDetails = startAllTabsUpdateDataContent.userDetails();
        UserInfo userInfo = UserInfo
                .builder()
                .idamId(userDetails.getId())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname().orElse(null))
                .emailAddress(userDetails.getEmail())
                .build();
        CaseData dbCaseData = startAllTabsUpdateDataContent.caseData();
        dbCaseData = dbCaseData.toBuilder().userInfo(wrapElements(userInfo))
                .courtName(C100_DEFAULT_COURT_NAME)
                .taskListVersion(TASK_LIST_VERSION_V2)
                .build();

        CaseData caseDataToSubmit = citizenPartyDetailsMapper
                .buildUpdatedCaseData(dbCaseData, citizenUpdatedCaseData.getC100RebuildData());
        Map<String, Object> caseDataMapToBeUpdated = objectMapper.convertValue(caseDataToSubmit, Map.class);
        // Do not remove the next line as it will overwrite the case state change
        caseDataMapToBeUpdated.remove("state");
        Iterables.removeIf(caseDataMapToBeUpdated.values(), Objects::isNull);
        CaseDetails caseDetails = allTabService.submitUpdateForSpecificUserEvent(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                caseDataMapToBeUpdated,
                startAllTabsUpdateDataContent.userDetails()
        );

        return partyLevelCaseFlagsService.generateAndStoreCaseFlags(String.valueOf(caseDetails.getId()));
    }

    public CaseDetails deleteApplication(String caseId, CaseData citizenUpdatedCaseData, String authToken)
            throws JsonProcessingException {
        Map<String, Object> caseDataMapToBeUpdated = citizenPartyDetailsMapper.getC100RebuildCaseDataMap(citizenUpdatedCaseData);
        caseDataMapToBeUpdated.put(STATE, READY_FOR_DELETION_STATE);
        caseDataMapToBeUpdated.put(
            CASE_STATUS,
            CaseStatus.builder().state(State.READY_FOR_DELETION.getLabel()).build()
        );

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificUserEvent(
                caseId,
                CaseEvent.DELETE_APPLICATION.getValue(),
                authToken
            );

        return allTabService.submitUpdateForSpecificUserEvent(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataMapToBeUpdated,
            startAllTabsUpdateDataContent.userDetails()
        );
    }

    public CaseDetails withdrawCase(CaseData oldCaseData, String caseId, String authToken) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent =
            allTabService.getStartUpdateForSpecificUserEvent(
                caseId,
                CaseEvent.CITIZEN_CASE_WITHDRAW.getValue(),
                authToken
            );
        Map<String, Object> caseDataMapToBeUpdated = new HashMap<>();

        WithdrawApplication withDrawApplicationData = oldCaseData.getWithDrawApplicationData();
        Optional<YesOrNo> withdrawApplication = ofNullable(withDrawApplicationData.getWithDrawApplication());
        if ((withdrawApplication.isPresent() && Yes.equals(withdrawApplication.get()))) {
            caseDataMapToBeUpdated.put(WITHDRAW_APPLICATION_DATA, withDrawApplicationData);
            caseDataMapToBeUpdated.put(STATE, WITHDRAWN_STATE);
            caseDataMapToBeUpdated.put(
                CASE_STATUS,
                CaseStatus.builder().state(State.CASE_WITHDRAWN.getLabel()).build()
            );
        }

        allTabService.submitUpdateForSpecificUserEvent(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataMapToBeUpdated,
            startAllTabsUpdateDataContent.userDetails()
        );

        return allTabService.updateAllTabsIncludingConfTab(caseId);
    }

    public ResponseEntity<Object> addLanguageSupportCaseNotes(
        String caseId,
        String authorisation,
        LanguageSupportCaseNotesRequest languageSupportCaseNotesRequest) {
        log.info("Inside addLanguageSupportCaseNotes for caseId {}", caseId);
        log.info(
            "Inside addLanguageSupportCaseNotes languageSupportCaseNotesRequest {}",
            languageSupportCaseNotesRequest
        );

        if (StringUtils.isEmpty(languageSupportCaseNotesRequest.getPartyIdamId())
            || StringUtils.isEmpty(languageSupportCaseNotesRequest.getLanguageSupportNotes())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("bad request");
        }

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = allTabService.getStartUpdateForSpecificUserEvent(
            caseId,
            CaseEvent.CITIZEN_LANG_SUPPORT_NOTES.getValue(),
            authorisation
        );
        CaseData dbCaseData = startAllTabsUpdateDataContent.caseData();

        CaseNoteDetails currentCaseNoteDetails = addCaseNoteService.getCurrentCaseNoteDetails(
            LANG_SUPPORT_NEED_SUBJECT,
            languageSupportCaseNotesRequest.getLanguageSupportNotes(),
            startAllTabsUpdateDataContent.userDetails()
        );
        Map<String, Object> caseNotesMap = new HashMap<>();
        caseNotesMap.put(
            CASE_NOTES,
            addCaseNoteService.getCaseNoteDetails(dbCaseData, currentCaseNoteDetails)
        );

        try {
            log.info("CaseDataContent ===>" + objectMapper.writeValueAsString(caseNotesMap));
        } catch (JsonProcessingException e) {
            log.info("error");
        }

        allTabService.submitUpdateForSpecificUserEvent(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseNotesMap,
            startAllTabsUpdateDataContent.userDetails()
        );
        return ResponseEntity.status(HttpStatus.OK).body("Language support needs published in case notes");
    }
}
