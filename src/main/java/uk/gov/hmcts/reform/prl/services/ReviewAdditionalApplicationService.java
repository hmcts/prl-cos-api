package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.wa.WaMapper;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_C2_APPLICATION_SNR_CODE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_OTHER_APPLICATION_SNR_CODE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_SUBMITTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.UNDERSCORE;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewAdditionalApplicationService {

    public Map<String, Object> populateReviewAdditionalApplication(CaseData caseData,
                                                                   Map<String, Object> caseDataMap,
                                                                   String clientContext,
                                                                   String eventId) {
        AdditionalApplicationsBundle selectedAdditionalApplicationsBundle = getSelectedAdditionalApplicationDetails(
            caseData,
            clientContext, eventId
        );
        caseDataMap.put("selectedAdditionalApplicationsBundle", selectedAdditionalApplicationsBundle);
        return caseDataMap;
    }

    private AdditionalApplicationsBundle getSelectedAdditionalApplicationDetails(CaseData caseData,
                                                                                String clientContext,
                                                                                String eventId) {
        final UUID additionalApplicationId;
        List<Element<AdditionalApplicationsBundle>> additionalApplicationCollection = caseData.getAdditionalApplicationsBundle();
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

    public String getApplicationBundleDynamicCode(AdditionalApplicationsBundle additionalApplicationsBundle) {
        if (null != additionalApplicationsBundle.getOtherApplicationsBundle()) {
            OtherApplicationsBundle otherApplicationsBundle = additionalApplicationsBundle.getOtherApplicationsBundle();
            if (null != otherApplicationsBundle.getApplicationStatus()
                && otherApplicationsBundle.getApplicationStatus().equals(AWP_STATUS_SUBMITTED)) {
                return AWP_OTHER_APPLICATION_SNR_CODE.concat(UNDERSCORE)
                    .concat(otherApplicationsBundle.getUploadedDateTime());
            }
        } else if (null != additionalApplicationsBundle.getC2DocumentBundle()) {
            C2DocumentBundle c2DocumentBundle = additionalApplicationsBundle.getC2DocumentBundle();
            if (null != c2DocumentBundle.getApplicationStatus()
                && c2DocumentBundle.getApplicationStatus().equals(AWP_STATUS_SUBMITTED)) {
                return AWP_C2_APPLICATION_SNR_CODE.concat(UNDERSCORE).concat(c2DocumentBundle.getUploadedDateTime());
            }
        }
        return null;
    }

}
