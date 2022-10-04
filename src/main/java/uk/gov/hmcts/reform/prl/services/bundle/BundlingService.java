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
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;

@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BundlingService {

    private final ObjectMapper objectMapper;

    private final BundleApiClient bundleApiClient;

    private final AuthTokenGenerator authTokenGenerator;

    @Value("${bundle.api.url")
    private final String bundleUrl;

    @Value("${bundle.english.config")
    private final String bundleEnglishConfig;

    @Value("${bundle.welsh.config")
    private final String bundleWelshConfig;

    public PreSubmitCallbackResponse<CaseData> createBundleServiceRequest(CallbackRequest callbackRequest, String authorisation) throws Exception {

        final CaseDetails caseDetails = callbackRequest.getCaseDetails();
        final CaseData caseData = objectMapper.convertValue(
            CaseData.builder().applicantCaseName(caseDetails.getCaseData().getApplicantCaseName())
                .id(Long.valueOf(callbackRequest.getCaseDetails().getCaseId())).build(),
            CaseData.class
        );
        caseData.setBundleConfiguration(getBundleConfig(caseData.getLanguagePreferenceWelsh()));
        BundleCallbackRequest<CaseData> bundleCallback = new BundleCallbackRequest<>(callbackRequest);
        return creaeBundle(authorisation, authTokenGenerator.generate(), bundleCallback);
    }

    private PreSubmitCallbackResponse<CaseData> creaeBundle(String authorization, String serviceAuthorization,
                                                            BundleCallbackRequest<CaseData> callbackRequest) throws Exception {
        return bundleApiClient.createBundleServiceRequest(authorization, serviceAuthorization, callbackRequest);
    }

    private String getBundleConfig(YesOrNo welshPreference) {
        if (YesOrNo.Yes.equals(welshPreference)) {
            return bundleWelshConfig;
        }
        return bundleEnglishConfig;
    }
}
