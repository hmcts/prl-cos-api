package uk.gov.hmcts.reform.prl.services.caseinitiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessService;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.utils.CaseUtils.getCaseData;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInitiationService {

    private final ObjectMapper objectMapper;
    private final EventService eventPublisher;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AssignCaseAccessService assignCaseAccessService;

    public void handleCaseInitiation(String authorisation, CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        String caseId = String.valueOf(caseData.getId());
        assignCaseAccessService.assignCaseAccess(caseId, authorisation);

        // setting supplementary data updates to enable global search
        Map<String, Map<String, Map<String, Object>>> supplementaryData = new HashMap<>();
        supplementaryData.put(
            "supplementary_data_updates",
            Map.of("$set", Map.of("HMCTSServiceId", "ABA5"))
        );
        coreCaseDataApi.submitSupplementaryData(authorisation, authTokenGenerator.generate(), caseId,
                                                supplementaryData
        );

        eventPublisher.publishEvent(new CaseDataChanged(caseData));
    }
}
