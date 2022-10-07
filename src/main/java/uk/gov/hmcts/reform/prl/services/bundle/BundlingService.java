package uk.gov.hmcts.reform.prl.services.bundle;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.BundleApiClient;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.bundle.BundleCreateRequestMapper;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BundlingService {

    private final ObjectMapper objectMapper;

    private final BundleApiClient bundleApiClient;

    private final BundleCreateRequestMapper bundleCreateRequestMapper;

    private final AuthTokenGenerator authTokenGenerator;

    @Value("${bundle.api.url")
    private final String bundleUrl;

    @Value("${bundle.english.config")
    private final String bundleEnglishConfig;

    @Value("${bundle.welsh.config")
    private final String bundleWelshConfig;

    public BundleCreateResponse createBundleServiceRequest(CallbackRequest callbackRequest, String authorization) throws Exception {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        //need to check on historical bundles on how to handle
        return createBundle(authorization, authTokenGenerator.generate(),
                            bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(caseData,getBundleConfig(caseData.getLanguagePreferenceWelsh())));
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
