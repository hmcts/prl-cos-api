package uk.gov.hmcts.reform.prl.services.bundle;

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

@Service
public class BundlingService {
    @Autowired
    private BundleApiClient bundleApiClient;

    @Autowired
    private BundleCreateRequestMapper bundleCreateRequestMapper;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Value("${bundle.english.config}")
    private String bundleEnglishConfig;

    @Value("${bundle.welsh.config}")
    private String bundleWelshConfig;

    public BundleCreateResponse createBundleServiceRequest(CaseData caseData, String authorization) {
        return createBundle(authorization, authTokenGenerator.generate(),
            bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(
                caseData, getBundleConfig(caseData.getLanguagePreferenceWelsh())));
    }

    private BundleCreateResponse createBundle(String authorization, String serviceAuthorization,
                                              BundleCreateRequest bundleCreateRequest) {
        return bundleApiClient.createBundleServiceRequest(authorization, serviceAuthorization, bundleCreateRequest);
    }

    private String getBundleConfig(YesOrNo welshPreference) {
        if (YesOrNo.Yes.equals(welshPreference)) {
            return bundleWelshConfig;
        }
        return bundleEnglishConfig;
    }

}
