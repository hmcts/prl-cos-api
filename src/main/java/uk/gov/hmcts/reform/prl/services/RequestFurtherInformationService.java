package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.*;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestFurtherInformation;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.*;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.REQUEST_FURTHER_INFORMATION;
import static uk.gov.hmcts.reform.prl.enums.State.AWAITING_INFORMATION;

@Slf4j
@Builder
@RequiredArgsConstructor
@Service
public class RequestFurtherInformationService {

    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;
    private final CcdCoreCaseDataService ccdCoreCaseDataService;
    private final SystemUserService systemUserService;

    public List<String> validate(CallbackRequest callbackRequest) {
        if (!featureToggleService.isAwaitingInformationEnabled()) {
            return emptyList();
        }
        var caseDataUpdated = addToCase(callbackRequest);
        var requestFurtherInformation = getRequestFurtherInformation(caseDataUpdated);
        var errorList = new ArrayList<String>();
        if (requestFurtherInformation.getReviewDate()
            != null && !requestFurtherInformation.getReviewDate().isAfter(LocalDate.now())) {
            errorList.add("Please enter a future date");
        }
        return errorList;
    }

    public Map<String, Object> addToCase(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put(
            CASE_STATUS, CaseStatus.builder()
                .state(AWAITING_INFORMATION.getLabel())
                .build()
        );
        CaseUtils.setCaseState(callbackRequest, caseDataUpdated);
        return caseDataUpdated;
    }

    private RequestFurtherInformation getRequestFurtherInformation(Map<String, Object> caseDataUpdated) {
        var requestFurtherInformation = objectMapper.convertValue(
            caseDataUpdated.get(REQUEST_FURTHER_INFORMATION_DETAILS),
            RequestFurtherInformation.class
        );
        return requestFurtherInformation;
    }

    /**
     * Builds event description from RequestFurtherInformation data.
     * Combines reviewDate and requestFurtherInformationReasonEnum into a formatted string.
     *
     * @param requestFurtherInformation The RequestFurtherInformation object
     * @return Formatted event description string
     */
    private Event buildEventWithDescription(RequestFurtherInformation requestFurtherInformation) {
        StringBuilder description = new StringBuilder();
        // Add review date to description
        if (requestFurtherInformation.getReviewDate() != null) {
            description.append("Review By Date: ")
                .append(requestFurtherInformation.getReviewDate());
        }
        // Add reasons to description
        if (requestFurtherInformation.getRequestFurtherInformationReasonEnum() != null
            && !requestFurtherInformation.getRequestFurtherInformationReasonEnum().isEmpty()) {
            if (description.length() > 0) {
                description.append(" | ");
            }
            String reasons = requestFurtherInformation.getRequestFurtherInformationReasonEnum()
                .stream()
                .map(reason -> reason.getDisplayedValue())
                .collect(joining(", "));
            description.append("Reasons: ").append(reasons);
        }
        // Create Event with description
        return Event.builder()
            .id(REQUEST_FURTHER_INFORMATION_DETAILS)
            .description(description.toString())
            .build();
    }

    public CaseDetails updateHistoryTab(CallbackRequest callbackRequest, String authorisationComingFromAPI, String s2sTokenComingFromAPI) {
        log.info("CCCCCCCCCCCCCCCCCCCCCCc In method During RequestFurtherInformation with event description");
        String caseId = valueOf(callbackRequest.getCaseDetails().getId());
        log.info("CCCCCCCCCCCCCCCCCCCCCCc Case ID  : {}", caseId);
        String systemAuthToken = systemUserService.getSysUserToken();
        log.info("CCCCCCCCCCCCCCCCCCCCCCc systemAuthToken : {}", systemAuthToken);
        String systemUpdateUserId = systemUserService.getUserId(systemAuthToken);
        log.info("CCCCCCCCCCCCCCCCCCCCCCc systemAuthToken : {}", systemUpdateUserId);

        EventRequestData eventRequestData = ccdCoreCaseDataService.eventRequest(
            CaseEvent.REQUEST_FURTHER_INFORMATION,
            systemUpdateUserId
        );
        log.info("CCCCCCCCCCCCCCCCCCCCCCc eventRequestData : {}", eventRequestData);
        StartEventResponse startEventResponse =
            ccdCoreCaseDataService.startUpdate(
                systemAuthToken,
                eventRequestData,
                caseId,
                true
            );
        log.info("CCCCCCCCCCCCCCCCCCCCCCc startEventResponse : {}", startEventResponse);
        var requestFurtherInformation = getRequestFurtherInformation(callbackRequest.getCaseDetails().getData());
        log.info("CCCCCCCCCCCCCCCCCCCCCCc requestFurtherInformation : {}", requestFurtherInformation);
        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(buildEventWithDescription(requestFurtherInformation))
            .data(startEventResponse.getCaseDetails().getData())
            .build();
        log.info("CCCCCCCCCCCCCCCCCCCCCCc caseDataContent : {}", caseDataContent);
        CaseDetails caseDetails = null;
        try {
            caseDetails = ccdCoreCaseDataService.submitUpdate(
                systemAuthToken,
                eventRequestData,
                caseDataContent,
                caseId,
                true
            );
        } catch (RuntimeException ex) {
            log.error(
                "CCCCCCCCCCCCCCCCCCCCCCc systemAuthToken    UnauthorizedException while updating history tab for caseId {}: {}",
                caseId,
                ex.getMessage()
            );
        }

        try {
            caseDetails = ccdCoreCaseDataService.submitUpdate(
                s2sTokenComingFromAPI,
                eventRequestData,
                caseDataContent,
                caseId,
                true
            );
        } catch (RuntimeException ex) {
            log.error(
                "CCCCCCCCCCCCCCCCCCCCCCB s2sTokenComingFromAPI UnauthorizedException while updating history tab for caseId {}: {}",
                caseId,
                ex.getMessage()
            );
        }

        try {
            caseDetails = ccdCoreCaseDataService.submitUpdate(
                authorisationComingFromAPI,
                eventRequestData,
                caseDataContent,
                caseId,
                true
            );
        } catch (RuntimeException ex) {
            log.error(
                "CCCCCCCCCCCCCCCCCCCCCCB authorisationComingFromAPI UnauthorizedException while updating history tab for caseId {}: {}",
                caseId,
                ex.getMessage()
            );
        }
        log.info("CCCCCCCCCCCCCCCCCCCCCCc caseDetails : {}", caseDetails);
        return caseDetails;
    }

}
