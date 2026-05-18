package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentRemovalWrapper;
import uk.gov.hmcts.reform.prl.services.DeleteDocumentService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.documentremoval.postabouttosubmitaction.DocumentRemovalAboutToSubmitAction;
import uk.gov.hmcts.reform.prl.services.documentremoval.submittedaction.DocumentRemovalSubmittedAction;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentRemovalServiceTest {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private DocumentExtractor documentRetriever;
    @Mock
    private DocumentRemover documentRemover;
    @Mock
    private DeleteDocumentService deleteDocumentService;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private DocumentRemovalAboutToSubmitAction documentRemovalAboutToSubmitAction;
    @Mock
    private DocumentRemovalSubmittedAction documentRemovalSubmittedAction;

    private DocumentRemovalService documentRemovalService;

    @Mock private CaseDetails caseDetails;

    private Document document;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        documentRemovalService = new DocumentRemovalService(objectMapper, documentRetriever, documentRemover,
                                                            deleteDocumentService, systemUserService,
                                                            List.of(documentRemovalAboutToSubmitAction),
                                                            List.of(documentRemovalSubmittedAction));
        document = Document.builder()
            .documentUrl("http://someserver/doc1")
            .documentFileName("file1.pdf")
            .uploadTimeStamp(LocalDateTime.parse("2007-12-03T10:15:30"))
            .build();

        DynamicListElement element = DynamicListElement.builder()
            .code("doc1")
            .label("file1.pdf")
            .build();

        DynamicList dynamicList = DynamicList.builder()
            .value(element)
            .listItems(List.of(element))
            .build();

        caseData = CaseData.builder()
            .documentRemovalWrapper(DocumentRemovalWrapper.builder()
                .documentRemovalCaseDocuments(dynamicList)
                .build())
            .build();
    }

    @Test
    void testGetCaseDocuments() {
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
        when(documentRetriever.getCaseDocuments(caseData)).thenReturn(List.of(document));

        Map<String, Object> result = documentRemovalService.getCaseDocuments(caseDetails);
        assertEquals(1, result.size());

        DynamicList list = (DynamicList) result.get("documentRemovalCaseDocuments");
        assertEquals(1, list.getListItems().size());
        assertEquals("doc1", list.getListItems().getFirst().getCode());
        assertEquals("file1.pdf (03 Dec 2007 10:15)", list.getListItems().getFirst().getLabel());
    }

    @Test
    void testGetCaseDocumentsWithNoUploadTimestamp() {
        document = Document.builder()
            .documentUrl("http://someserver/doc1")
            .documentFileName("file1.pdf")
            .build();
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
        when(documentRetriever.getCaseDocuments(caseData)).thenReturn(List.of(document));

        Map<String, Object> result = documentRemovalService.getCaseDocuments(caseDetails);
        assertEquals(1, result.size());

        DynamicList list = (DynamicList) result.get("documentRemovalCaseDocuments");
        assertEquals(1, list.getListItems().size());
        assertEquals("doc1", list.getListItems().getFirst().getCode());
        assertEquals("file1.pdf", list.getListItems().getFirst().getLabel());
    }

    @Test
    void testGetCaseDocumentSelectedForRemoval() {
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
        when(documentRetriever.getCaseDocuments(caseData)).thenReturn(List.of(document));

        Map<String, Object> result = documentRemovalService.getCaseDocumentSelectedForRemoval(caseDetails);

        assertTrue(result.containsKey("documentRemovalCaseDocuments"));
        Document doc = (Document) result.get("documentRemovalDocumentToRemove");
        assertEquals("doc1", doc.getDocumentId());
    }

    @Test
    void testRemoveDocumentFromCaseData() throws IOException {
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
        when(documentRemover.removeDocument(anyMap(), eq("doc1"))).thenReturn(new HashMap<>(Map.of("someKey", "someValue")));

        Map<String, Object> result = documentRemovalService.removeDocumentFromCaseData(caseDetails);

        assertFalse(result.containsKey("documentToRemove"));
        assertFalse(result.containsKey("documentRemovalConfirmOptions"));
        assertEquals("someValue", result.get("someKey"));

        verify(documentRemovalAboutToSubmitAction).onAboutToSubmit(any(CaseData.class), anyMap());
    }

    @Test
    void testDeleteDocument() throws IOException {
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
        when(systemUserService.getSysUserToken()).thenReturn("token");

        documentRemovalService.deleteDocument(caseDetails);

        verify(deleteDocumentService).deleteDocument("token", "doc1");
    }

    @Test
    void testExecuteSubmittedActions() {
        CallbackRequest callbackRequest = mock(CallbackRequest.class);
        documentRemovalService.executePostSubmittedActions(callbackRequest);

        verify(documentRemovalSubmittedAction).onSubmitted(any(CallbackRequest.class));
    }
}
