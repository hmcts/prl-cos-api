package uk.gov.hmcts.reform.prl.services.documentremoval.submittedaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.reviewdocument.ReviewDocumentService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

@Service
@RequiredArgsConstructor
public class ReviewDocumentTaskAction implements DocumentRemovalSubmittedAction {
    private final ReviewDocumentService reviewDocumentService;
    private final ObjectMapper objectMapper;

    @Override
    public void onSubmitted(CallbackRequest callbackRequest) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        CaseData caseDataBefore = CaseUtils.getCaseData(callbackRequest.getCaseDetailsBefore(), objectMapper);

        if (reviewDocumentService.hasDocumentsToBeReviewed(caseDataBefore)
            && !reviewDocumentService.hasDocumentsToBeReviewed(caseData)) {
            reviewDocumentService.triggerAllDocsReviewedEvent(caseData);
        }
    }
}
