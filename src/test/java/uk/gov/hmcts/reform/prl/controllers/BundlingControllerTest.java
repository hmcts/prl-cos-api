package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.bundle.*;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.services.bundle.BundlingService;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BundlingControllerTest {

    @InjectMocks
    private BundlingController bundlingController;
    @Mock
    private BundlingService bundlingService;
    private CaseData caseData;
    private PreSubmitCallbackResponse<CaseData> preSubmitCallbackResponse;
    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setUp() {
        caseData = CaseData.builder().build();
        caseData.setBundleConfiguration("bundle.yml");
        CaseData responseCaseData = CaseData.builder().build();
        List<Bundle> bundleList = new ArrayList<>();
        List<BundleFolder> bundleFolders =new ArrayList<>();
        List<BundleSubfolder> bundleSubfolders = new ArrayList<>();
        List<BundleDocument> bundleDocuments = new ArrayList<>();
        bundleDocuments.add(BundleDocument.builder().value(BundleDocumentDetails.builder().name("CaseDocuments").description("Case Documents").sortIndex(1).
                                                               sourceDocument(DocumentLink.builder().build()).build()).build());
        bundleSubfolders.add(BundleSubfolder.builder().value(BundleSubfolderDetails.builder().documents(bundleDocuments).build()).build());
        bundleFolders.add(BundleFolder.builder().value(BundleFolderDetails.builder().folders(bundleSubfolders).build()).build());
        bundleList.add(Bundle.builder().value(BundleDetails.builder().folders(bundleFolders).build()).build());
        responseCaseData.setCaseBundles(bundleList);
        preSubmitCallbackResponse = new PreSubmitCallbackResponse<>(responseCaseData);

    }
    @Test
    public void testCreateBundle() throws Exception{
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(CaseDetails.builder().caseData(caseData).build()).build();
        when(bundlingService.createBundleServiceRequest(callbackRequest,authToken)).thenReturn(preSubmitCallbackResponse);
        bundlingController.createBundle(authToken,callbackRequest);
        assertEquals("CaseDocuments",preSubmitCallbackResponse.getData().getCaseBundles().get(0).getValue().getFolders().get(0).getValue().getFolders().get(0).getValue().getDocuments().get(0).getValue().getName());

    }
}
