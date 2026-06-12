package uk.gov.hmcts.reform.prl.services.cafcass;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_DATE_TIME;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassDateTimeService {

    private final FeatureToggleService featureToggleService;
    private final CafcassDateTimeUpdateHelper cafcassDateTimeUpdateHelper;

    @Value("#{'${cafcaas.caseState}'.split(',')}")
    private List<String> caseStateList;

    @Value("#{'${cafcaas.caseTypeOfApplicationList}'.split(',')}")
    private List<String> caseTypeList;

    @Value("#{'${cafcaas.excludedEvents}'.split(',')}")
    private List<String> excludedEventList;

    public Map<String, Object> updateCafcassDateTime(CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();

        return updateCafcassDateTime(caseDetails,
                                     caseDetailsBefore,
                                     callbackRequest.getEventId());
    }

    private Map<String, Object> updateCafcassDateTime(CaseDetails caseDetails,
                                                      CaseDetails caseDetailsBefore,
                                                      String eventId) {
        Map<String, Object> caseDataMap = caseDetails.getData();
        if (featureToggleService.isCafcassDateTimeFeatureEnabled()
            && !excludedEventList.contains(eventId)
            && caseStateList.contains(caseDetails.getState())
            && cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(caseDetails, caseDetailsBefore)) {
            caseDataMap.put(CAFCASS_DATE_TIME, ZonedDateTime.now(ZoneId.of("UTC")).toLocalDateTime());
        }

        return caseDataMap;
    }
}
