package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.CitizenUpdatePartyDataContent;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenPartyDetailsMapper;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_COURT_NAME;
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

    protected static final List<CaseEvent> EVENT_IDS_FOR_ALL_TAB_REFRESHED = Arrays.asList(
        CaseEvent.CONFIRM_YOUR_DETAILS,
        CaseEvent.KEEP_DETAILS_PRIVATE
    );

    public static final String WITHDRAW_APPLICATION_DATA = "withDrawApplicationData";
    public static final String CASE_STATUS = "caseStatus";

    public CaseDetails updateCitizenPartyDetails(String authorisation,
                                                 String caseId,
                                                 String eventId,
                                                 CitizenUpdatedCaseData citizenUpdatedCaseData) {
        CaseDetails caseDetails = null;
        CaseEvent caseEvent = CaseEvent.fromValue(eventId);
        log.info("*************** eventId received from " + caseEvent.getValue());

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
            = allTabService.getStartUpdateForSpecificUserEvent(caseId, eventId, authorisation);
        CaseData dbCaseData = startAllTabsUpdateDataContent.caseData();

        Optional<CitizenUpdatePartyDataContent> citizenUpdatePartyDataContent = Optional.ofNullable(
            citizenPartyDetailsMapper.mapUpdatedPartyDetails(
                dbCaseData, citizenUpdatedCaseData,
                caseEvent,
                startAllTabsUpdateDataContent.authorisation()
            ));

        if (citizenUpdatePartyDataContent.isPresent()) {
            log.info("*************** Going to update party details received from Citizen");
            caseDetails = allTabService.submitUpdateForSpecificUserEvent(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                citizenUpdatePartyDataContent.get().updatedCaseDataMap(),
                startAllTabsUpdateDataContent.userDetails()
            );

            if (EVENT_IDS_FOR_ALL_TAB_REFRESHED.contains(caseEvent)) {
                log.info("*************** Going to refresh all tabs after updating citizen party details");
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
        Map<String, Object> caseDataMapToBeUpdated = caseDataToSubmit.toMap(objectMapper);
        // Do not remove the next line as it will overwrite the case state change
        caseDataMapToBeUpdated.remove("state");
        Iterables.removeIf(caseDataMapToBeUpdated.values(), Objects::isNull);
        try {
            log.info("caseDataMapToBeUpdated to be stored ===>" + objectMapper.writeValueAsString(caseDataMapToBeUpdated));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        CaseDetails caseDetails = allTabService.submitUpdateForSpecificUserEvent(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                caseDataMapToBeUpdated,
                startAllTabsUpdateDataContent.userDetails()
        );

        log.info("Submit event executed {}", eventId);
        try {
            log.info("caseDetails updated ===>" + objectMapper.writeValueAsString(caseDetails));
        } catch (JsonProcessingException e) {
            log.info("error");
        }
        return caseDetails;
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
}
