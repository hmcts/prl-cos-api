package uk.gov.hmcts.reform.prl.services.documentremoval.postabouttosubmitaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.reviewdocument.ReviewDocumentService;

import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReviewDocumentUpdaterTest {
    @Mock
    private ReviewDocumentService reviewDocumentService;
    @InjectMocks
    private ReviewDocumentUpdater reviewDocumentUpdater;

    @Test
    void shouldUpdateReviewDocument() {
        CaseData caseData = CaseData.builder().build();
        Map<String, Object> caseDataUpdated = Map.of();
        reviewDocumentUpdater.onAboutToSubmit(caseData, caseDataUpdated);
        verify(reviewDocumentService).removeReviewDocumentWithMissingDocument(caseData, caseDataUpdated);
    }
}
