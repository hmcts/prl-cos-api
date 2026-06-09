package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestFurtherInformation;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.SPACE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_STATUS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REQUEST_FURTHER_INFORMATION_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.REQUEST_FURTHER_INFORMATION_HISTORY;
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
    public Event buildEventWithDescription(RequestFurtherInformation requestFurtherInformation) {
        StringBuilder description = new StringBuilder();

        // Add review date with DD/Month/YYYY format
        if (requestFurtherInformation.getReviewDate() != null) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
            String formattedDate = requestFurtherInformation.getReviewDate().format(dateFormatter);
            description.append("Review By Date: ")
                .append(formattedDate)
                .append(SPACE)
                .append(".\n");
        }

        // Add reasons
        var reasons = requestFurtherInformation.getRequestFurtherInformationReasonEnum();
        if (reasons != null && !reasons.isEmpty()) {
            // Add newline if something already exists
            if (description.length() > 0) {
                description.append("\n");
            }
            description.append("Awaiting Information Reasons:\n");
            // Join all displayed values with newlines
            String reasonText = reasons.stream()
                .map(r -> r.getDisplayedValue())
                .collect(joining(", \n"));
            description.append(reasonText);
        }

        // Create Event with description
        return Event.builder()
            .id(REQUEST_FURTHER_INFORMATION_HISTORY.getValue())
            .description(description.toString())
            .build();
    }

    public CaseDetails updateHistoryTab(CallbackRequest callbackRequest) {
        String systemAuthToken = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(systemAuthToken);

        EventRequestData eventRequestData = ccdCoreCaseDataService.eventRequest(
            REQUEST_FURTHER_INFORMATION_HISTORY, systemUpdateUserId);

        String caseId = valueOf(callbackRequest.getCaseDetails().getId());
        var startEventResponse =
            ccdCoreCaseDataService.startUpdate(systemAuthToken, eventRequestData, caseId, true);
        var requestFurtherInformation = getRequestFurtherInformation(startEventResponse.getCaseDetails().getData());

        var caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(buildEventWithDescription(requestFurtherInformation))
            .data(startEventResponse.getCaseDetails().getData())
            .build();
        CaseDetails caseDetails = ccdCoreCaseDataService.submitUpdate(
            systemAuthToken, eventRequestData, caseDataContent, caseId, true);
        log.info("History tab updated for case id: {}", caseId);
        return caseDetails;
    }

}
