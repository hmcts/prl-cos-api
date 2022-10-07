package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.*;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.bundle.BundlingService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BundlingControllerTest {

    @InjectMocks
    private BundlingController bundlingController;
    @Mock
    private BundlingService bundlingService;

    private BundleCreateResponse bundleCreateResponse;
    private CaseDetails caseDetails;
    private AboutToStartOrSubmitCallbackResponse response;

    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setUp() {
        List<Bundle> bundleList = new ArrayList<>();
        List<BundleFolder> bundleFolders =new ArrayList<>();
        List<BundleSubfolder> bundleSubfolders = new ArrayList<>();
        List<BundleDocument> bundleDocuments = new ArrayList<>();
        bundleDocuments.add(BundleDocument.builder().value(BundleDocumentDetails.builder().name("CaseDocuments").description("Case Documents").sortIndex(1).
                                                               sourceDocument(DocumentLink.builder().build()).build()).build());
        bundleSubfolders.add(BundleSubfolder.builder().value(BundleSubfolderDetails.builder().documents(bundleDocuments).build()).build());
        bundleFolders.add(BundleFolder.builder().value(BundleFolderDetails.builder().folders(bundleSubfolders).build()).build());
        bundleList.add(Bundle.builder().value(BundleDetails.builder().folders(bundleFolders).build()).build());
        bundleCreateResponse = BundleCreateResponse.builder().data(BundleData.builder().id("334").caseBundles(bundleList).build()).build();
        Map<String,Object> caseData = new HashMap<>();
        caseData.put("bundles",bundleCreateResponse);
        caseDetails=CaseDetails.builder().data(caseData).build();
    }
    @Test
    public void testCreateBundle() throws Exception{
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(bundlingService.createBundleServiceRequest(callbackRequest,authToken)).thenReturn(bundleCreateResponse);
        response = bundlingController.createBundle(authToken,callbackRequest);
        BundleCreateResponse responserecieved =(BundleCreateResponse) response.getData().get("bundles");
        assertEquals("CaseDocuments",responserecieved.getData().getCaseBundles().get(0).getValue().getFolders().get(0).getValue().getFolders().get(0).getValue().getDocuments().get(0).getValue().getName());
    }
}
