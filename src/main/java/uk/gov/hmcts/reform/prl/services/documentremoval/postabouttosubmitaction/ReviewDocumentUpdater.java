package uk.gov.hmcts.reform.prl.services.documentremoval.postabouttosubmitaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.reviewdocument.ReviewDocumentService;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewDocumentUpdater implements DocumentRemovalAboutToSubmitAction {

    private final ReviewDocumentService reviewDocumentService;

    @Override
    public void onAboutToSubmit(CaseData caseData, Map<String, Object> caseDataUpdated) {
        reviewDocumentService.removeReviewDocumentWithMissingDocument(caseData, caseDataUpdated);
    }
}
