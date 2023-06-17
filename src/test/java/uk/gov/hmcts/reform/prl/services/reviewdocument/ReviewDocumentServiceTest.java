package uk.gov.hmcts.reform.prl.services.reviewdocument;

import org.junit.Assert;
import org.junit.Before;
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

import java.time.LocalDate;
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
        +  System.lineSeparator()
        + " This document is visible to all parties and can be viewed in the case documents tab.";
    private static final String REVIEW_NOT_SURE = "### You need to confirm if the uploaded document needs to be restricted"
        + System.lineSeparator()
        + "If you are not sure, you can use Send and reply to messages to get further information about whether "
        + "the document needs to be restricted.";

    @InjectMocks
    ReviewDocumentService reviewDocumentService;

    Element element;

    @Before
    public void init() {

        element =  Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(QuarantineLegalDoc.builder()
                       .categoryId("test")
                       .notes("test")
                       .documentUploadedDate(LocalDateTime.now())
                       .document(Document.builder().build())
                       .build()).build();

    }


    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentArePresentLegalProfQuarantineDocsList() {
        CaseData caseData =  CaseData.builder()
            .legalProfQuarantineDocsList(List.of(ElementUtils.element(QuarantineLegalDoc.builder()
                                                                          .documentUploadedDate(LocalDateTime.now())
                                                                          .document(Document.builder().build())
                                                                          .build())))
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();

        Assert.assertTrue(!reviewDocumentService.getDynamicListElements(caseData).isEmpty());
    }

    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForCafcassQuarantineDocsList() {
        CaseData caseData =  CaseData.builder()
            .cafcassQuarantineDocsList(List.of(ElementUtils.element(QuarantineLegalDoc.builder()
                                                                          .documentUploadedDate(LocalDateTime.now())
                                                                          .document(Document.builder().build())
                                                                          .cafcassQuarantineDocument(Document.builder()
                                                                                                         .documentFileName("filename")
                                                                                                         .build())
                                                                          .build())))
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();

        Assert.assertTrue(!reviewDocumentService.getDynamicListElements(caseData).isEmpty());
    }

    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForCourtStaffQuarantineDocsList() {
        CaseData caseData =  CaseData.builder()
            .courtStaffQuarantineDocsList(List.of(ElementUtils.element(QuarantineLegalDoc.builder()
                                                                        .documentUploadedDate(LocalDateTime.now())
                                                                        .document(Document.builder().build())
                                                                        .courtStaffQuarantineDocument(Document.builder()
                                                                                                       .documentFileName("filename")
                                                                                                       .build())
                                                                        .build())))
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();

        Assert.assertTrue(!reviewDocumentService.getDynamicListElements(caseData).isEmpty());
    }

    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForCitizenUploadQuarantineDocsList() {
        CaseData caseData =  CaseData.builder()
            .citizenUploadQuarantineDocsList(List.of(ElementUtils.element(UploadedDocuments.builder()
                                                                              .dateCreated(LocalDate.now())
                                                                           .citizenDocument(Document.builder()
                                                                                                             .documentFileName("filename")
                                                                                                             .build())
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
        CaseData caseData =  CaseData.builder()
            .legalProfQuarantineDocsList(List.of(element))
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build())
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetails(caseData,caseDataMap);
        Assert.assertNotNull(caseDataMap.get("docToBeReviewed"));
    }

    @Test
    public void testGetDocumentDetailsWhenUploadedByCafcassProfessional() {

        CaseData caseData =  CaseData.builder()
            .cafcassQuarantineDocsList(List.of(element))
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build())
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetails(caseData,caseDataMap);
        Assert.assertNotNull(caseDataMap.get("docToBeReviewed"));
    }

    @Test
    public void testGetDocumentDetailsWhenUploadedByCourtStaffProfessional() {

        CaseData caseData =  CaseData.builder()
            .courtStaffQuarantineDocsList(List.of(element))
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build())
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetails(caseData,caseDataMap);
        Assert.assertNotNull(caseDataMap.get("docToBeReviewed"));
    }

    @Test
    public void testGetDocumentDetailsWhenUploadedByCitizen() {
        Element element =  Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(UploadedDocuments.builder()
                       .citizenDocument(Document.builder()
                                            .build())
                       .partyName("test")
                       .documentType("test").build()).build();
        CaseData caseData =  CaseData.builder()
            .citizenUploadQuarantineDocsList(List.of(element))
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build())
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes).build())
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetails(caseData,caseDataMap);
        Assert.assertNotNull(caseDataMap.get("docToBeReviewed"));
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForLegalProfQuarantineDocsWhenYesIsSelected() {

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData =  CaseData.builder()
            .legalProfQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes)
                                 .legalProfUploadDocListConfTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));
        Assert.assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListConfTab());

        List<Element<QuarantineLegalDoc>>  listQuarantineLegalDoc =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get("legalProfUploadDocListConfTab");

        Assert.assertEquals(caseData.getReviewDocuments().getLegalProfUploadDocListConfTab().get(0).getValue().getCategoryId(),
                            listQuarantineLegalDoc.get(0).getValue().getCategoryId());
        Assert.assertEquals(caseData.getReviewDocuments().getLegalProfUploadDocListConfTab().get(0).getValue().getNotes(),
                            listQuarantineLegalDoc.get(0).getValue().getNotes());
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForCafcassQuarantineDocsWhenYesIsSelected() {

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData =  CaseData.builder()
            .cafcassQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes)
                                 .cafcassUploadDocListConfTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));
        Assert.assertNotNull(caseData.getReviewDocuments().getCafcassUploadDocListConfTab());

        List<Element<QuarantineLegalDoc>>  listQuarantineLegalDoc = (List<Element<QuarantineLegalDoc>>)caseDataMap.get("cafcassUploadDocListConfTab");

        Assert.assertEquals(caseData.getReviewDocuments().getCafcassUploadDocListConfTab().get(0).getValue().getCategoryId(),
                            listQuarantineLegalDoc.get(0).getValue().getCategoryId());
        Assert.assertEquals(caseData.getReviewDocuments().getCafcassUploadDocListConfTab().get(0).getValue().getNotes(),
                            listQuarantineLegalDoc.get(0).getValue().getNotes());
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForCourtStaffQuarantineDocsWhenYesIsSelected() {

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData =  CaseData.builder()
            .courtStaffQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes)
                                 .courtStaffUploadDocListConfTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));
        Assert.assertNotNull(caseData.getReviewDocuments().getCourtStaffUploadDocListConfTab());

        List<Element<QuarantineLegalDoc>>  listQuarantineLegalDoc =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get("courtStaffUploadDocListConfTab");

        Assert.assertEquals(caseData.getReviewDocuments().getCourtStaffUploadDocListConfTab().get(0).getValue().getCategoryId(),
                            listQuarantineLegalDoc.get(0).getValue().getCategoryId());
        Assert.assertEquals(caseData.getReviewDocuments().getCourtStaffUploadDocListConfTab().get(0).getValue().getNotes(),
                            listQuarantineLegalDoc.get(0).getValue().getNotes());
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForCitizenUploadQuarantineDocsWhenYesIsSelected() {
        Element element =  Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(UploadedDocuments.builder().dateCreated(LocalDate.now())
                       .isApplicant("yes")
                       .build()).build();
        List<Element<UploadedDocuments>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData =  CaseData.builder()
            .citizenUploadQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes)
                                 .citizenUploadDocListConfTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));
        Assert.assertNotNull(caseData.getReviewDocuments().getCitizenUploadDocListConfTab());

        List<Element<UploadedDocuments>>  listQuarantineLegalDoc = (List<Element<UploadedDocuments>>)caseDataMap.get("citizenUploadDocListConfTab");

        Assert.assertEquals(caseData.getReviewDocuments().getCitizenUploadDocListConfTab().get(0).getValue().getIsApplicant(),
                            listQuarantineLegalDoc.get(0).getValue().getIsApplicant());
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForLegalProfQuarantineDocsWhenNoIsSelected() {

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData =  CaseData.builder()
            .legalProfQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.no)
                                 .legalProfUploadDocListDocTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));
        Assert.assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());
        List<Element<QuarantineLegalDoc>>  listQuarantineLegalDoc =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get("legalProfUploadDocListDocTab");

        Assert.assertEquals(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab().get(0).getValue().getCategoryId(),
                            listQuarantineLegalDoc.get(0).getValue().getCategoryId());
        Assert.assertEquals(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab().get(0).getValue().getNotes(),
                            listQuarantineLegalDoc.get(0).getValue().getNotes());
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForCafcassQuarantineDocsWhenNoIsSelected() {

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData =  CaseData.builder()
            .cafcassQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.no)
                                 .cafcassUploadDocListDocTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));
        Assert.assertNotNull(caseData.getReviewDocuments().getCafcassUploadDocListDocTab());
        List<Element<QuarantineLegalDoc>>  listQuarantineLegalDoc = (List<Element<QuarantineLegalDoc>>)caseDataMap.get("cafcassUploadDocListDocTab");

        Assert.assertEquals(caseData.getReviewDocuments().getCafcassUploadDocListDocTab().get(0).getValue().getCategoryId(),
                            listQuarantineLegalDoc.get(0).getValue().getCategoryId());
        Assert.assertEquals(caseData.getReviewDocuments().getCafcassUploadDocListDocTab().get(0).getValue().getNotes(),
                            listQuarantineLegalDoc.get(0).getValue().getNotes());
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForCourtStaffQuarantineDocsWhenNoIsSelected() {

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData =  CaseData.builder()
            .courtStaffQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.no)
                                 .courtStaffUploadDocListDocTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));
        Assert.assertNotNull(caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab());
        List<Element<QuarantineLegalDoc>>  listQuarantineLegalDoc =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get("courtStaffUploadDocListDocTab");

        Assert.assertEquals(caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab().get(0).getValue().getCategoryId(),
                            listQuarantineLegalDoc.get(0).getValue().getCategoryId());
        Assert.assertEquals(caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab().get(0).getValue().getNotes(),
                            listQuarantineLegalDoc.get(0).getValue().getNotes());
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForCitizenUploadQuarantineDocsWhenNoIsSelected() {
        Element element =  Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(UploadedDocuments.builder().dateCreated(LocalDate.now())
                       .isApplicant("yes")
                       .build()).build();
        List<Element<UploadedDocuments>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData =  CaseData.builder()
            .citizenUploadQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.no)
                                 .citizenUploadedDocListDocTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));
        Assert.assertNotNull(caseData.getReviewDocuments().getCitizenUploadedDocListDocTab());

        List<Element<UploadedDocuments>>  listQuarantineLegalDoc = (List<Element<UploadedDocuments>>)caseDataMap.get("citizenUploadedDocListDocTab");
        Assert.assertEquals(caseData.getReviewDocuments().getCitizenUploadedDocListDocTab().get(0).getValue().getIsApplicant(),
                            listQuarantineLegalDoc.get(0).getValue().getIsApplicant());
    }


    @Test
    public void testReviewResultWhenYesOptionSelected() {

        CaseData caseData =  CaseData.builder()
            .legalProfQuarantineDocsList(List.of(element))
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

        CaseData caseData =  CaseData.builder()
            .legalProfQuarantineDocsList(List.of(element))
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

        CaseData caseData =  CaseData.builder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.dontKnow).build())
            .legalProfQuarantineDocsList(List.of(element))
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);
        Assert.assertNotNull(response);
        Assert.assertEquals(DOCUMENT_IN_REVIEW, response.getBody().getConfirmationHeader());
        Assert.assertEquals(REVIEW_NOT_SURE, response.getBody().getConfirmationBody());
    }

}
