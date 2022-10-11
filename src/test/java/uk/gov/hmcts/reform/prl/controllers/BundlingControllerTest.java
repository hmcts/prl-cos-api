package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.bundle.Bundle;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateResponse;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleDocument;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleDocumentDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleFolder;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleFolderDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleSubfolder;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleSubfolderDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.DocumentLink;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.bundle.BundlingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BundlingControllerTest {

    @InjectMocks
    private BundlingController bundlingController;
    @Mock
    private BundlingService bundlingService;

    @Mock
    private ObjectMapper objectMapper;

    private BundleCreateResponse bundleCreateResponse;
    private CaseDetails caseDetails;

    private Map<String,Object> caseData;
    private AboutToStartOrSubmitCallbackResponse response;

    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setUp() {
        List<BundleFolder> bundleFolders = new ArrayList<>();
        List<BundleSubfolder> bundleSubfolders = new ArrayList<>();
        List<BundleDocument> bundleDocuments = new ArrayList<>();
        bundleDocuments.add(BundleDocument.builder().value(
            BundleDocumentDetails.builder().name("CaseDocuments").description("Case Documents").sortIndex(1)
            .sourceDocument(DocumentLink.builder().build()).build()).build());
        bundleSubfolders.add(BundleSubfolder.builder().value(BundleSubfolderDetails.builder().documents(bundleDocuments).build()).build());
        bundleFolders.add(BundleFolder.builder().value(BundleFolderDetails.builder().folders(bundleSubfolders).build()).build());
        List<Bundle> bundleList = new ArrayList<>();
        bundleList.add(Bundle.builder().value(BundleDetails.builder().folders(bundleFolders).build()).build());
        bundleCreateResponse = BundleCreateResponse.builder().data(BundleData.builder().id("334").caseBundles(bundleList).build()).build();
        caseData = new HashMap<>();
        caseData.put("caseBundles",bundleCreateResponse);
        caseDetails = CaseDetails.builder().data(caseData).state(State.GATEKEEPING.getValue())
            .id(123488888L).createdDate(LocalDateTime.now()).lastModified(LocalDateTime.now()).build();
    }

    @Test
    public void testCreateBundle() throws Exception {
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(CaseData.builder().build());
        when(bundlingService.createBundleServiceRequest(any(CaseData.class),anyString())).thenReturn(bundleCreateResponse);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        response = bundlingController.createBundle(authToken,callbackRequest);
        BundleCreateResponse responserecieved = (BundleCreateResponse) response.getData().get("caseBundles");
        assertEquals("CaseDocuments",
            responserecieved.getData().getCaseBundles().get(0).getValue().getFolders().get(0)
                    .getValue().getFolders().get(0).getValue().getDocuments().get(0).getValue().getName());
    }
}
