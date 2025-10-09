package uk.gov.hmcts.reform.prl.services.cafcass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_DATE_TIME;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassDateTimeService {

    private final FeatureToggleService featureToggleService;

    @Value("#{'${cafcaas.caseState}'.split(',')}")
    private List<String> caseStateList;

    @Value("#{'${cafcaas.caseTypeOfApplicationList}'.split(',')}")
    private List<String> caseTypeList;

    @Value("#{'${cafcaas.excludedEvents}'.split(',')}")
    private List<String> excludedEventList;

    public Map<String, Object> updateCafcassDateTime(CallbackRequest callbackRequest) {
        if (featureToggleService.isCafcassDateTimeFeatureEnabled()
            && !excludedEventList.contains(callbackRequest.getEventId())
            && caseStateList.contains(callbackRequest.getCaseDetails().getState())) {
            callbackRequest.getCaseDetails().getData().put(CAFCASS_DATE_TIME, LocalDateTime.now());
        }

        return callbackRequest.getCaseDetails().getData();
    }
}
