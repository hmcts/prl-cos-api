package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestFurtherInformation;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_STATUS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REQUEST_FURTHER_INFORMATION_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.State.AWAITING_INFORMATION;

@Slf4j
@Builder
@RequiredArgsConstructor
@Service
public class RequestFurtherInformaitonService {

    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;

    public List<String> validate(CallbackRequest callbackRequest) {
        if (!featureToggleService.isAwaitingInformationEnabled()) {
            return emptyList();
        }
        var caseDataUpdated = addToCase(callbackRequest);
        var awaitingInformation = objectMapper.convertValue(
            caseDataUpdated.get(REQUEST_FURTHER_INFORMATION_DETAILS), RequestFurtherInformation.class);
        List<String> errorList = new ArrayList<>();
        if (awaitingInformation.getReviewDate()
            != null && !awaitingInformation.getReviewDate().isAfter(LocalDate.now())) {
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
}
