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
import uk.gov.hmcts.reform.prl.models.dto.bundle.Bundle;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.List;

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
    private BundleCreateRequestMapper bundleCreateRequestMapper;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Value("${bundle.english.config}")
    private String bundleEnglishConfig;

    @Value("${bundle.welsh.config}")
    private String bundleWelshConfig;

    public BundleCreateResponse createBundleServiceRequest(CaseData caseData,String eventId,
                                                           String authorization) throws Exception {
        return createBundle(authorization,authTokenGenerator.generate(),
            bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(
                caseData, eventId,
                getBundleConfig(null != caseData.getLanguagePreferenceWelsh() ? caseData.getLanguagePreferenceWelsh() : YesOrNo.No)));
    }

    private BundleCreateResponse createBundle(String authorization, String serviceAuthorization,
                                              BundleCreateRequest bundleCreateRequest) throws Exception {
        return bundleApiClient.createBundleServiceRequest(authorization, serviceAuthorization, bundleCreateRequest);
    }

    public CaseData getCaseDataWithGeneratedPdf(String authorization, String serviceAuthorization,String caseId) {
        CaseData updatedCaseData = null;
        for (int i = 0; i < 5; i++) {
            log.info("*** Invoking the core case data api to get the latest bundle for the case id: {}", caseId);
            updatedCaseData = CaseUtils.getCaseData(coreCaseDataApi.getCase(authorization, serviceAuthorization, caseId), objectMapper);
            String stitchStatus = getBundleStatus(updatedCaseData.getBundleInformation().getCaseBundles());
            if ("NEW".equals(stitchStatus)) {
                try {
                    log.info("*** Invoking Thread.sleep(1000) before retriggering the core case data api for the case id: {}", caseId);
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.info("*** Got exception during Thread.sleep(1000) for the case id: {}", caseId);
                    throw new RuntimeException(e);
                }
                log.info("*** Created Bundle is still NEW for the case id: {}", caseId);
            } else if ("DONE".equals(stitchStatus)) {
                log.info("*** Created Bundle status is DONE for the case id: {}", caseId);
                break;
            } else if ("FAILED".equals(stitchStatus)) {
                log.info("*** Created Bundle is FAILED for the case id: {}", caseId);
                break;
            } else {
                log.info("*** Empty caseBundles recieved for the case id: {}", caseId);
            }
        }
        return  updatedCaseData;
    }

    private String getBundleStatus(List<Bundle> caseBundles) {
        if (null != caseBundles && caseBundles.size() > 0) {
            return caseBundles.get(0).getValue().getStitchStatus();
        } else {
            return "Empty";
        }
    }

    private String getBundleConfig(YesOrNo welshPreference) {
        if (YesOrNo.Yes.equals(welshPreference)) {
            return bundleWelshConfig;
        }
        return bundleEnglishConfig;
    }

}
