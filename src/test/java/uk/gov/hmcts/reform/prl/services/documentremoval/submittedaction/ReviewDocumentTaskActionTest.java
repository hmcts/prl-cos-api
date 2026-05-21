package uk.gov.hmcts.reform.prl.services.documentremoval.submittedaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.reviewdocument.ReviewDocumentService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewDocumentTaskActionTest {

    @Mock
    private ReviewDocumentService reviewDocumentService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private CallbackRequest callbackRequest;
    @Mock
    private CaseDetails caseDetails;
    @Mock
    private CaseDetails caseDetailsBefore;
    @Mock
    private CaseData caseData;
    @Mock
    private CaseData caseDataBefore;

    @InjectMocks
    private ReviewDocumentTaskAction reviewDocumentTaskAction;

    @Test
    void shouldTriggerEventWhenDocsReviewed() {
        try (MockedStatic<CaseUtils> mockedCaseUtils = org.mockito.Mockito.mockStatic(CaseUtils.class)) {
            mockCaseData(mockedCaseUtils);
            when(reviewDocumentService.hasDocumentsToBeReviewed(caseDataBefore)).thenReturn(true);
            when(reviewDocumentService.hasDocumentsToBeReviewed(caseData)).thenReturn(false);

            reviewDocumentTaskAction.onSubmitted(callbackRequest);

            verify(reviewDocumentService).triggerAllDocsReviewedEvent(caseData);
        }
    }

    @Test
    void shouldNotTriggerEventWhenDocsStillToBeReviewed() {
        try (MockedStatic<CaseUtils> mockedCaseUtils = org.mockito.Mockito.mockStatic(CaseUtils.class)) {
            mockCaseData(mockedCaseUtils);
            when(reviewDocumentService.hasDocumentsToBeReviewed(caseDataBefore)).thenReturn(true);
            when(reviewDocumentService.hasDocumentsToBeReviewed(caseData)).thenReturn(true);

            reviewDocumentTaskAction.onSubmitted(callbackRequest);

            verify(reviewDocumentService, never()).triggerAllDocsReviewedEvent(any());
        }
    }

    @Test
    void shouldNotTriggerEventWhenNoDocsToBeReviewedBefore() {
        try (MockedStatic<CaseUtils> mockedCaseUtils = org.mockito.Mockito.mockStatic(CaseUtils.class)) {
            mockCaseData(mockedCaseUtils);
            when(reviewDocumentService.hasDocumentsToBeReviewed(caseDataBefore)).thenReturn(false);

            reviewDocumentTaskAction.onSubmitted(callbackRequest);

            verify(reviewDocumentService, never()).triggerAllDocsReviewedEvent(any());
        }
    }

    private void mockCaseData(MockedStatic<CaseUtils> mockedCaseUtils) {
        mockedCaseUtils.when(() -> CaseUtils.getCaseData(caseDetails, objectMapper)).thenReturn(caseData);
        mockedCaseUtils.when(() -> CaseUtils.getCaseData(caseDetailsBefore, objectMapper)).thenReturn(caseDataBefore);

        when(callbackRequest.getCaseDetails()).thenReturn(caseDetails);
        when(callbackRequest.getCaseDetailsBefore()).thenReturn(caseDetailsBefore);
    }
}
