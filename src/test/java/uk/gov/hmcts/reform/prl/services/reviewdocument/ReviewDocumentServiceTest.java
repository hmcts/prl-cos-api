package uk.gov.hmcts.reform.prl.services.reviewdocument;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.ScannedDocument;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.TestConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ReviewDocumentServiceTest {

    public static final String DOCUMENT_SUCCESSFULLY_REVIEWED = "# Document successfully reviewed";
    String auth = "authorisation";
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

    @Mock
    CoreCaseDataService coreCaseDataService;

    @Mock
    CaseDocumentClient caseDocumentClient;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    SystemUserService systemUserService;

    Element element;

    @Before
    public void init() {

        element =  Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(QuarantineLegalDoc.builder()
                       .categoryId("test")
                       .notes("test")
                       .documentUploadedDate(LocalDateTime.now())
                       .document(Document
                                     .builder()
                                     .documentUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/"
                                                      + "6d664075-2166-43cf-a4cc-61058a3a0a99")
                                     .build())
                       .courtStaffQuarantineDocument(Document
                                                         .builder()
                                                         .documentUrl("http://dm-store-aat.service.core-compute-aat.internal"
                                                                          + "/documents/"
                                                                          + "6d664075-2166-43cf-a4cc-61058a3a0a99").build())
                       .cafcassQuarantineDocument(Document
                                                      .builder()
                                                      .documentUrl("http://dm-store-aat.service.core-compute-aat.internal"
                                                                       + "/documents/"
                                                                       + "6d664075-2166-43cf-a4cc-61058a3a0a99").build())
                       .legalProfQuarantineDocument(Document
                                                        .builder()
                                                        .documentUrl("http://dm-store-aat.service.core-compute-aat.internal"
                                                                         + "/documents/"
                                                                         + "6d664075-2166-43cf-a4cc-61058a3a0a99").build())
                       .build()).build();

        Mockito.when(systemUserService.getSysUserToken()).thenReturn(auth);

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
    public void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForBulkscanDocuments() {
        CaseData caseData =  CaseData.builder()
            .scannedDocuments(List.of(ElementUtils.element(
                ScannedDocument.builder()
                    .scannedDate(LocalDateTime.now())
                    .url(Document.builder().build())
                    .controlNumber("123")
                    .deliveryDate(LocalDateTime.now())
                    .exceptionRecordReference("EXREF")
                    .type("Other")
                    .subtype("test")
                    .fileName("filename")
                    .build()

            )))
            .build();

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
    public void testGetDocumentDetailsWhenUploadedByBulkscan() {
        Element element1 =  Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(ScannedDocument.builder()
                       .scannedDate(LocalDateTime.now())
                       .url(Document.builder().build())
                       .controlNumber("123")
                       .deliveryDate(LocalDateTime.now())
                       .exceptionRecordReference("EXREF")
                       .type("Other")
                       .subtype("test")
                       .fileName("filename")
                       .build()).build();
        CaseData caseData =  CaseData.builder()
            .scannedDocuments(List.of(element1))
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build())
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes).build()).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetails(caseData,caseDataMap);
        Assert.assertNotNull(caseDataMap.get("docToBeReviewed"));
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForLegalProfQuarantineDocsWhenYesIsSelected() {

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        Resource expectedResource = new ClassPathResource("documents/document.pdf");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, OK);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TestConstants.TEST_SERVICE_AUTHORIZATION);
        Mockito.when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any(UUID.class)))
            .thenReturn(expectedResponse);
        Mockito.when(caseDocumentClient.uploadDocuments(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(List.class)
        )).thenReturn(createDocumentUploadResponse());
        CaseData caseData =  CaseData.builder()
            .legalProfQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes)
                                 .legalProfUploadDocListConfTab(documentList).build())
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
        Resource expectedResource = new ClassPathResource("documents/document.pdf");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, OK);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TestConstants.TEST_SERVICE_AUTHORIZATION);
        Mockito.when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any(UUID.class)))
            .thenReturn(expectedResponse);
        Mockito.when(caseDocumentClient.uploadDocuments(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(List.class)
        )).thenReturn(createDocumentUploadResponse());
        CaseData caseData =  CaseData.builder()
            .cafcassQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes)
                                 .cafcassUploadDocListConfTab(documentList).build())
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
        Resource expectedResource = new ClassPathResource("documents/document.pdf");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, OK);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TestConstants.TEST_SERVICE_AUTHORIZATION);
        Mockito.when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any(UUID.class)))
            .thenReturn(expectedResponse);
        Mockito.when(caseDocumentClient.uploadDocuments(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(List.class)
        )).thenReturn(createDocumentUploadResponse());
        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData =  CaseData.builder()
            .courtStaffQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes)
                                 .courtStaffUploadDocListConfTab(documentList).build())
            .courtStaffQuarantineDocsList(documentList)
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
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
            .citizenUploadQuarantineDocsList(List.of(ElementUtils.element(UploadedDocuments.builder().build())))
            .cafcassQuarantineDocsList(List.of(ElementUtils.element(QuarantineLegalDoc.builder()
                                                                        .documentUploadedDate(LocalDateTime.now())
                                                                        .document(Document.builder().build())
                                                                        .cafcassQuarantineDocument(Document.builder()
                                                                                                       .documentFileName("filename")
                                                                                                       .build())
                                                                        .build())))
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
            .citizenUploadQuarantineDocsList(List.of(ElementUtils.element(UploadedDocuments.builder().build())))
            .cafcassQuarantineDocsList(List.of(ElementUtils.element(QuarantineLegalDoc.builder()
                                                                        .documentUploadedDate(LocalDateTime.now())
                                                                        .document(Document.builder().build())
                                                                        .cafcassQuarantineDocument(Document.builder()
                                                                                                       .documentFileName("filename")
                                                                                                       .build())
                                                                        .build())))
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
            .citizenUploadQuarantineDocsList(List.of(ElementUtils.element(UploadedDocuments.builder()
                                                                              .dateCreated(LocalDate.now())
                                                                              .citizenDocument(Document.builder()
                                                                                                   .documentFileName("filename")
                                                                                                   .build())
                                                                              .build())))
            .cafcassQuarantineDocsList(List.of(ElementUtils.element(QuarantineLegalDoc.builder()
                                                                        .documentUploadedDate(LocalDateTime.now())
                                                                        .document(Document.builder().build())
                                                                        .cafcassQuarantineDocument(Document.builder()
                                                                                                       .documentFileName("filename")
                                                                                                       .build())
                                                                        .build())))
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);
        Assert.assertNotNull(response);
        Assert.assertEquals(DOCUMENT_IN_REVIEW, response.getBody().getConfirmationHeader());
        Assert.assertEquals(REVIEW_NOT_SURE, response.getBody().getConfirmationBody());
    }

    @Test
    public void testReviewResultWhenCaseTypeApplicationC100() {

        CaseData caseData =  CaseData.builder().caseTypeOfApplication(C100_CASE_TYPE)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.dontKnow).build())
            .legalProfQuarantineDocsList(List.of(element))
            .citizenUploadQuarantineDocsList(new ArrayList<>())
            .cafcassQuarantineDocsList(List.of(ElementUtils.element(QuarantineLegalDoc.builder()
                                                                        .documentUploadedDate(LocalDateTime.now())
                                                                        .document(Document.builder().build())
                                                                        .cafcassQuarantineDocument(Document.builder()
                                                                                                       .documentFileName("filename")
                                                                                                       .build())
                                                                        .build())))
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build())))
                .build();
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);
        Assert.assertNotNull(response);
        Assert.assertEquals(DOCUMENT_IN_REVIEW, response.getBody().getConfirmationHeader());
        Assert.assertEquals(REVIEW_NOT_SURE, response.getBody().getConfirmationBody());
    }


    @Test
    public void testReviewResultWhenLegalCourtCitizenCafcassQuarantineDocListIsEmpty() {

        CaseData caseData =  CaseData.builder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes).build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Mockito.doNothing().when(coreCaseDataService).triggerEvent(Mockito.anyString(),
                                                                  Mockito.anyString(),
                                                                  Mockito.anyLong(),
                                                                  Mockito.anyString(),
                                                                  Mockito.any(Map.class));
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);
        Assert.assertNotNull(response);
        Assert.assertEquals(DOCUMENT_SUCCESSFULLY_REVIEWED, response.getBody().getConfirmationHeader());
        Assert.assertEquals(REVIEW_YES, response.getBody().getConfirmationBody());
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForScanDocWhenNoIsSelected() {

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData =  CaseData.builder()
            .scannedDocuments(List.of(ElementUtils.element(
                ScannedDocument.builder()
                    .scannedDate(LocalDateTime.now())
                    .url(Document.builder().build())
                    .controlNumber("123")
                    .deliveryDate(LocalDateTime.now())
                    .exceptionRecordReference("EXREF")
                    .type("Other")
                    .subtype("test")
                    .fileName("filename")
                    .build()

            )))
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.no)
                                 .legalProfUploadDocListDocTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));
        Assert.assertNotNull(caseDataMap.get("scannedDocuments"));
        List<Element<ScannedDocument>>  listScannedDocuments =
            (List<Element<ScannedDocument>>) caseDataMap.get("scannedDocuments");

        Assert.assertEquals(caseData.getScannedDocuments().get(0).getValue().controlNumber,
                            listScannedDocuments.get(0).getValue().getControlNumber());

        Assert.assertEquals(caseData.getScannedDocuments().get(0).getValue().exceptionRecordReference,
                            listScannedDocuments.get(0).getValue().getExceptionRecordReference());

    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForScanDocWhenYesIsSelected() {

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        Resource expectedResource = new ClassPathResource("documents/document.pdf");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, OK);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TestConstants.TEST_SERVICE_AUTHORIZATION);
        Mockito.when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any(UUID.class)))
            .thenReturn(expectedResponse);
        Mockito.when(caseDocumentClient.uploadDocuments(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(List.class)
        )).thenReturn(createDocumentUploadResponse());
        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData =  CaseData.builder()
            .scannedDocuments(List.of(ElementUtils.element(
                ScannedDocument.builder()
                    .scannedDate(LocalDateTime.now())
                    .url(Document.builder()
                             .documentUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/"
                                              + "6d664075-2166-43cf-a4cc-61058a3a0a99").build())
                    .controlNumber("123")
                    .deliveryDate(LocalDateTime.now())
                    .exceptionRecordReference("EXREF")
                    .type("Other")
                    .subtype("test")
                    .fileName("filename")
                    .build()

            )))
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes)
                                 .legalProfUploadDocListConfTab(documentList).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));
        Assert.assertNotNull(caseDataMap.get("scannedDocuments"));
        List<Element<ScannedDocument>>  listScannedDocuments =
            (List<Element<ScannedDocument>>) caseDataMap.get("scannedDocuments");

        Assert.assertEquals(caseData.getScannedDocuments().get(0).getValue().controlNumber,
                            listScannedDocuments.get(0).getValue().getControlNumber());

        Assert.assertEquals(caseData.getScannedDocuments().get(0).getValue().exceptionRecordReference,
                            listScannedDocuments.get(0).getValue().getExceptionRecordReference());

    }

    @Test (expected = RuntimeException.class)
    public void testReviewProcessOfDocumentWhenIdMatchFailsForQuarantineDocsWhenYesIsSelected() {
        Element element =  Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(QuarantineLegalDoc.builder()
                       .categoryId("test")
                       .notes("test")
                       .documentUploadedDate(LocalDateTime.now())
                       .cafcassQuarantineDocument(Document
                                                      .builder()
                                                      .documentUrl("http://dm-store-aat.service.core-compute-aat."
                                                                       + "internal/documents/")
                                                      .build())
                       .build()).build();

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        Resource expectedResource = new ClassPathResource("documents/document.pdf");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, OK);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TestConstants.TEST_SERVICE_AUTHORIZATION);
        Mockito.when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any(UUID.class)))
            .thenReturn(expectedResponse);
        Mockito.when(caseDocumentClient.uploadDocuments(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(List.class)
        )).thenReturn(new UploadResponse(new ArrayList<>()));
        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData =  CaseData.builder()
            .cafcassQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes)
                                 .cafcassUploadDocListConfTab(documentList).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));
    }

    @Test (expected = RuntimeException.class)
    public void testReviewProcessOfDocumentWhenNewUploadFailsForQuarantineDocsWhenYesIsSelected() {
        Element element =  Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(QuarantineLegalDoc.builder()
                       .categoryId("test")
                       .notes("test")
                       .documentUploadedDate(LocalDateTime.now())
                       .cafcassQuarantineDocument(Document
                                                      .builder()
                                                      .documentUrl("http://dm-store-aat.service.core-compute-aat.internal"
                                                                       + "/documents/"
                                                                       + "6d664075-2166-43cf-a4cc-61058a3a0a99").build())
                       .build()).build();

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        Resource expectedResource = new ClassPathResource("documents/document.pdf");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, OK);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TestConstants.TEST_SERVICE_AUTHORIZATION);
        Mockito.when(caseDocumentClient.getDocumentBinary(anyString(), anyString(), any(UUID.class)))
            .thenReturn(expectedResponse);
        Mockito.when(caseDocumentClient.uploadDocuments(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(List.class)
        )).thenReturn(new UploadResponse(new ArrayList<>()));
        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData =  CaseData.builder()
            .cafcassQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoDontKnow.yes)
                                 .cafcassUploadDocListConfTab(documentList).build())
            .citizenUploadedDocumentList(List.of(ElementUtils.element(UploadedDocuments.builder().build()))).build();
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));
    }

    private UploadResponse createDocumentUploadResponse() {
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link binaryLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        binaryLink.href = randomAlphanumeric(10);
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link selfLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        selfLink.href = randomAlphanumeric(10);

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Links links = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        links.binary = binaryLink;
        links.self = selfLink;

        uk.gov.hmcts.reform.ccd.document.am.model.Document document = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();
        document.links = links;
        document.originalDocumentName = randomAlphanumeric(10);


        return new UploadResponse(List.of(document));
    }
}
