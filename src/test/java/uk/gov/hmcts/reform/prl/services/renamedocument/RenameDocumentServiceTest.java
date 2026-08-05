package uk.gov.hmcts.reform.prl.services.renamedocument;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RenameDocument;
import uk.gov.hmcts.reform.prl.services.DocumentCategoryService;
import uk.gov.hmcts.reform.prl.services.documentremoval.DocumentExtractor;
import uk.gov.hmcts.reform.prl.services.sendandreply.SendAndReplyService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CATEGORY_DOCUMENTS_LIST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_URL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NEW_NAME_FOR_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RENAME_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RENAME_DOCUMENTS_LIST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RENAME_LIST_DOC_SELECTED;

@ExtendWith(MockitoExtension.class)
class RenameDocumentServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SendAndReplyService sendAndReplyService;

    @Mock
    private DocumentExtractor documentExtractor;

    @Mock
    private DocumentCategoryService documentCategoryService;

    @InjectMocks
    private RenameDocumentService renameDocumentService;

    private static final String AUTH = "auth";
    private CaseData caseData;
    private CallbackRequest callbackRequest;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder().id(123L).build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(123L)
                .data(new HashMap<>())
                .build())
            .build();
    }

    @Test
    void testHandleAboutToStart() {
        DynamicList documentsList = DynamicList.builder().listItems(new ArrayList<>()).build();
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
        when(sendAndReplyService.getCategoriesAndDocuments(AUTH, "123")).thenReturn(documentsList);
        when(documentCategoryService.retrieveDocumentCategories(AUTH, caseData, null)).thenReturn(DynamicList.builder().build());
        when(documentExtractor.getCaseDocuments(any())).thenReturn(Collections.emptyList());

        Map<String, Object> result = renameDocumentService.handleAboutToStart(AUTH, callbackRequest);

        assertNotNull(result);
        assertTrue(result.containsKey(RENAME_DOCUMENTS_LIST));
        assertTrue(result.containsKey(CATEGORY_DOCUMENTS_LIST));
    }

    @Test
    void testHandleMidEvent() {
        DynamicListElement element = DynamicListElement.builder().code("categoryCode -> documentId").label("categoryName -> documentName").build();
        DynamicList selectedList = DynamicList.builder().value(element).listItems(List.of(element)).build();

        RenameDocument renameDocument = RenameDocument.builder()
            .renameDocumentsList(selectedList)
            .build();
        caseData = caseData.toBuilder().renameDocument(renameDocument).build();

        DynamicList categoriesAndDocumentsList = DynamicList.builder()
            .listItems(List.of(DynamicListElement.builder().code("categoryCode").label("Category 1").build()))
            .build();

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
        when(documentCategoryService.retrieveDocumentCategories(AUTH, caseData, null)).thenReturn(categoriesAndDocumentsList);

        Map<String, Object> result = renameDocumentService.handleMidEvent(AUTH, callbackRequest);

        assertNotNull(result);
        assertEquals("categoryName -> documentName", result.get(RENAME_LIST_DOC_SELECTED));
        DynamicList updatedCategories = (DynamicList) result.get(CATEGORY_DOCUMENTS_LIST);
        assertEquals("categoryCode", updatedCategories.getValue().getCode());
    }

    @Test
    void testHandleAboutToSubmit() {
        DynamicListElement selectedDoc = DynamicListElement.builder().code("categoryCode -> documentId1").build();
        DynamicListElement selectedCategory = DynamicListElement.builder().code("newCategoryId").build();

        RenameDocument renameDocument = RenameDocument.builder()
            .renameDocumentsList(DynamicList.builder().value(selectedDoc).build())
            .categoryDocumentsList(DynamicList.builder().value(selectedCategory).build())
            .newNameForDocument("New Name")
            .build();
        caseData = caseData.toBuilder().renameDocument(renameDocument).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        Map<String, Object> documentMap = createDocumentMap("documentId1", "old_name.pdf");
        caseDataMap.put("someDocument", documentMap);

        callbackRequest.getCaseDetails().setData(caseDataMap);

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        Map<String, Object> result = renameDocumentService.handleAboutToSubmit(callbackRequest);

        assertNotNull(result);
        assertEquals("New Name.pdf", ((Map<String, Object>)result.get("someDocument")).get(DOCUMENT_FILENAME));
        assertEquals("newCategoryId", ((Map<String, Object>)result.get("someDocument")).get("category_id"));
        assertFalse(result.containsKey(RENAME_DOCUMENT));
    }

    @Test
    void testHandleAboutToSubmitInList() {
        DynamicListElement selectedDoc = DynamicListElement.builder().code("documentId1").build();
        RenameDocument renameDocument = RenameDocument.builder()
            .renameDocumentsList(DynamicList.builder().value(selectedDoc).build())
            .newNameForDocument("New Name")
            .build();
        caseData = caseData.toBuilder().renameDocument(renameDocument).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        List<Map<String, Object>> documentList = new ArrayList<>();
        Map<String, Object> documentMap = createDocumentMap("documentId1", "old_name.pdf");
        documentList.add(Map.of("value", documentMap));
        caseDataMap.put("docList", documentList);

        callbackRequest.getCaseDetails().setData(caseDataMap);
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        renameDocumentService.handleAboutToSubmit(callbackRequest);

        Map<String, Object> updatedDoc = (Map<String, Object>) ((Map<String, Object>)documentList.get(0)).get("value");
        assertEquals("New Name.pdf", updatedDoc.get(DOCUMENT_FILENAME));
    }

    @Test
    void testCreateDynamicListForRenameDocumentsFiltersReviewDocuments() {
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        List<DynamicListElement> listItems = new ArrayList<>();
        listItems.add(DynamicListElement.builder().code("1").label("Standard Document.pdf").build());
        listItems.add(DynamicListElement.builder().code("2").label("Documents to be reviewed -> test.pdf").build());

        DynamicList documentsList = DynamicList.builder().listItems(listItems).build();

        when(sendAndReplyService.getCategoriesAndDocuments(any(), any())).thenReturn(documentsList);
        when(documentExtractor.getCaseDocuments(any())).thenReturn(Collections.emptyList());
        when(documentCategoryService.retrieveDocumentCategories(anyString(), any(), any())).thenReturn(DynamicList.builder().build());

        Map<String, Object> result = renameDocumentService.handleAboutToStart(AUTH, callbackRequest);

        DynamicList filteredList = (DynamicList) result.get(RENAME_DOCUMENTS_LIST);

        assertEquals(1, filteredList.getListItems().size());
        assertEquals("Standard Document.pdf", filteredList.getListItems().get(0).getLabel());
    }

    @Test
    void testCreateDynamicListAddsExtraDocuments() {
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
        when(sendAndReplyService.getCategoriesAndDocuments(any(), any())).thenReturn(DynamicList.builder().listItems(new ArrayList<>()).build());

        Document doc = Document.builder().documentFileName("extra.pdf").documentUrl("http://doc/uuid1").build();
        when(documentExtractor.getCaseDocuments(any(CaseData.class)))
            .thenReturn(List.of(doc))
            .thenReturn(Collections.emptyList());

        when(documentCategoryService.retrieveDocumentCategories(anyString(), any(), any())).thenReturn(DynamicList.builder().build());

        when(documentCategoryService.retrieveDocumentCategories(anyString(), any(), any())).thenReturn(DynamicList.builder().build());

        Map<String, Object> result = renameDocumentService.handleAboutToStart(AUTH, callbackRequest);

        DynamicList filteredList = (DynamicList) result.get(RENAME_DOCUMENTS_LIST);
        assertTrue(filteredList.getListItems().stream().anyMatch(item -> item.getLabel().equals("extra.pdf")));
    }

    @Test
    void testValidateRenamedFieldWithExtensionError() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(NEW_NAME_FOR_DOCUMENT, "test.pdf");

        List<String> errors = renameDocumentService.validateRenamedField(caseDataUpdated);

        assertEquals(1, errors.size());
        assertEquals("Document name must not include the file type", errors.get(0));
    }

    @Test
    void testHandleMidEventWithNullRenameDocument() {
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
        when(documentCategoryService.retrieveDocumentCategories(AUTH, caseData, null)).thenReturn(DynamicList.builder().build());

        Map<String, Object> result = renameDocumentService.handleMidEvent(AUTH, callbackRequest);

        assertNotNull(result);
        assertTrue(result.containsKey(CATEGORY_DOCUMENTS_LIST));
    }

    @Test
    void testHandleAboutToSubmitWithNonMatchingDoc() {
        DynamicListElement selectedDoc = DynamicListElement.builder().code("docId1").build();
        RenameDocument renameDocument = RenameDocument.builder()
            .renameDocumentsList(DynamicList.builder().value(selectedDoc).build())
            .newNameForDocument("New Name")
            .build();
        caseData = caseData.toBuilder().renameDocument(renameDocument).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        Map<String, Object> docMap = new HashMap<>();
        docMap.put(DOCUMENT_URL, "http://dm-store/otherDocId");
        docMap.put(DOCUMENT_FILENAME, "old_name.pdf");
        caseDataMap.put("someDocument", docMap);

        callbackRequest.getCaseDetails().setData(caseDataMap);
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        Map<String, Object> result = renameDocumentService.handleAboutToSubmit(callbackRequest);

        assertEquals("old_name.pdf", ((Map<String, Object>)result.get("someDocument")).get(DOCUMENT_FILENAME));
    }

    @Test
    void testFindAndRenameDocumentWithNestedMap() {
        DynamicListElement selectedDoc = DynamicListElement.builder().code("docId1").build();
        RenameDocument renameDocument = RenameDocument.builder()
            .renameDocumentsList(DynamicList.builder().value(selectedDoc).build())
            .newNameForDocument("New Name")
            .build();
        caseData = caseData.toBuilder().renameDocument(renameDocument).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        Map<String, Object> outerMap = new HashMap<>();
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put(DOCUMENT_URL, "http://dm-store/docId1");
        innerMap.put(DOCUMENT_FILENAME, "old_name.pdf");
        outerMap.put("inner", innerMap);
        caseDataMap.put("outer", outerMap);

        callbackRequest.getCaseDetails().setData(caseDataMap);
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        renameDocumentService.handleAboutToSubmit(callbackRequest);

        assertEquals("New Name.pdf", ((Map<String, Object>)outerMap.get("inner")).get(DOCUMENT_FILENAME));
    }

    @Test
    void testValidateRenamedFieldWithNoDocumentSelected() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put("newNameForDocument", "Confidential_new_name");

        List<String> errors = renameDocumentService.validateRenamedField(caseDataUpdated);

        assertTrue(errors.isEmpty());
    }

    @Test
    void testValidateRenamedFieldWithConfidentialPrefixAndNoNewName() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(RENAME_LIST_DOC_SELECTED, "Applications -> Applicant documents -> Confidential_test.pdf");

        List<String> errors = renameDocumentService.validateRenamedField(caseDataUpdated);

        assertTrue(errors.isEmpty());
    }

    @Test
    void testHandleAboutToSubmitWithUncategorizedAndCategorized() {
        String documentId = "docId1";
        DynamicListElement selectedDoc = DynamicListElement.builder().code(documentId).build();
        DynamicListElement selectedCategory = DynamicListElement.builder().code("newCategoryId").build();

        RenameDocument renameDocument = RenameDocument.builder()
            .renameDocumentsList(DynamicList.builder().value(selectedDoc).build())
            .categoryDocumentsList(DynamicList.builder().value(selectedCategory).build())
            .newNameForDocument("New Name")
            .build();
        caseData = caseData.toBuilder().renameDocument(renameDocument).build();

        Map<String, Object> caseDataMap = new HashMap<>();

        Map<String, Object> categorisedDocumentMap = createDocumentMap(documentId, "categorised_old_name.pdf");
        addCategorisedDocument(caseDataMap, categorisedDocumentMap);

        Map<String, Object> uncategorisedDocumentMap = createDocumentMap(documentId, "uncategorisedDocument_old_name.pdf");
        addUncategorisedDocument(caseDataMap, uncategorisedDocumentMap);

        callbackRequest.getCaseDetails().setData(caseDataMap);
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        renameDocumentService.handleAboutToSubmit(callbackRequest);

        assertEquals("New Name.pdf", categorisedDocumentMap.get(DOCUMENT_FILENAME));
        assertEquals("New Name.pdf", uncategorisedDocumentMap.get(DOCUMENT_FILENAME));

        assertEquals("newCategoryId", categorisedDocumentMap.get("category_id"));
        assertFalse(uncategorisedDocumentMap.containsKey("category_id"));
    }

    @Test
    void testHandleAboutToSubmitWithOnlyUncategorized() {
        String documentId = "docId1";
        DynamicListElement selectedDoc = DynamicListElement.builder().code(documentId).build();
        DynamicListElement selectedCategory = DynamicListElement.builder().code("newCategoryId").build();

        RenameDocument renameDocument = RenameDocument.builder()
            .renameDocumentsList(DynamicList.builder().value(selectedDoc).build())
            .categoryDocumentsList(DynamicList.builder().value(selectedCategory).build())
            .newNameForDocument("New Name")
            .build();
        caseData = caseData.toBuilder().renameDocument(renameDocument).build();

        Map<String, Object> caseDataMap = new HashMap<>();

        Map<String, Object> uncategorisedDocumentMap = createDocumentMap(documentId, "uncat_old_name.pdf");
        addUncategorisedDocument(caseDataMap, uncategorisedDocumentMap);

        callbackRequest.getCaseDetails().setData(caseDataMap);
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        renameDocumentService.handleAboutToSubmit(callbackRequest);

        assertEquals("New Name.pdf", uncategorisedDocumentMap.get(DOCUMENT_FILENAME));
        assertEquals("newCategoryId", uncategorisedDocumentMap.get("category_id"));
    }

    private Map<String, Object> createDocumentMap(String documentId, String fileName) {
        Map<String, Object> docMap = new HashMap<>();
        docMap.put(DOCUMENT_URL, "http://dm-store/" + documentId);
        docMap.put(DOCUMENT_FILENAME, fileName);
        return docMap;
    }

    private void addCategorisedDocument(Map<String, Object> caseDataMap, Map<String, Object> documentMap) {
        List<Map<String, Object>> documentList = new ArrayList<>();
        documentList.add(new HashMap<>(Map.of("value", documentMap)));
        caseDataMap.put("scannedDocuments", documentList);
    }

    private void addUncategorisedDocument(Map<String, Object> caseDataMap, Map<String, Object> documentMap) {
        Map<String, Object> packMap = new HashMap<>();
        List<Map<String, Object>> packList = new ArrayList<>();
        packList.add(new HashMap<>(Map.of("value", documentMap)));
        packMap.put("packDocument", packList);
        caseDataMap.put("unServedApplicantPack", packMap);
    }
}
