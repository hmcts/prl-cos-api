package uk.gov.hmcts.reform.prl.services.documentremoval;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.ScannedDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentRemovalWrapper;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.services.DeleteDocumentService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.documentremoval.postabouttosubmitaction.DocumentRemovalAboutToSubmitAction;
import uk.gov.hmcts.reform.prl.services.documentremoval.submittedaction.DocumentRemovalSubmittedAction;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.services.documentremoval.DocumentRemovalService.CAFCASS_QUARANTINE_DOC_LIST;
import static uk.gov.hmcts.reform.prl.services.documentremoval.DocumentRemovalService.CITIZEN_QUARANTINE_DOC_LIST;
import static uk.gov.hmcts.reform.prl.services.documentremoval.DocumentRemovalService.COURT_NAV_QUARANTINE_DOCUMENT_LIST;
import static uk.gov.hmcts.reform.prl.services.documentremoval.DocumentRemovalService.COURT_STAFF_QUARANTINE_DOC_LIST;
import static uk.gov.hmcts.reform.prl.services.documentremoval.DocumentRemovalService.LEGAL_PROF_QUARANTINE_DOC_LIST;
import static uk.gov.hmcts.reform.prl.services.documentremoval.DocumentRemovalService.LOCAL_AUTHORITY_QUARANTINE_DOC_LIST;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

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

    private CaseDetails caseDetails;

    private Document document;
    private CaseData caseData;
    private Map<String, Object> courtStaffUploadDoc;

    @BeforeEach
    void setUp() {
        documentRemovalService = new DocumentRemovalService(objectMapper, documentRetriever, documentRemover,
                                                            deleteDocumentService, systemUserService,
                                                            List.of(documentRemovalAboutToSubmitAction),
                                                            List.of(documentRemovalSubmittedAction));

        caseDetails = CaseDetails.builder()
            .id(1234123412341234L)
            .data(new HashMap<>())
            .build();

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

        courtStaffUploadDoc = Map.of(
            "id", "1",
            "value", Map.of(
                    "categoryId", "respondentStatements",
                    "respondentStatementsDocument", Map.of(
                        "document_url", "http://someserver/doc1",
                        "document_filename", "file1.pdf"
                    )
            )
        );
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
        ReviewDocuments reviewDocuments = ReviewDocuments.builder()
            .courtStaffUploadDocListDocTab(
                List.of(element(QuarantineLegalDoc.builder()
                                    .respondentStatementsDocument(document)
                                    .categoryId("respondentStatements")
                                    .build()
                )))
            .build();
        caseData.setReviewDocuments(reviewDocuments);

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
        when(objectMapper.convertValue(any(), eq(Document.class))).thenReturn(document);
        when(objectMapper.convertValue(
            any(QuarantineLegalDoc.class),
            ArgumentMatchers.<TypeReference<Map<String, Object>>>any()
        )).thenReturn(courtStaffUploadDoc);
        when(documentRemover.removeDocument(anyMap(), eq("doc1"))).thenReturn(new HashMap<>(Map.of("someKey", "someValue")));

        Map<String, Object> result = documentRemovalService.removeDocumentFromCaseData(caseDetails);

        assertFalse(result.containsKey("documentToRemove"));
        assertFalse(result.containsKey("documentRemovalConfirmOptions"));
        assertEquals("someValue", result.get("someKey"));

        verify(documentRemovalAboutToSubmitAction).onAboutToSubmit(any(CaseData.class), anyMap());
    }

    @Test
    void testRemoveDocumentFromCaseDataAlsoRemovesDocumentFromCollectionWhereKeyIsNotDocument() throws IOException {
        ReviewDocuments reviewDocuments = ReviewDocuments.builder()
            .courtStaffUploadDocListDocTab(
                List.of(element(QuarantineLegalDoc.builder()
                                    .respondentStatementsDocument(document)
                                    .categoryId("respondentStatements")
                                    .build()
                )))
            .build();
        caseData.setReviewDocuments(reviewDocuments);

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
        when(objectMapper.convertValue(any(), eq(Document.class))).thenReturn(document);
        when(objectMapper.convertValue(
            any(QuarantineLegalDoc.class),
            ArgumentMatchers.<TypeReference<Map<String, Object>>>any()
        )).thenReturn(courtStaffUploadDoc);
        ArgumentCaptor<Map> caseDataMapCaptor = ArgumentCaptor.forClass(Map.class);
        when(documentRemover.removeDocument(caseDataMapCaptor.capture(), eq("doc1")))
            .thenReturn(new HashMap<>(Map.of("someKey", "someValue")));

        Map<String, Object> result = documentRemovalService.removeDocumentFromCaseData(caseDetails);

        assertFalse(result.containsKey("documentToRemove"));
        assertFalse(result.containsKey("documentRemovalConfirmOptions"));
        assertEquals("someValue", result.get("someKey"));

        verify(documentRemovalAboutToSubmitAction).onAboutToSubmit(any(CaseData.class), anyMap());

        // Verify document removed from the collection
        Map<String, Object> caseDataMap = caseDataMapCaptor.getValue();
        assertEquals(Collections.emptyList(), caseDataMap.get("courtStaffUploadDocListDocTab"));
    }

    @Test
    void testRemoveDocumentFromCaseDataAlsoRemovesDocumentFromCollectionWhereKeyIsDocument() throws IOException {
        ReviewDocuments reviewDocuments = ReviewDocuments.builder()
            .courtStaffUploadDocListDocTab(
                List.of(element(QuarantineLegalDoc.builder()
                                    .respondentStatementsDocument(document)
                                    .categoryId("respondentStatements")
                                    .build()
                )))
            .build();
        caseData.setReviewDocuments(reviewDocuments);

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        Document mockedDocument = mock(Document.class);
        Map<String, Object> uploadedDocument = Map.of("document", mockedDocument);
        when(objectMapper.convertValue(any(QuarantineLegalDoc.class),
                                       ArgumentMatchers.<TypeReference<Map<String, Object>>>any()))
            .thenReturn(uploadedDocument);

        when(objectMapper.convertValue(null, Document.class)).thenReturn(null);
        when(objectMapper.convertValue(mockedDocument, Document.class)).thenReturn(document);

        ArgumentCaptor<Map> caseDataMapCaptor = ArgumentCaptor.forClass(Map.class);
        when(documentRemover.removeDocument(caseDataMapCaptor.capture(), eq("doc1")))
            .thenReturn(new HashMap<>(Map.of("someKey", "someValue")));

        Map<String, Object> result = documentRemovalService.removeDocumentFromCaseData(caseDetails);

        assertFalse(result.containsKey("documentToRemove"));
        assertFalse(result.containsKey("documentRemovalConfirmOptions"));
        assertEquals("someValue", result.get("someKey"));

        verify(documentRemovalAboutToSubmitAction).onAboutToSubmit(any(CaseData.class), anyMap());

        // Verify document removed from the collection
        Map<String, Object> caseDataMap = caseDataMapCaptor.getValue();
        assertEquals(Collections.emptyList(), caseDataMap.get("courtStaffUploadDocListDocTab"));
    }

    @ParameterizedTest
    @MethodSource("testRemoveQuarantineDocument")
    void testRemoveQuarantineDocument(String field, DocumentManagementDetails documentManagementDetails) throws IOException {
        caseData.setDocumentManagementDetails(documentManagementDetails);

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        ArgumentCaptor<Map> caseDataMapCaptor = ArgumentCaptor.forClass(Map.class);
        when(documentRemover.removeDocument(caseDataMapCaptor.capture(), eq("doc1")))
            .thenReturn(new HashMap<>(Map.of("someKey", "someValue")));

        Map<String, Object> result = documentRemovalService.removeDocumentFromCaseData(caseDetails);

        assertFalse(result.containsKey("documentToRemove"));
        assertFalse(result.containsKey("documentRemovalConfirmOptions"));
        assertEquals("someValue", result.get("someKey"));

        verify(documentRemovalAboutToSubmitAction).onAboutToSubmit(any(CaseData.class), anyMap());

        // Verify document removed from the collection
        Map<String, Object> caseDataMap = caseDataMapCaptor.getValue();
        assertEquals(Collections.emptyList(), caseDataMap.get(field));
    }

    private static Stream<Arguments> testRemoveQuarantineDocument() {
        Document document = Document.builder()
            .documentUrl("http://someserver/doc1")
            .documentFileName("file1.pdf")
            .uploadTimeStamp(LocalDateTime.parse("2007-12-03T10:15:30"))
            .build();

        DocumentManagementDetails solicitor = DocumentManagementDetails.builder()
            .legalProfQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder()
                                                             .document(document)
                                                             .build()))
            ).build();

        DocumentManagementDetails courtStaff = DocumentManagementDetails.builder()
            .courtStaffQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder()
                                                              .courtStaffQuarantineDocument(document)
                                                              .build()))
            ).build();

        DocumentManagementDetails cafcass = DocumentManagementDetails.builder()
            .cafcassQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder()
                                                           .cafcassQuarantineDocument(document)
                                                           .build()))
            ).build();

        DocumentManagementDetails citizen = DocumentManagementDetails.builder()
            .citizenQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder()
                                                           .citizenQuarantineDocument(document)
                                                           .build()))
            ).build();

        DocumentManagementDetails courtNav = DocumentManagementDetails.builder()
            .courtNavQuarantineDocumentList(List.of(element(QuarantineLegalDoc.builder()
                                                                .courtNavQuarantineDocument(document)
                                                                .build()))
            ).build();

        DocumentManagementDetails localAuthority = DocumentManagementDetails.builder()
            .localAuthorityQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder()
                                                                  .localAuthorityQuarantineDocument(document)
                                                                  .build()))
            ).build();

        return Stream.of(
            Arguments.of(
                LEGAL_PROF_QUARANTINE_DOC_LIST,
                solicitor
            ),
            Arguments.of(
                COURT_STAFF_QUARANTINE_DOC_LIST,
                courtStaff
            ),
            Arguments.of(
                CAFCASS_QUARANTINE_DOC_LIST,
                cafcass
            ),
            Arguments.of(
                CITIZEN_QUARANTINE_DOC_LIST,
                citizen
            ),
            Arguments.of(
                COURT_NAV_QUARANTINE_DOCUMENT_LIST,
                courtNav
            ),
            Arguments.of(
                LOCAL_AUTHORITY_QUARANTINE_DOC_LIST,
                localAuthority
            )
        );
    }

    @Test
    void testRemoveScannedDocuments() throws IOException {
        Element<ScannedDocument> scannedDocumentToRemove = element(ScannedDocument.builder()
                                                                      .url(Document.builder()
                                                                               .documentUrl("http://someserver/doc1")
                                                                               .documentFileName("file1.pdf")
                                                                               .build())
                                                                      .build());
        Element<ScannedDocument> scannedDocumentToKeep = element(ScannedDocument.builder()
                                                                      .url(Document.builder()
                                                                               .documentUrl("http://someserver/doc2")
                                                                               .documentFileName("file2.pdf")
                                                                               .build())
                                                                      .build());

        caseData.setScannedDocuments(List.of(scannedDocumentToKeep, scannedDocumentToRemove));

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        when(documentRemover.removeDocument(mapCaptor.capture(), eq("doc1"))).thenReturn(new HashMap<>(Map.of("someKey", "someValue")));

        Map<String, Object> result = documentRemovalService.removeDocumentFromCaseData(caseDetails);

        assertFalse(result.containsKey("documentToRemove"));
        assertFalse(result.containsKey("documentRemovalConfirmOptions"));
        assertEquals("someValue", result.get("someKey"));

        verify(documentRemovalAboutToSubmitAction).onAboutToSubmit(any(CaseData.class), anyMap());

        Map<String, Object> mapCaptorValue = mapCaptor.getValue();
        List<Element<ScannedDocument>> updatedScannedDocuments = (List<Element<ScannedDocument>>) mapCaptorValue.get("scannedDocuments");
        assertEquals(1, updatedScannedDocuments.size());
        assertEquals(scannedDocumentToKeep, updatedScannedDocuments.getFirst());
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
