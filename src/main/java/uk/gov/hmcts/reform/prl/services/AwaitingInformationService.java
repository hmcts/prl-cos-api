package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AwaitingInformation;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_STATUS;
import static uk.gov.hmcts.reform.prl.enums.State.AWAITING_INFORMATION;

@Slf4j
@Builder
@RequiredArgsConstructor
@Service
public class AwaitingInformationService {

    private final FeatureToggleService featureToggleService;
    private final ObjectMapper objectMapper;

    public List<String> validate(AwaitingInformation awaitingInformation) {
        List<String> errorList = new ArrayList<>();
        if (featureToggleService.isAwaitingInformationEnabled() && awaitingInformation.getReviewDate()
            != null && !awaitingInformation.getReviewDate().isAfter(LocalDate.now())) {
            errorList.add("The date must be in the future");
        }
        return errorList;
    }

    public  Map<String, Object> addToCase(CallbackRequest callbackRequest) {
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put(
            CASE_STATUS, CaseStatus.builder()
                .state(AWAITING_INFORMATION.getLabel())
                .build()
        );
        var awaitingInformation = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(), AwaitingInformation.class);
        CaseUtils.setCaseState(callbackRequest, caseDataUpdated);
        return caseDataUpdated;
    }
}
