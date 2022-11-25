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
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.FurtherEvidenceDocumentType;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
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
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingInformation;
import uk.gov.hmcts.reform.prl.models.dto.bundle.DocumentLink;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.bundle.BundlingService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_POSITION_STATEMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YOUR_WITNESS_STATEMENTS;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

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
    @Mock
    private AboutToStartOrSubmitCallbackResponse response;

    public static final String authToken = "Bearer TestAuthToken";
    private CaseData c100CaseData;

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
        caseData.put("bundleInformation",bundleCreateResponse.getData().getCaseBundles());
        caseDetails = CaseDetails.builder().data(caseData).state(State.CASE_HEARING.getValue())
            .id(123488888L).createdDate(LocalDateTime.now()).lastModified(LocalDateTime.now()).build();
        List<FurtherEvidence> furtherEvidences = new ArrayList<>();
        furtherEvidences.add(FurtherEvidence.builder().typeOfDocumentFurtherEvidence(FurtherEvidenceDocumentType.miamCertificate)
            .documentFurtherEvidence(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Sample1.pdf").build())
            .restrictCheckboxFurtherEvidence(new ArrayList<>()).build());

        List<OtherDocuments> otherDocuments = new ArrayList<>();
        otherDocuments.add(OtherDocuments.builder().documentName("Application docu")
            .documentOther(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Sample2.pdf").build()).documentTypeOther(
                DocTypeOtherDocumentsEnum.applicantStatement).restrictCheckboxOtherDocuments(new ArrayList<>()).build());

        List<OrderDetails> orders = new ArrayList<>();
        orders.add(OrderDetails.builder().orderType("orders")
            .orderDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("Order.pdf").build()).build());

        List<ResponseDocuments> citizenC7uploadedDocs = new ArrayList<>();
        citizenC7uploadedDocs.add(ResponseDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("C7Document.pdf").build()).build());

        List<UploadedDocuments> uploadedDocuments = new ArrayList<>();
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("PositionStatement.pdf").build())
                .documentType(YOUR_POSITION_STATEMENTS).isApplicant("No").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("PositionStatement.pdf").build())
            .documentType(YOUR_POSITION_STATEMENTS).isApplicant("Yes").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("WitnessStatement.pdf").build())
            .documentType(YOUR_WITNESS_STATEMENTS).isApplicant("Yes").build());
        uploadedDocuments.add(UploadedDocuments.builder()
            .citizenDocument(Document.builder().documentUrl("url").documentBinaryUrl("url").documentFileName("WitnessStatement.pdf").build())
            .documentType(YOUR_WITNESS_STATEMENTS).isApplicant("No").build());

        //uploadedDocuments.add(uploadedDocuments);
        c100CaseData = CaseData.builder()
            .id(123456789123L)
            .languagePreferenceWelsh(No)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.CASE_HEARING)
            .finalDocument(Document.builder().documentFileName("C100AppDoc").documentUrl("Url").build())
            .c1ADocument(Document.builder().documentFileName("c1ADocument").documentUrl("Url").build())
            //.allegationsOfHarmYesNo(No)
            .otherDocuments(ElementUtils.wrapElements(otherDocuments))
            .furtherEvidences(ElementUtils.wrapElements(furtherEvidences))
            .orderCollection(ElementUtils.wrapElements(orders))
            .bundleInformation(BundlingInformation.builder().build())
            .citizenResponseC7DocumentList(ElementUtils.wrapElements(citizenC7uploadedDocs))
            .citizenUploadedDocumentList(ElementUtils.wrapElements(uploadedDocuments))
            .bundleInformation(BundlingInformation.builder().bundleConfiguration("sample.yaml").historicalBundles(bundleList).build())
            //.home(homefull)
            .build();
    }

    @Test
    public void testCreateBundle() throws Exception {
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(c100CaseData);
        when(bundlingService.createBundleServiceRequest(any(CaseData.class),anyString(),anyString())).thenReturn(bundleCreateResponse);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).eventId("eventId").build();
        response = bundlingController.createBundle(authToken,"serviceAuth",callbackRequest);
        BundlingInformation bundleInformation = (BundlingInformation) response.getData().get("bundleInformation");
        List<Bundle> responseCaseBundles = bundleInformation.getCaseBundles();
        assertEquals("CaseDocuments",
            responseCaseBundles.get(0).getValue().getFolders().get(0)
                    .getValue().getFolders().get(0).getValue().getDocuments().get(0).getValue().getName());
    }
}
