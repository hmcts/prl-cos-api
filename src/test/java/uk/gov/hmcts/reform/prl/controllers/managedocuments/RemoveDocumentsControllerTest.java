package uk.gov.hmcts.reform.prl.controllers.managedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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
            element(elementId,
                RemovableDocument.builder().document(TEST_DOCUMENT).build())
        );

        CaseData caseData = CaseData.builder()
            .reviewDocuments(ReviewDocuments.builder()
                .bulkScannedDocListDocTab(List.of(
                    element(elementId,
                        QuarantineLegalDoc.builder()
                            .categoryId("caseSummary")
                            .caseSummaryDocument(TEST_DOCUMENT)
                            .build()
                    )
                ))
                .build())
            .build();

        CaseData caseDataUpdated = caseData.toBuilder()
            .removableDocuments(removalList)
            .build();

        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(removeDocumentsService.populateRemovalList(caseData)).thenReturn(caseDataUpdated);

        CallbackRequest cb = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(123L).data(caseDataMap).build())
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
        CaseData caseDataUpdated = caseData.toBuilder()
            .documentsToBeRemoved(expectedText)
            .build();

        when(removeDocumentsService.getConfirmationTextForDocsBeingRemoved(caseData, old))
            .thenReturn(expectedText);

        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        Map<String, Object> oldCaseDataMap = old.toMap(new ObjectMapper());
        when(objectMapper.convertValue(oldCaseDataMap, CaseData.class)).thenReturn(old);

        CallbackRequest cb = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(123L).data(caseDataMap).build())
            .caseDetailsBefore(CaseDetails.builder().id(123L).data(oldCaseDataMap).build())
            .build();

        CallbackResponse response = removeDocumentsController.confirmRemovals(auth, serviceAuthToken, cb);

        verify(removeDocumentsService).getConfirmationTextForDocsBeingRemoved(caseData, old);
        assertThat(response.getData()).isEqualTo(caseDataUpdated);
    }

    @Test
    void testAboutToSubmit_RemovesDocsAndPreservesData() {
        Map<String, Object> originalData = new HashMap<>();
        originalData.put("existingField", "keepMe");

        CaseData caseData = CaseData.builder().build();
        CaseData oldData = CaseData.builder().build();

        when(objectMapper.convertValue(originalData, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(originalData, CaseData.class)).thenReturn(oldData);

        List<Element<RemovableDocument>> removalList = List.of();
        when(removeDocumentsService.getDocsBeingRemoved(caseData, oldData)).thenReturn(removalList);

        Map<String, Object> delta = Map.of("newKey", "newValue");
        when(removeDocumentsService.removeDocuments(caseData, removalList)).thenReturn(delta);

        CallbackRequest cb = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(1L).data(originalData).build())
            .caseDetailsBefore(CaseDetails.builder().id(1L).data(originalData).build())
            .build();

        AboutToStartOrSubmitCallbackResponse response =
            removeDocumentsController.aboutToSubmit(auth, serviceAuthToken, cb);

        Map<String, Object> merged = response.getData();
        assertThat(merged.get("existingField")).isEqualTo("keepMe");
        assertThat(merged.get("newKey")).isEqualTo("newValue");

        verify(removeDocumentsService).getDocsBeingRemoved(caseData, oldData);
        verify(removeDocumentsService).removeDocuments(caseData, removalList);
    }

    @Test
    void testSubmitted() {
        UUID elementId = UUID.randomUUID();
        CaseData old = CaseData.builder().build();
        CaseData caseData = CaseData.builder()
            .removableDocuments(List.of(
                element(elementId,
                    RemovableDocument.builder().document(TEST_DOCUMENT).build()
                )
            ))
            .build();

        Map<String, Object> caseDataMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        Map<String, Object> oldCaseDataMap = old.toMap(new ObjectMapper());
        when(objectMapper.convertValue(oldCaseDataMap, CaseData.class)).thenReturn(old);

        CallbackRequest cb = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().id(123L).data(caseDataMap).build())
            .caseDetailsBefore(CaseDetails.builder().id(123L).data(oldCaseDataMap).build())
            .build();

        CallbackResponse response = removeDocumentsController.submitted(auth, serviceAuthToken, cb);

        verify(removeDocumentsService).deleteDocumentsInCdam(caseData, old);
        assertThat(response.getData()).isNull();
    }
}
