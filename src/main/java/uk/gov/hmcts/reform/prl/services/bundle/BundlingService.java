package uk.gov.hmcts.reform.prl.services.bundle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.BundleApiClient;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.bundle.BundleCreateRequestMapper;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleDocumentDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingCaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.DocumentLink;
import uk.gov.hmcts.reform.prl.models.dto.bundle.stitch.Bundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.enums.State.DECISION_OUTCOME;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

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
        /*return createBundle(authorization,authTokenGenerator.generate(),
            bundleCreateRequestMapper.mapCaseDataToBundleCreateRequest(
                caseData, eventId, hearingDetails,
                getBundleConfig(null != caseData.getLanguagePreferenceWelsh() ? caseData.getLanguagePreferenceWelsh() : YesOrNo.No)));*/

        return createBundle(authorization,authTokenGenerator.generate(),
           stitchDocumentRequest(caseData));
    }

    private BundleCreateResponse createBundle(String authorization, String serviceAuthorization,
                                              BundleCreateRequest bundleCreateRequest) {
        BundleCreateResponse bundleCreateResponse = null;
        try {
            log.info("**** calling API with request ****** {}", bundleCreateRequest);

            //bundleCreateResponse = bundleApiClient.createBundleServiceRequest(authorization, serviceAuthorization, bundleCreateRequest);
            //log.info("calling New bundle API successful");

            bundleCreateResponse = bundleApiClient.createStitchServiceRequest(authorization, serviceAuthorization, bundleCreateRequest);
            log.info("**************** Bundle response  for new bundle api ****************** {}", bundleCreateResponse.getData().getCaseBundles());
        } catch (Exception e) {
            log.info("Error calling stitching API");
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




    private BundleCreateRequest stitchDocumentRequest(CaseData caseData) {

        Document document = null;
        Document c1ADocument = null;

        if (null != caseData.getFinalDocument()) {

            document = caseData.getFinalDocument();

        }
        if (null != caseData.getC1ADocument()) {

            c1ADocument = caseData.getC1ADocument();

        }

        final Bundle bundle = Bundle.builder()
            .documents(wrapElements(getBundleDocumentDetails(document, c1ADocument)))
            .build();

        final BundlingCaseData bundlingCaseData = BundlingCaseData.builder().id(String.valueOf(caseData.getId()))
            .caseBundles(wrapElements(bundle))
            .build();


        return BundleCreateRequest.builder()
            .caseDetails(BundlingCaseDetails.builder()
                             .id(caseData.getApplicantName())
                             .caseData(bundlingCaseData)
                             .build())
            .caseTypeId(CASE_TYPE).jurisdictionId(JURISDICTION).build();

    }

    private List<BundleDocumentDetails> getBundleDocumentDetails(Document document, Document c1ADocument) {

        List<BundleDocumentDetails> bundleDocumentDetailsList = new ArrayList<>();

        final BundleDocumentDetails bundleDocumentDetails;
        final BundleDocumentDetails bundleDocumentDetails1;
        if (document != null && c1ADocument != null) {
            bundleDocumentDetails = BundleDocumentDetails.builder()
                .sourceDocument(DocumentLink.builder()
                                    .documentUrl(document.getDocumentUrl())
                                    .documentBinaryUrl(document.getDocumentBinaryUrl())
                                    .documentFilename(document.getDocumentFileName())
                                    .build())
                .build();

            bundleDocumentDetails1 = BundleDocumentDetails.builder()
                .sourceDocument(DocumentLink.builder()
                                    .documentUrl(c1ADocument.getDocumentUrl())
                                    .documentBinaryUrl(c1ADocument.getDocumentBinaryUrl())
                                    .documentFilename(c1ADocument.getDocumentFileName())
                                    .build())
                .build();

            bundleDocumentDetailsList.add(bundleDocumentDetails);
            bundleDocumentDetailsList.add(bundleDocumentDetails1);
        }
        return bundleDocumentDetailsList;
    }
}
