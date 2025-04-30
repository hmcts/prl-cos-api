package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.wa.WaMapper;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewAdditionalApplicationService {

    public Map<String, Object> populateReviewAdditionalApplication(CaseData caseData,
                                                                   String authorization,
                                                                   String clientContext,
                                                                   String eventId) {
        Map<String, Object> caseDataMap = new HashMap<>();
        AdditionalApplicationsBundle selectedAdditionalApplicationsBundle = getSelectedAdditionalApplicationDetails(
            caseData.getAdditionalApplicationsBundle(),
            clientContext, eventId
        );

        caseDataMap.put(CASE_TYPE_OF_APPLICATION, CaseUtils.getCaseTypeOfApplication(caseData));
        caseDataMap.put("selectedAdditionalApplicationsBundle", selectedAdditionalApplicationsBundle);
        return caseDataMap;
    }

    private AdditionalApplicationsBundle getSelectedAdditionalApplicationDetails(
                                            List<Element<AdditionalApplicationsBundle>> additionalApplicationCollection,
                                            String clientContext,
                                            String eventId) {
        final UUID additionalApplicationId;
        log.info("Inside getSelectedAdditionalApplicationDetails");
        if (Event.REVIEW_ADDITIONAL_APPLICATION.getId().equals(eventId) && StringUtils.isNotEmpty(clientContext)) {
            log.info("Getting additional application id from client context");
            WaMapper waMapper = CaseUtils.getWaMapper(clientContext);
            additionalApplicationId = UUID.fromString(CaseUtils.getAdditionalApplicationId(waMapper));
        } else {
            log.info("Getting first additional application id from dynamic list ");
            additionalApplicationId = additionalApplicationCollection.getFirst().getId();
        }
        return CaseUtils.getAdditionalApplicationFromCollectionId(additionalApplicationCollection, additionalApplicationId);
    }

}
