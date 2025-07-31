package uk.gov.hmcts.reform.prl.controllers.managedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.RemovableDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.managedocuments.RemoveDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class RemoveDocumentsControllerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RemoveDocumentsService removeDocumentsService;

    @Mock
    private UserService userService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private AllTabServiceImpl tabService;

    @InjectMocks
    private RemoveDocumentsController removeDocumentsController;

    private final String auth = "authorisation";
    private final String serviceAuthToken = "serviceAuthToken";

    private static final Document TEST_DOCUMENT = Document.builder()
        .documentFileName("test.pdf")
        .documentBinaryUrl("http://example.com/test.pdf")
        .documentUrl("http://example.com/test.pdf")
        .build();

    @BeforeEach
    void setUp() {
        when(authorisationService.isAuthorized(auth, serviceAuthToken)).thenReturn(true);
    }

    @Test
    void testHandleAboutToStart() {
        UUID elementId = UUID.randomUUID();
        List<Element<RemovableDocument>> removalList = List.of(
            element(
                elementId, RemovableDocument.builder()
                    .document(TEST_DOCUMENT)
                    .build()
            )
        );

        CaseData caseData = CaseData.builder()
            .reviewDocuments(
                ReviewDocuments.builder()
                    .bulkScannedDocListDocTab(List.of(
                        element(
                            elementId,
                            QuarantineLegalDoc.builder()
                                .categoryId("caseSummary")
                                .caseSummaryDocument(TEST_DOCUMENT)
                                .build()
                        )))
                    .build())
            .build();

        CaseData caseDataUpdated = caseData.toBuilder()
            .removableDocuments(removalList)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        when(removeDocumentsService.populateRemovalList(caseData)).thenReturn(caseDataUpdated);

        CallbackRequest cb = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(123L).data(stringObjectMap).build())
            .build();

        CallbackResponse response = removeDocumentsController.handleAboutToStart(auth, serviceAuthToken, cb);

        assertThat(response.getData().getRemovableDocuments()).isEqualTo(removalList);
        verify(removeDocumentsService).populateRemovalList(caseData);
        verifyNoMoreInteractions(removeDocumentsService);
    }

    @Test
    void testConfirmRemovals() {
        String expectedText = "No documents to be removed.";

        CaseData old = CaseData.builder().build();
        CaseData caseData = CaseData.builder().build();
        CaseData caseDataUpdated = CaseData.builder()
            .documentsToBeRemoved(expectedText)
            .build();

        when(removeDocumentsService.getConfirmationTextForDocsBeingRemoved(caseData, old))
            .thenReturn(expectedText);

        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        Map<String, Object> oldCaseDataMap = old.toMap(new ObjectMapper());
        when(objectMapper.convertValue(oldCaseDataMap, CaseData.class)).thenReturn(old);

        CallbackRequest cb = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .caseDetailsBefore(CaseDetails.builder()
                                   .id(123L)
                                   .data(oldCaseDataMap)
                                   .build())
            .build();

        CallbackResponse response = removeDocumentsController.confirmRemovals(auth, serviceAuthToken, cb);

        verify(removeDocumentsService).getConfirmationTextForDocsBeingRemoved(caseData, old);
        assertThat(response.getData()).isEqualTo(caseDataUpdated);
    }


    @Test
    void testAboutToSubmit() {
        UUID elementId = UUID.randomUUID();
        CaseData old = CaseData.builder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .courtStaffUploadDocListDocTab(List.of(
                                     element(
                                         elementId, QuarantineLegalDoc.builder()
                                             .categoryId("caseSummary")
                                             .caseSummaryDocument(TEST_DOCUMENT)
                                             .build()
                                     )))
                                 .build())
            .build();
        CaseData caseData = CaseData.builder()
            .removableDocuments(List.of(
                element(
                    elementId, RemovableDocument.builder()
                        .document(TEST_DOCUMENT)
                        .build()
                )))
            .build();

        List<Element<RemovableDocument>> removalList = List.of(
            element(
                elementId, RemovableDocument.builder()
                    .document(TEST_DOCUMENT)
                    .build()
            )
        );
        when(removeDocumentsService.getDocsBeingRemoved(caseData, old)).thenReturn(removalList);

        Map<String, Object> after = Map.of("courtStaffUploadDocListDocTab", ReviewDocuments.builder().build());

        when(removeDocumentsService.removeDocuments(caseData, removalList)).thenReturn(after);

        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        Map<String, Object> oldCaseDataMap = old.toMap(new ObjectMapper());
        when(objectMapper.convertValue(oldCaseDataMap, CaseData.class)).thenReturn(old);

        CallbackRequest cb = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .caseDetailsBefore(CaseDetails.builder()
                                   .id(123L)
                                   .data(oldCaseDataMap)
                                   .build())
            .build();

        removeDocumentsController.aboutToSubmit(auth, serviceAuthToken, cb);

        verify(removeDocumentsService).getDocsBeingRemoved(caseData, old);
        verify(removeDocumentsService).removeDocuments(caseData, removalList);
    }

    @Test
    void testSubmitted() {
        UUID elementId = UUID.randomUUID();
        CaseData old = CaseData.builder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .courtStaffUploadDocListDocTab(List.of(
                                     element(
                                         elementId, QuarantineLegalDoc.builder()
                                             .categoryId("caseSummary")
                                             .caseSummaryDocument(TEST_DOCUMENT)
                                             .build()
                                     )))
                                 .build())
            .build();
        CaseData caseData = CaseData.builder()
            .removableDocuments(List.of(
                element(
                    elementId, RemovableDocument.builder()
                        .document(TEST_DOCUMENT)
                        .build()
                )))
            .build();

        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        Map<String, Object> oldCaseDataMap = old.toMap(new ObjectMapper());
        when(objectMapper.convertValue(oldCaseDataMap, CaseData.class)).thenReturn(old);

        CallbackRequest cb = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(123L).data(caseDataMap).build())
            .caseDetailsBefore(CaseDetails.builder().id(123L).data(oldCaseDataMap).build())
            .build();

        removeDocumentsController.submitted(auth, serviceAuthToken, cb);

        verify(removeDocumentsService).deleteDocumentsInCdam(caseData, old);
    }

    @Test
    void testSubmitted_fullSubmitFlow() {
        Map<String, Object> originalMap = new HashMap<>();
        originalMap.put("foo", "bar");

        CaseData newData = CaseData.builder().build();
        CaseData oldData = CaseData.builder().build();
        when(objectMapper.convertValue(originalMap, CaseData.class)).thenReturn(newData);
        when(objectMapper.convertValue(oldData.toMap(new ObjectMapper()), CaseData.class))
            .thenReturn(oldData);

        EventRequestData requestData = EventRequestData.builder().build();
        StartEventResponse startEventResponse = StartEventResponse.builder().build();
        StartAllTabsUpdateDataContent startData = new StartAllTabsUpdateDataContent(
            auth,
            requestData,
            startEventResponse,
            new HashMap<>(originalMap),
            newData,
            null
        );
        when(tabService.getStartAllTabsUpdate("3")).thenReturn(startData);

        when(removeDocumentsService.getDocsBeingRemoved(newData, oldData))
            .thenReturn(Collections.emptyList());
        Map<String, Object> delta = Map.of("baz", "qux");
        when(removeDocumentsService.removeDocuments(newData, Collections.emptyList()))
            .thenReturn(delta);

        CallbackRequest cb = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(3L).data(originalMap).build())
            .caseDetailsBefore(CaseDetails.builder()
                                   .id(3L)
                                   .data(oldData.toMap(new ObjectMapper()))
                                   .build())
            .build();

        CallbackResponse resp = removeDocumentsController.submitted(auth, serviceAuthToken, cb);

        assertThat(resp.getData()).isNull();
        verify(removeDocumentsService).deleteDocumentsInCdam(newData, oldData);
        verify(tabService).getStartAllTabsUpdate("3");
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(tabService).submitAllTabsUpdate(
            eq(auth),
            eq("3"),
            eq(startEventResponse),
            eq(requestData),
            captor.capture()
        );

        Map<String, Object> submittedMap = captor.getValue();
        assertThat(submittedMap.get("foo")).isEqualTo("bar");
        assertThat(submittedMap.get("baz")).isEqualTo("qux");
    }

    @Test
    void testOriginalFieldsArePreservedWhenRemovingDocs() {
        Map<String, Object> incoming = new HashMap<>();
        incoming.put("someExistingField", "keep me");

        CaseData newData = CaseData.builder().build();
        CaseData oldData = CaseData.builder().build();
        when(objectMapper.convertValue(incoming, CaseData.class)).thenReturn(newData);
        when(objectMapper.convertValue(oldData.toMap(new ObjectMapper()), CaseData.class)).thenReturn(oldData);

        EventRequestData requestData = EventRequestData.builder().build();
        StartEventResponse startEventResponse = StartEventResponse.builder().build();
        StartAllTabsUpdateDataContent startData = new StartAllTabsUpdateDataContent(
            auth,
            requestData,
            startEventResponse,
            new HashMap<>(incoming),
            newData,
            null
        );
        when(tabService.getStartAllTabsUpdate("42")).thenReturn(startData);

        when(removeDocumentsService.getDocsBeingRemoved(newData, oldData))
            .thenReturn(Collections.emptyList());
        when(removeDocumentsService.removeDocuments(newData, Collections.emptyList()))
            .thenReturn(Map.of("removedCount", 0));

        CallbackRequest cb = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(42L).data(incoming).build())
            .caseDetailsBefore(CaseDetails.builder()
                                   .id(42L)
                                   .data(oldData.toMap(new ObjectMapper()))
                                   .build())
            .build();

        removeDocumentsController.submitted(auth, serviceAuthToken, cb);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String,Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(tabService).submitAllTabsUpdate(
            eq(auth),
            eq("42"),
            eq(startEventResponse),
            eq(requestData),
            captor.capture()
        );

        Map<String,Object> finalPayload = captor.getValue();
        // verify original field still there
        assertEquals("keep me", finalPayload.get("someExistingField"));
        // verify delta was applied
        assertEquals(0, finalPayload.get("removedCount"));
    }

}
