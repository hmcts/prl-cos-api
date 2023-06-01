package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarentineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.services.ManageOrderService;
import uk.gov.hmcts.reform.prl.services.TaskListRenderElements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ReviewDocumentsControllerTest {

    @InjectMocks
    ReviewDocumentsController reviewDocumentsController;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ManageOrderService manageOrderService;

    @Mock
    private TaskListRenderElements taskListRenderElements;

    public static final String authToken = "Bearer TestAuthToken";
    private CaseData caseData;
    private GeneratedDocumentInfo generatedDocumentInfo;
    private Element<QuarentineLegalDoc> quarantineLegalDocElement;
    private Element<QuarentineLegalDoc> quarantineLegalDocElement1;
    private List<Element<QuarentineLegalDoc>> quarantineLegalList = new ArrayList<>();
    private Element<UploadedDocuments> uploadedDocumentsElement;
    private List<Element<UploadedDocuments>> listOfUploadedDocuments;
    private ReviewDocuments reviewDocuments;
    private DynamicList dynamicList;

    @Before
    public void setUp() {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        QuarentineLegalDoc quarentineLegalDoc = QuarentineLegalDoc.builder()
            .documentName("test doc name")
            .notes("test Notes")
            .document(Document.builder()
                          .documentUrl(generatedDocumentInfo.getUrl())
                          .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                          .documentHash(generatedDocumentInfo.getHashToken())
                          .documentFileName("testDraftFileName.pdf")
                          .build())
            .category("test category")
            .documentType("test doc type")
            .restrictCheckboxCorrespondence(Collections.singletonList(RestrictToCafcassHmcts.restrictToGroup))
            .documentParty("test doc")
            .applicantApplicationDocument(Document.builder()
                                              .documentUrl(generatedDocumentInfo.getUrl())
                                              .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                              .documentHash(generatedDocumentInfo.getHashToken())
                                              .documentFileName("testApplicantApplicationDoc.pdf")
                                              .build())
            .build();

        quarantineLegalDocElement = Element.<QuarentineLegalDoc>builder()
            .id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
            .value(quarentineLegalDoc).build();

        QuarentineLegalDoc quarentineLegalDoc1 = QuarentineLegalDoc.builder()
            .documentName("test doc name1")
            .notes("test Notes1")
            .document(Document.builder()
                          .documentUrl(generatedDocumentInfo.getUrl())
                          .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                          .documentHash(generatedDocumentInfo.getHashToken())
                          .documentFileName("testDraftFileName1.pdf")
                          .build())
            .category("test category1")
            .documentType("test doc type1")
            .restrictCheckboxCorrespondence(Collections.singletonList(RestrictToCafcassHmcts.restrictToGroup))
            .documentParty("test doc1")
            .applicantApplicationDocument(Document.builder()
                                              .documentUrl(generatedDocumentInfo.getUrl())
                                              .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                              .documentHash(generatedDocumentInfo.getHashToken())
                                              .documentFileName("testApplicantApplicationDoc1.pdf")
                                              .build())
            .build();

        quarantineLegalDocElement1 = Element.<QuarentineLegalDoc>builder()
            .id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
            .value(quarentineLegalDoc1).build();

        quarantineLegalList.add(quarantineLegalDocElement);
        quarantineLegalList.add(quarantineLegalDocElement1);

        Document document = Document.builder().documentUrl("")
            .documentFileName("test")
            .build();

        UploadedDocuments uploadedDocuments = UploadedDocuments.builder()
            .documentType("test")
            .partyName("test")
            .documentRequestedByCourt(YesOrNo.Yes)
            .parentDocumentType("Parent")
            .citizenDocument(document)
            .build();
        uploadedDocumentsElement = Element.<UploadedDocuments>builder()
            .id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
            .value(uploadedDocuments).build();
        listOfUploadedDocuments = new ArrayList<>(List.of(uploadedDocumentsElement));

        Map<String, Object> stringObjectMap = new HashMap<>();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        DynamicListElement dynamicListElement = DynamicListElement.builder().code(UUID.fromString("00000000-0000-0000-0000-000000000000")).label(" ").build();
        dynamicList = DynamicList.builder()
            .listItems(List.of(dynamicListElement))
            .value(dynamicListElement)
            .build();

        reviewDocuments = ReviewDocuments.builder()
            .docToBeReviewed("Test Doc to be reviewed")
            .reviewDoc(Document.builder()
                           .documentUrl(generatedDocumentInfo.getUrl())
                           .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                           .documentHash(generatedDocumentInfo.getHashToken())
                           .documentFileName("testReviewDoc.pdf")
                           .build())
            .reviewDecisionYesOrNo(YesNoDontKnow.yes)
            .reviewDocsDynamicList(dynamicList)
            .citizenUploadDocListConfTab(listOfUploadedDocuments)
            .legalProfUploadDocListConfTab(quarantineLegalList)
            .citizenUploadedDocListDocTab(listOfUploadedDocuments)
            .legalProfUploadDocListDocTab(quarantineLegalList)
            .build();

    }

    @Test
    public void testForAboutToStart() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("test case")
            .legalProfQuarentineDocsList(quarantineLegalList)
            .citizenUploadQuarentineDocsList(new ArrayList<>(List.of(uploadedDocumentsElement)))
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = reviewDocumentsController.handleAboutToStart(authToken,
                                                                                                                                     callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("reviewDocsDynamicList"));

    }

    @Test
    public void testForAboutToStartEmptyLists() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("test case")
            .legalProfQuarentineDocsList(null)
            .citizenUploadQuarentineDocsList(null)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = reviewDocumentsController.handleAboutToStart(authToken,
                                                                                                                                 callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getErrors());

    }

    @Test
    public void testForMidEventInReviewDocumentsWithLegalQuaratineElements() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("test case")
            .legalProfQuarentineDocsList(quarantineLegalList)
            .citizenUploadQuarentineDocsList(new ArrayList<>(List.of(uploadedDocumentsElement)))
            .reviewDocuments(reviewDocuments)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = reviewDocumentsController.handleMidEvent(authToken,
                                                                                                                                 callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("docToBeReviewed"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("reviewDoc"));

    }

    @Test
    public void testForMidEventInReviewDocumentsWithCitizenDocumentLists() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("test case")
            .legalProfQuarentineDocsList(null)
            .citizenUploadQuarentineDocsList(new ArrayList<>(List.of(uploadedDocumentsElement)))
            .reviewDocuments(reviewDocuments)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = reviewDocumentsController.handleMidEvent(authToken,
                                                                                                                             callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("docToBeReviewed"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("reviewDoc"));

    }

    @Test
    public void testForAboutToSubmitWithReviewDocDecisionAsYes() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("test case")
            .legalProfQuarentineDocsList(quarantineLegalList)
            .citizenUploadQuarentineDocsList(new ArrayList<>(List.of(uploadedDocumentsElement)))
            .reviewDocuments(reviewDocuments)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = reviewDocumentsController.handleAboutToSubmit(authToken,
                                                                                                                             callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("citizenUploadDocListConfTab"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("citizenUploadedDocListDocTab"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("legalProfUploadDocListConfTab"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("legalProfUploadDocListDocTab"));

    }

    @Test
    public void testForAboutToSubmitWithReviewDocDecisionAsNo() throws Exception {

        reviewDocuments = reviewDocuments.toBuilder()
            .reviewDecisionYesOrNo(YesNoDontKnow.no)
            .build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("test case")
            .legalProfQuarentineDocsList(quarantineLegalList)
            .citizenUploadQuarentineDocsList(new ArrayList<>(List.of(uploadedDocumentsElement)))
            .reviewDocuments(reviewDocuments)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = reviewDocumentsController.handleAboutToSubmit(authToken,
                                                                                                                                  callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("citizenUploadDocListConfTab"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("citizenUploadedDocListDocTab"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("legalProfUploadDocListConfTab"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("legalProfUploadDocListDocTab"));

    }

    @Test
    public void testForAboutToSubmitWithAllTabsINReviewDocsAsNull() throws Exception {

        reviewDocuments = reviewDocuments.toBuilder()
            .reviewDecisionYesOrNo(YesNoDontKnow.no)
            .legalProfUploadDocListDocTab(null)
            .legalProfUploadDocListConfTab(null)
            .citizenUploadedDocListDocTab(null)
            .citizenUploadDocListConfTab(null)
            .build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("test case")
            .legalProfQuarentineDocsList(quarantineLegalList)
            .citizenUploadQuarentineDocsList(new ArrayList<>(List.of(uploadedDocumentsElement)))
            .reviewDocuments(reviewDocuments)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = reviewDocumentsController.handleAboutToSubmit(authToken,
                                                                                                                                  callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("citizenUploadDocListConfTab"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("citizenUploadedDocListDocTab"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("legalProfUploadDocListConfTab"));
        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().containsKey("legalProfUploadDocListDocTab"));

    }
}
