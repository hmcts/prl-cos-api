package uk.gov.hmcts.reform.prl.services.reviewdocument;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ReviewDocumentServiceTest {

    public static final String DOCUMENT_SUCCESSFULLY_REVIEWED = "# Document successfully reviewed";
    public static final String DOCUMENT_IN_REVIEW = "# Document review in progress";
    private static final String REVIEW_YES = "### You have successfully reviewed this document"
        + System.lineSeparator()
        + "This document can only be seen by court staff, Cafcass and the judiciary. "
        + "You can view it in case file view and the confidential details tab.";
    private static final String REVIEW_NO = "### You have successfully reviewed this document"
        + System.lineSeparator()
        + " This document is visible to all parties and can be viewed in the case documents tab.";
    private static final String REVIEW_NOT_SURE = "### You need to confirm if the uploaded document needs to be restricted"
        + System.lineSeparator()
        + "If you are not sure, you can use Send and reply to messages to get further information about whether "
        + "the document needs to be restricted.";

    @InjectMocks
    ReviewDocumentService reviewDocumentService;

    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentArePresent() {
        CaseData caseData = CaseData.builder()
            .legalProfQuarantineDocsList(List.of(ElementUtils.element(QuarantineLegalDoc.builder()
                                                                          .documentUploadedDate(LocalDateTime.now())
                                                                          .document(Document.builder().build())
                                                                          .build())))
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();

        Assert.assertTrue(!reviewDocumentService.getDynamicListElements(caseData).isEmpty());
    }

    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentsAreNotPresent() {
        Assert.assertTrue(reviewDocumentService.getDynamicListElements(CaseData.builder().build()).isEmpty());
    }

    @Test
    public void testGetDocumentDetailsWhenUploadedByLegalProfessional() {
        Element element = Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(QuarantineLegalDoc.builder()
                       .categoryId("test")
                       .notes("test")
                       .documentUploadedDate(LocalDateTime.now())
                       .document(Document.builder().build())
                       .build()).build();
        CaseData caseData = CaseData.builder()
            .legalProfQuarantineDocsList(List.of(element))
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build())
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetails(caseData, caseDataMap);
        Assert.assertNotNull(caseDataMap.get("docToBeReviewed"));
    }

    @Test
    public void testGetDocumentDetailsWhenUploadedByCitizen() {
        Element element = Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(UploadedDocuments.builder()
                       .citizenDocument(Document.builder()
                                            .build())
                       .partyName("test")
                       .documentType("test").build()).build();
        CaseData caseData = CaseData.builder()
            .citizenUploadQuarantineDocsList(List.of(element))
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build())
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes).build())
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetails(caseData, caseDataMap);
        Assert.assertNotNull(caseDataMap.get("docToBeReviewed"));
    }

    //@Test
    public void testReviewProcessOfDocumentToConfidentialTabWhenYesIsSelected() {
        Element element = Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(QuarantineLegalDoc.builder()
                       .categoryId("test")
                       .notes("test")
                       .documentUploadedDate(LocalDateTime.now())
                       .document(Document.builder().build())
                       .build()).build();
        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData = CaseData.builder()
            .legalProfQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes)
                                 .legalProfUploadDocListConfTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );
        Assert.assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListConfTab());
        //Assert.assertEquals(caseData.getReviewDocuments().getLegalProfUploadDocListConfTab().get(0).getValue().getCategoryId(),
        //                    caseDataMap.get("categoryId"));
        //Assert.assertEquals(caseData.getReviewDocuments().getLegalProfUploadDocListConfTab().get(0).getValue().getNotes(),
        //                    caseDataMap.get("notes"));
    }

    @Test
    public void testReviewResultWhenYesOptionSelected() {
        Element element = Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(QuarantineLegalDoc.builder()
                       .categoryId("test")
                       .notes("test")
                       .documentUploadedDate(LocalDateTime.now())
                       .document(Document.builder().build())
                       .build()).build();
        CaseData caseData = CaseData.builder()
            .legalProfQuarantineDocsList(List.of(element))
            .cafcassQuarantineDocsList(List.of(element))
            .citizenUploadQuarantineDocsList(List.of(element))
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);
        Assert.assertNotNull(response);
        Assert.assertEquals(DOCUMENT_SUCCESSFULLY_REVIEWED, response.getBody().getConfirmationHeader());
        Assert.assertEquals(REVIEW_YES, response.getBody().getConfirmationBody());
    }

    @Test
    public void testReviewResultWhenNoOptionSelected() {
        Element element = Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(QuarantineLegalDoc.builder()
                       .categoryId("test")
                       .notes("test")
                       .documentUploadedDate(LocalDateTime.now())
                       .document(Document.builder().build())
                       .build()).build();
        CaseData caseData = CaseData.builder()
            .legalProfQuarantineDocsList(List.of(element))
            .cafcassQuarantineDocsList(List.of(element))
            .citizenUploadQuarantineDocsList(List.of(element))
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.no).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);
        Assert.assertNotNull(response);
        Assert.assertEquals(DOCUMENT_SUCCESSFULLY_REVIEWED, response.getBody().getConfirmationHeader());
        Assert.assertEquals(REVIEW_NO, response.getBody().getConfirmationBody());
    }

    @Test
    public void testReviewResultWhenDoNotKnowOptionSelected() {
        Element element = Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(QuarantineLegalDoc.builder()
                       .categoryId("test")
                       .notes("test")
                       .documentUploadedDate(LocalDateTime.now())
                       .document(Document.builder().build())
                       .build()).build();
        CaseData caseData = CaseData.builder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.dontKnow).build())
            .legalProfQuarantineDocsList(List.of(element))
            .cafcassQuarantineDocsList(List.of(element))
            .citizenUploadQuarantineDocsList(List.of(element))
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);
        Assert.assertNotNull(response);
        Assert.assertEquals(DOCUMENT_IN_REVIEW, response.getBody().getConfirmationHeader());
        Assert.assertEquals(REVIEW_NOT_SURE, response.getBody().getConfirmationBody());
    }

}
