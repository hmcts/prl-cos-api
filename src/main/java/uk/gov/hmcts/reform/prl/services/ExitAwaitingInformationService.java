package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ExitAwaitingInformation;

import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_STATUS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EXIT_AWAITING_INFORMATION_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;

@Slf4j
@Builder
@RequiredArgsConstructor
@Service
public class ExitAwaitingInformationService {

    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> updateCase(CallbackRequest callbackRequest) {
        var caseDataUpdated = callbackRequest.getCaseDetails().getData();
        var exitAwaitingInformation = getExitAwaitingInformation(caseDataUpdated);
        var targetState = exitAwaitingInformation.getExitAwaitingInformationTargetState();

        caseDataUpdated.put(STATE_FIELD, targetState.getValue());
        caseDataUpdated.put(
            CASE_STATUS, CaseStatus.builder()
                .state(targetState.getLabel())
                .build()
        );
        log.info(
            "Updated '{}' field for case {} to {}",
            CASE_STATUS, callbackRequest.getCaseDetails().getId(), targetState.getLabel()
        );
        return caseDataUpdated;
    }

    private ExitAwaitingInformation getExitAwaitingInformation(Map<String, Object> caseData) {
        return objectMapper.convertValue(
            caseData.get(EXIT_AWAITING_INFORMATION_DETAILS),
            ExitAwaitingInformation.class
        );
    }
}
