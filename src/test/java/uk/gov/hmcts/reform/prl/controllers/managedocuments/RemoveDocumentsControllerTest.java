package uk.gov.hmcts.reform.prl.controllers.managedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RemoveDocumentsControllerTest {

    @Mock
    private RemoveDocumentsService removeDocumentsService;

    @Mock
    private UserService userService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private RemoveDocumentsController removeDocumentsController;

    private final String auth = "authorisation";
    private final String serviceAuthToken = "serviceAuthToken";

    private static final Document TEST_DOCUMENT = Document.builder()
        .documentFileName("test.pdf")
        .documentBinaryUrl("http://example.com/test.pdf")
        .documentUrl("http://example.com/test.pdf")
        .build();

    @Before
    public void setUp() {
        when(authorisationService.isAuthorized(auth, serviceAuthToken)).thenReturn(true);
    }

    @Test
    public void testHandleAboutToStart() {
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

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .build();


        when(removeDocumentsService.populateRemovalList(caseData)).thenReturn(caseDataUpdated);

        CallbackRequest cb = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        CallbackResponse response = removeDocumentsController.handleAboutToStart(auth, serviceAuthToken, cb);

        assertNotNull(response.getData().getRemovableDocuments());

        verify(removeDocumentsService).populateRemovalList(caseData);
        verifyNoMoreInteractions(removeDocumentsService);
    }

    @Test
    public void testConfirmRemovals() {
        String expectedText = "No documents to be removed.";

        CaseData old = CaseData.builder().build();
        CaseData caseData = CaseData.builder().build();
        CaseData caseDataUpdated = CaseData.builder()
            .documentsToBeRemoved(expectedText)
            .build();

        when(removeDocumentsService.getConfirmationTextForDocsBeingRemoved(caseData, old)).thenReturn(expectedText);

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
    public void testAboutToSubmit() {
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

        CaseData after = CaseData.builder()
            .reviewDocuments(ReviewDocuments.builder().build())
            .build();

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
    public void testSubmitted() {
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
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(caseDataMap)
                             .build())
            .caseDetailsBefore(CaseDetails.builder()
                                   .id(123L)
                                   .data(oldCaseDataMap)
                                   .build())
            .build();

        removeDocumentsController.submitted(auth, serviceAuthToken, cb);

        verify(removeDocumentsService).deleteDocumentsInCdam(caseData, old);
    }
}
