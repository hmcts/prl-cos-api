package uk.gov.hmcts.reform.prl.services.managedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.RemovableDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RemoveDocumentsServiceTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    RemoveDocumentsService removeDocumentsService;

    private static final UUID TEST_DOCUMENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private static final Document TEST_DOCUMENT = Document.builder()
        .documentUrl(TEST_DOCUMENT_ID.toString())
        .documentBinaryUrl(TEST_DOCUMENT_ID + "/binary")
        .documentFileName("document.pdf")
        .documentHash("hash123")
        .build();

    @Before
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        when(systemUserService.getSysUserToken()).thenReturn("systemUserToken");
        when(authTokenGenerator.generate()).thenReturn("authToken");
    }

    @Test
    public void shouldPopulateRemovalListFromReviewDocuments() {
        UUID elementId = UUID.randomUUID();
        QuarantineLegalDoc doc = QuarantineLegalDoc.builder()
            .caseSummaryDocument(TEST_DOCUMENT)
            .categoryId("caseSummary")
            .build();

        CaseData caseData = CaseData.builder()
            .reviewDocuments(
                ReviewDocuments.builder()
                    .courtStaffUploadDocListDocTab(
                        List.of(new Element<>(elementId, doc)))
                    .build())
            .build();

        CaseData updatedCaseData = removeDocumentsService.populateRemovalList(caseData);

        assertThat(updatedCaseData.getRemovableDocuments()).isEqualTo(
            List.of(new Element<>(elementId, RemovableDocument.builder().document(TEST_DOCUMENT).build()))
        );
    }

    @Test
    public void shouldPopulateRemovalListFromDocMgmtDocuments() {
        UUID elementId = UUID.randomUUID();
        QuarantineLegalDoc doc = QuarantineLegalDoc.builder()
            .caseSummaryDocument(TEST_DOCUMENT)
            .categoryId("caseSummary")
            .build();

        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .courtStaffQuarantineDocsList(
                        List.of(new Element<>(elementId, doc)))
                    .build())
            .build();

        CaseData updatedCaseData = removeDocumentsService.populateRemovalList(caseData);

        assertThat(updatedCaseData.getRemovableDocuments()).isEqualTo(
            List.of(new Element<>(elementId, RemovableDocument.builder().document(TEST_DOCUMENT).build()))
        );
    }

    @Test
    public void shouldGetDocsBeingRemoved() {
        UUID documentId = UUID.randomUUID();

        CaseData caseData = CaseData.builder()
            .removableDocuments(List.of())
            .build();

        CaseData old = CaseData.builder()
            .reviewDocuments(
                ReviewDocuments.builder()
                    .courtStaffUploadDocListDocTab(
                        List.of(new Element<>(
                            documentId, QuarantineLegalDoc.builder()
                            .respondentStatementsDocument(TEST_DOCUMENT)
                            .categoryId("respondentStatements")
                            .build()
                        )))
                    .build())
            .build();

        List<Element<RemovableDocument>> docsBeingRemoved = removeDocumentsService.getDocsBeingRemoved(caseData, old);
        assertThat(docsBeingRemoved).isEqualTo(
            List.of(new Element<>(documentId, RemovableDocument.builder().document(TEST_DOCUMENT).build()))
        );
    }

    @Test
    public void shouldGetConfirmationTextForDocsBeingRemoved() {
        CaseData caseData = CaseData.builder()
            .removableDocuments(List.of())
            .build();

        CaseData old = CaseData.builder()
            .reviewDocuments(
                ReviewDocuments.builder()
                    .courtStaffUploadDocListDocTab(
                        List.of(new Element<>(
                            UUID.randomUUID(), QuarantineLegalDoc.builder()
                            .respondentStatementsDocument(TEST_DOCUMENT)
                            .categoryName("Respondent Statements")
                            .documentParty("Respondent")
                            .documentUploadedDate(LocalDateTime.of(2025, 1, 1, 12, 0))
                            .categoryId("respondentStatements")
                            .build()
                        )))
                    .build())
            .build();

        String message = removeDocumentsService.getConfirmationTextForDocsBeingRemoved(caseData, old);

        assertThat(message)
            .isEqualTo(" • Respondent Statements - document.pdf, Respondent (01 Jan 2025, 12:00:00 PM)");
    }

    @Test
    public void shouldGetConfirmationTextWhenNoDocsRemoved() {
        UUID documentId = UUID.randomUUID();

        CaseData caseData = CaseData.builder()
            .removableDocuments(
                List.of(new Element<>(documentId, RemovableDocument.builder().document(TEST_DOCUMENT).build()))
            )
            .build();

        CaseData old = CaseData.builder()
            .reviewDocuments(
                ReviewDocuments.builder()
                    .courtStaffUploadDocListDocTab(
                        List.of(new Element<>(
                            documentId, QuarantineLegalDoc.builder()
                            .respondentStatementsDocument(TEST_DOCUMENT)
                            .categoryId("respondentStatements")
                            .build()
                        )))
                    .build())
            .build();

        String message = removeDocumentsService.getConfirmationTextForDocsBeingRemoved(caseData, old);

        assertThat(message)
            .isEqualTo("No documents removed.");
    }


    @Test
    public void shouldRemoveDocumentsFromOriginalField() {
        UUID elementId = UUID.randomUUID();

        List<Element<RemovableDocument>> docsToRemove = List.of(
            new Element<>(elementId, RemovableDocument.builder().document(TEST_DOCUMENT).build())
        );

        CaseData caseData = CaseData.builder()
            .reviewDocuments(
                ReviewDocuments.builder()
                    .courtStaffUploadDocListDocTab(
                        List.of(new Element<>(
                            elementId, QuarantineLegalDoc.builder()
                            .respondentStatementsDocument(TEST_DOCUMENT)
                            .categoryId("respondentStatements")
                            .build()
                        )))
                    .build())
            .build();

        CaseData after = removeDocumentsService.removeDocuments(caseData, docsToRemove);

        assertThat(after.getReviewDocuments().getCourtStaffUploadDocListDocTab())
            .asInstanceOf(LIST).isEmpty();
    }

    @Test
    public void shouldConvertQuarantineDoc() {
    }

    @Test
    public void shouldDeleteDocumentsInCdam() {
        UUID documentId = UUID.randomUUID();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .reviewDocuments(ReviewDocuments.builder().build())
            .build();

        CaseData old = CaseData.builder()
            .id(12345L)
            .reviewDocuments(
                ReviewDocuments.builder()
                    .courtStaffUploadDocListDocTab(
                        List.of(new Element<>(
                            documentId, QuarantineLegalDoc.builder()
                            .privacyNoticeDocument(TEST_DOCUMENT)
                            .categoryId("privacyNotice")
                            .build()
                        )))
                    .build())
            .build();

        removeDocumentsService.deleteDocumentsInCdam(caseData, old);

        verify(caseDocumentClient).deleteDocument(
            anyString(),
            anyString(),
            eq(TEST_DOCUMENT_ID),
            eq(true)
        );

    }
}
