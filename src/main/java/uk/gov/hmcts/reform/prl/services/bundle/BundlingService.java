package uk.gov.hmcts.reform.prl.services.bundle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.BundleApiClient;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.bundle.BundleCreateRequestMapper;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import static uk.gov.hmcts.reform.prl.enums.State.DECISION_OUTCOME;

@Slf4j
@Service
public class BundlingService {
    @Autowired
    private BundleApiClient bundleApiClient;

    @Autowired
    private BundleCreateRequestMapper bundleCreateRequestMapper;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    HearingService hearingService;

    @Value("${bundle.english.config}")
    private String bundleEnglishConfig;

    @Value("${bundle.welsh.config}")
    private String bundleWelshConfig;

    public BundleCreateResponse createBundleServiceRequest(CaseData caseData,String eventId,
                                                           String authorization) {
        Hearings hearingDetails = null;
        if (DECISION_OUTCOME.equals(caseData.getState())) {
            hearingDetails = hearingService.getHearings(authorization, String.valueOf(caseData.getId()));
        }
        return createBundle(authorization,authTokenGenerator.generate(),
            bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(
                caseData, eventId, hearingDetails,
                getBundleConfig(null != caseData.getLanguagePreferenceWelsh() ? caseData.getLanguagePreferenceWelsh() : YesOrNo.No)));
    }

    private BundleCreateResponse createBundle(String authorization, String serviceAuthorization,
                                              BundleCreateRequest bundleCreateRequest) {
        BundleCreateResponse bundleCreateResponse = null;
        try {
//            bundleCreateResponse = bundleApiClient.createBundleServiceRequest(authorization, serviceAuthorization, bundleCreateRequest);
            log.info("calling stitching API");
            bundleCreateResponse = bundleApiClient.createStitchServiceRequest(authorization, serviceAuthorization, bundleCreateRequest);
            log.info("calling stitching API successful");
        } catch (Exception e) {
            log.info(" Error calling stitching API");
            log.error(e.getMessage());
        }
        return bundleCreateResponse;
    }

    private String getBundleConfig(YesOrNo welshPreference) {
        if (YesOrNo.Yes.equals(welshPreference)) {
            return bundleWelshConfig;
        }
        return bundleEnglishConfig;
    }

}
