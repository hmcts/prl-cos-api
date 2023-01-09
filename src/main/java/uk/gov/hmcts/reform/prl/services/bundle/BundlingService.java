package uk.gov.hmcts.reform.prl.services.bundle;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
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
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    HearingService hearingService;

    @Autowired
    private BundleCreateRequestMapper bundleCreateRequestMapper;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Value("${bundle.english.config}")
    private String bundleEnglishConfig;

    @Value("${bundle.welsh.config}")
    private String bundleWelshConfig;

    public BundleCreateResponse createBundleServiceRequest(CaseData caseData,String eventId,
                                                           String authorization) throws Exception {
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
                                              BundleCreateRequest bundleCreateRequest) throws Exception {
        return bundleApiClient.createBundleServiceRequest(authorization, serviceAuthorization, bundleCreateRequest);
    }

    private String getBundleConfig(YesOrNo welshPreference) {
        if (YesOrNo.Yes.equals(welshPreference)) {
            return bundleWelshConfig;
        }
        return bundleEnglishConfig;
    }

}
