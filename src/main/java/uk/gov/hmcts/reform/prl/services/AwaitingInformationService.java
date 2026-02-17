package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.*;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AwaitingInformation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_STATUS;
import static uk.gov.hmcts.reform.prl.enums.State.AWAITING_INFORMATION;

@Slf4j
@Builder
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class AwaitingInformationService {

    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;
    private final CcdCoreCaseDataService ccdCoreCaseDataService;
    private final SystemUserService systemUserService;

    public List<String> validate(AwaitingInformation awaitingInformation) {
        isFeatureEnabled();

        List<String> errorList = new ArrayList<>();
        if (awaitingInformation.getReviewDate() != null
            && !awaitingInformation.getReviewDate().isAfter(LocalDate.now())) {
            errorList.add("The date must be in the future");
        }
        return errorList;
    }

    public Map<String, Object> addToCase(CallbackRequest callbackRequest) {
        isFeatureEnabled();

        // Get system user token and ID for case data store operations
        String systemAuthorisation = systemUserService.getSysUserToken();
        String systemUpdateUserId = systemUserService.getUserId(systemAuthorisation);
        String caseId = String.valueOf(callbackRequest.getCaseDetails().getId());

        log.info("Updating case {} with Awaiting Information", caseId);

        // Step 1: Start update for the case
        EventRequestData eventRequestData = ccdCoreCaseDataService.eventRequest(
            CaseEvent.AWAITING_INFORMATION,
            systemUpdateUserId
        );
        StartEventResponse startEventResponse = ccdCoreCaseDataService.startUpdate(
            systemAuthorisation,
            eventRequestData,
            caseId,
            true
        );
        // Extract AwaitingInformation from request
        AwaitingInformation awaitingInformation = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            AwaitingInformation.class
        );

        log.info(
            "Awaiting Information set - Review Date: {}, Reason: {}",
            awaitingInformation.getReviewDate(),
            awaitingInformation.getAwaitingInformationReasonEnum()
        );
        // Step 3: Create case data content with updated data
        CaseDataContent caseDataContent = ccdCoreCaseDataService.createCaseDataContent(
            startEventResponse,
            awaitingInformation
        );

        ccdCoreCaseDataService.submitUpdate(
            systemAuthorisation,
            eventRequestData,
            caseDataContent,
            caseId,
            true
        );

        log.info("Case {} successfully updated with Awaiting Information data", caseId);

        // Return map for response - prepare data with awaiting information
        @SuppressWarnings("unchecked")
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put(
            CASE_STATUS, CaseStatus.builder()
                .state(AWAITING_INFORMATION.getLabel())
                .build()
        );
        return caseDataUpdated;
    }

    private void isFeatureEnabled() {
        if (!featureToggleService.isAwaitingInformationEnabled()) {
            throw new RuntimeException("Awaiting information feature is not enabled");
        }
    }

    public AboutToStartOrSubmitCallbackResponse populateHeader(CallbackRequest callbackRequest) {
        isFeatureEnabled();
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }
}
