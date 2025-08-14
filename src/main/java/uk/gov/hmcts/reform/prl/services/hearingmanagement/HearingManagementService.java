package uk.gov.hmcts.reform.prl.services.hearingmanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.HearingRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDateRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ADJOURNED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CANCELLED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMPLETED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NEXT_HEARING_DATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.POSTPONED;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HearingManagementService {
    public static final String USER_TOKEN = "userToken";
    public static final String SYSTEM_UPDATE_USER_ID = "systemUpdateUserId";
    public static final String CASE_REF_ID = "id";
    public static final String EVENT_ID = "eventId";
    public static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";

    private final ObjectMapper objectMapper;
    private final SystemUserService systemUserService;
    private final AllTabServiceImpl allTabService;
    private final CcdCoreCaseDataService coreCaseDataService;

    private final HearingService hearingService;

    public void caseStateChangeForHearingManagement(HearingRequest hearingRequest, State caseState) {

        log.info("Processing the callback for the caseId {} with HMC status {}", hearingRequest.getCaseRef(),
                 hearingRequest.getHearingUpdate().getHmcStatus());

        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);
        log.info("Fetching the Case details based on caseId {}", hearingRequest.getCaseRef()
        );

        Map<String, Object> customFields = new HashMap<>();
        customFields.put(USER_TOKEN, userToken);
        customFields.put(SYSTEM_UPDATE_USER_ID, systemUpdateUserId);
        customFields.put(CASE_REF_ID, hearingRequest.getCaseRef());

        Map<String, Object> fields = new HashMap<>();

        if (hearingRequest.getNextHearingDateRequest() != null
            && hearingRequest.getNextHearingDateRequest().getNextHearingDetails() != null
            && hearingRequest.getNextHearingDateRequest().getNextHearingDetails().getHearingDateTime() != null) {
            fields.put(NEXT_HEARING_DATE, hearingRequest.getNextHearingDateRequest().getNextHearingDetails()
                .getHearingDateTime().toLocalDate());
        }

        String hmcStatus = hearingRequest.getHearingUpdate().getHmcStatus();

        List<String> allowedHmcStatus = List.of(
            LISTED,
            COMPLETED,
            POSTPONED,
            ADJOURNED,
            CANCELLED
        );
        if (allowedHmcStatus.contains(hmcStatus)) {
            switch (caseState) {
                case PREPARE_FOR_HEARING_CONDUCT_HEARING -> {
                    fields.put("currentHearingId",hearingRequest.getHearingId());
                    fields.put("currentHearingStatus",hmcStatus);
                    customFields.put(EVENT_ID, CaseEvent.HMC_CASE_STATUS_UPDATE_TO_PREP_FOR_HEARING);
                    submitUpdate(fields, customFields);
                }
                case DECISION_OUTCOME -> {
                    customFields.put(EVENT_ID, CaseEvent.HMC_CASE_STATUS_UPDATE_TO_DECISION_OUTCOME);
                    submitUpdate(fields, customFields);
                }
                default -> {
                    break;
                }
            }
        }
    }

    private void submitUpdate(Map<String, Object> data, Map<String, Object> fields) {
        EventRequestData eventRequestData = coreCaseDataService.eventRequest(
            (CaseEvent) fields.get(EVENT_ID),
            (String) fields.get(SYSTEM_UPDATE_USER_ID)
        );
        StartEventResponse startEventResponse =
            coreCaseDataService.startUpdate(
                (String) fields.get(USER_TOKEN),
                eventRequestData,
                (String) fields.get(CASE_REF_ID),
                true
            );
        CaseData caseData = CaseUtils.getCaseDataFromStartUpdateEventResponse(
            startEventResponse,
            objectMapper
        );

        data.put(CASE_TYPE_OF_APPLICATION, caseData.getCaseTypeOfApplication());

        CaseDataContent caseDataContent = coreCaseDataService.createCaseDataContent(
            startEventResponse,
            data
        );
        coreCaseDataService.submitUpdate(
            (String) fields.get(USER_TOKEN),
            eventRequestData,
            caseDataContent,
            (String) fields.get(CASE_REF_ID),
            true
        );
    }

    public void caseNextHearingDateChangeForHearingManagement(NextHearingDateRequest nextHearingDateRequest) {

        log.info("Processing the callback for the caseId {} with next hearing date {}", nextHearingDateRequest.getCaseRef(),
                 nextHearingDateRequest.getNextHearingDetails().getHearingDateTime());

        String userToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(userToken);
        Map<String, Object> customFields = new HashMap<>();
        customFields.put(USER_TOKEN, userToken);
        customFields.put(SYSTEM_UPDATE_USER_ID, systemUpdateUserId);
        customFields.put(CASE_REF_ID, nextHearingDateRequest.getCaseRef());
        customFields.put(EVENT_ID, CaseEvent.UPDATE_NEXT_HEARING_DATE_IN_CCD);
        Map<String, Object> data = new HashMap<>();
        data.put("nextHearingDetails", nextHearingDateRequest.getNextHearingDetails());
        if (nextHearingDateRequest.getNextHearingDetails() != null
            && nextHearingDateRequest.getNextHearingDetails().getHearingDateTime() != null) {
            data.put(NEXT_HEARING_DATE, nextHearingDateRequest.getNextHearingDetails().getHearingDateTime().toLocalDate());
        }
        submitUpdate(data, customFields);
    }

    public NextHearingDetails getNextHearingDate(String caseReference) {
        String userToken = systemUserService.getSysUserToken();
        return hearingService.getNextHearingDate(userToken, caseReference);
    }

    public void validateHearingState(Map<String, Object> caseDataUpdated, CaseData caseData) {
        if (caseData.getHearingTaskData() != null
                && "LISTED".equals(caseData.getHearingTaskData().getCurrentHearingStatus())) {
            caseDataUpdated.put("hearingListed", "true");
            log.info("hearing listed for the case {} with hearing id {} ", caseData.getId(),
                    caseData.getHearingTaskData().getCurrentHearingId());
        } else {
            caseDataUpdated.put("hearingListed", "false");
            log.info("hearing not listed for the case {}", caseData.getId());
        }
    }
}
