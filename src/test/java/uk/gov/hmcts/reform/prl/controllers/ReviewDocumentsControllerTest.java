package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.services.reviewdocument.ReviewDocumentService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class ReviewDocumentsControllerTest {

    @InjectMocks
    private ReviewDocumentsController reviewDocumentsController;

    @Mock
    private ReviewDocumentService reviewDocumentService;

    @Mock
    private ObjectMapper objectMapper;

    String auth = "authorisation";

    DynamicListElement dynamicListElement;

    List<DynamicListElement> dynamicListElements;

    CaseData caseData;

    Map<String, Object> stringObjectMap;

    CaseDetails caseDetails;

    CallbackRequest callbackRequest;

    UUID uuid;

    @Before
    public void setUp() {
        uuid = UUID.randomUUID();

        dynamicListElement = DynamicListElement.builder().code(uuid).build();
        dynamicListElements = new ArrayList<>();

        dynamicListElements.add(dynamicListElement);

        caseData = CaseData.builder()
            .applicantCaseName("test")
            .id(123L)
            .reviewDocuments(ReviewDocuments.builder().reviewDocsDynamicList(DynamicList.builder().value(dynamicListElement).build()).build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        stringObjectMap = caseData.toMap(new ObjectMapper());

        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .build();

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);

    }

    @Test
    public void testHandleAboutToStart() throws Exception {

        when(reviewDocumentService.fetchDocumentDynamicListElements(caseData, caseDetails.getData())).thenReturn(dynamicListElements);
        reviewDocumentsController.handleAboutToStart(auth, callbackRequest);
        verify(reviewDocumentService).fetchDocumentDynamicListElements(caseData, caseDetails.getData());
        verifyNoMoreInteractions(reviewDocumentService);
    }

    @Test
    public void testHandleMidEvent() {
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        reviewDocumentsController.handleMidEvent(auth, callbackRequest);
        verify(reviewDocumentService).getReviewedDocumentDetailsNew(caseData,stringObjectMap);
        verifyNoMoreInteractions(reviewDocumentService);
    }

    @Test
    public void testHandleAboutToSubmit() throws Exception {
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        reviewDocumentsController.handleAboutToSubmit(auth, callbackRequest);
        verify(reviewDocumentService).processReviewDocument(stringObjectMap,caseData,uuid);
        verifyNoMoreInteractions(reviewDocumentService);
    }

    @Test
    public void testHandleSubmitted() {
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        reviewDocumentsController.handleSubmitted(auth, callbackRequest);
        verify(reviewDocumentService).getReviewResult(caseData);
        verifyNoMoreInteractions(reviewDocumentService);
    }


}
