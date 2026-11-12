package uk.gov.hmcts.reform.prl.services.reviewdocument;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.services.DocumentCategoryService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BULK_SCAN;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

class ReviewDocumentServiceCleanupTest {

    private static final String ORIGINAL_DOCUMENT_ID = "00000000-0000-0000-0000-000000000000";
    private static final String REPLACEMENT_DOCUMENT_ID = "11111111-1111-1111-1111-111111111111";

    private final ManageDocumentsService manageDocumentsService = mock(ManageDocumentsService.class);
    private final ReviewDocumentService reviewDocumentService = new ReviewDocumentService(
        mock(AllTabServiceImpl.class),
        manageDocumentsService,
        new ObjectMapper().registerModule(new JavaTimeModule()),
        mock(DocumentCategoryService.class)
    );

    @Test
    void shouldRetainOriginalsWhenConfidentialAndRestrictedReplacementDocumentsAreMissing() {
        QuarantineLegalDoc missingReplacement = reviewedDocument(null, ORIGINAL_DOCUMENT_ID);

        cleanupDocuments(List.of(element(missingReplacement)), List.of(element(missingReplacement)));

        verifyNoInteractions(manageDocumentsService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void shouldRetainOriginalWhenReplacementDocumentUrlIsMissing(String documentUrl) {
        Document replacement = Document.builder()
            .documentFileName("reviewed.pdf")
            .documentUrl(documentUrl)
            .build();

        cleanupDocuments(List.of(element(reviewedDocument(replacement, ORIGINAL_DOCUMENT_ID))), List.of());

        verifyNoInteractions(manageDocumentsService);
    }

    @Test
    void shouldContinueCleaningDocumentsWhenAnEarlierReplacementIsMissing() {
        QuarantineLegalDoc missingReplacement = reviewedDocument(null, REPLACEMENT_DOCUMENT_ID);
        Document replacement = Document.builder()
            .documentFileName("reviewed.pdf")
            .documentUrl("http://doc-store/documents/" + REPLACEMENT_DOCUMENT_ID)
            .build();

        cleanupDocuments(
            List.of(element(missingReplacement), element(reviewedDocument(replacement, ORIGINAL_DOCUMENT_ID))),
            List.of()
        );

        verify(manageDocumentsService).deleteDocumentById(ORIGINAL_DOCUMENT_ID);
        verifyNoMoreInteractions(manageDocumentsService);
    }

    @Test
    void shouldDeleteOriginalOfReviewedBulkScanDocumentStoredInUrl() {
        Document replacement = Document.builder()
            .documentFileName("Confidential_reviewed.pdf")
            .documentUrl("http://doc-store/documents/" + REPLACEMENT_DOCUMENT_ID)
            .build();
        QuarantineLegalDoc reviewedBulkScanDocument = QuarantineLegalDoc.builder()
            .categoryId("applicantApplication")
            .uploaderRole(BULK_SCAN)
            .url(replacement)
            .originalDocumentId(ORIGINAL_DOCUMENT_ID)
            .build();

        cleanupDocuments(List.of(element(reviewedBulkScanDocument)), List.of());

        verify(manageDocumentsService).deleteDocumentById(ORIGINAL_DOCUMENT_ID);
        verifyNoMoreInteractions(manageDocumentsService);
    }

    private QuarantineLegalDoc reviewedDocument(Document replacement, String originalDocumentId) {
        return QuarantineLegalDoc.builder()
            .categoryId("applicantApplication")
            .applicantApplicationDocument(replacement)
            .originalDocumentId(originalDocumentId)
            .build();
    }

    private void cleanupDocuments(List<Element<QuarantineLegalDoc>> confidentialDocuments,
                                  List<Element<QuarantineLegalDoc>> restrictedDocuments) {
        CaseData currentCaseData = CaseData.builder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .confidentialDocuments(confidentialDocuments)
                                 .restrictedDocuments(restrictedDocuments)
                                 .build())
            .build();

        reviewDocumentService.cleanupOldCopyOfDocuments(currentCaseData, CaseData.builder().build());
    }
}
