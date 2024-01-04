package uk.gov.hmcts.reform.prl.services.reviewdocument;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.enums.YesNoNotSure;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONFIDENTIAL_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESTRICTED_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MULTIPART_FILE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

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
        + "If you are not sure, you can use <a href=\"/cases/case-details/123/trigger/sendOrReplyToMessages/sendOrReplyToMessages1\">Send and reply to messages</a> to get further information about whether"
        + " the document needs to be restricted.";

    @InjectMocks
    ReviewDocumentService reviewDocumentService;

    @Mock
    CoreCaseDataService coreCaseDataService;
    @Mock
    AuthTokenGenerator authTokenGenerator;
    @Mock
    SystemUserService systemUserService;
    @Mock
    CaseDocumentClient caseDocumentClient;

    @Mock
    ObjectMapper objectMapper;

    Element element;
    Document document;
    QuarantineLegalDoc quarantineLegalDoc;
    private String authToken;
    private String s2sToken;

    @Before
    public void init() {

        element = Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(QuarantineLegalDoc.builder()
                       .categoryId("cafcassQuarantineDocument")
                       .notes("test")
                       .documentUploadedDate(LocalDateTime.now())
                       .document(Document.builder().build())
                       .build()).build();
        authToken = "auth-token";
        s2sToken = "s2sToken";
        Mockito.when(systemUserService.getSysUserToken()).thenReturn(authToken);
        Mockito.when(authTokenGenerator.generate()).thenReturn(s2sToken);

        document = Document.builder()
            .documentFileName("test.pdf")
            .documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15")
            .build();

        quarantineLegalDoc = QuarantineLegalDoc.builder()
            .documentParty(DocumentPartyEnum.APPLICANT.getDisplayedValue())
            .categoryId("MIAMCertificate")
            .documentUploadedDate(LocalDateTime.now())
            .build();

        Resource expectedResource = new ClassPathResource("task-list-markdown.md");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, HttpStatus.OK);
        Mockito.when(caseDocumentClient.getDocumentBinary(authToken, s2sToken, UUID.fromString("7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15")))
            .thenReturn(expectedResponse);

        byte[] pdf = new byte[]{1,2,3,4,5};
        MultipartFile file = new InMemoryMultipartFile(SOA_MULTIPART_FILE,
                                                       "Confidential_" + document.getDocumentFileName(),
                                                       APPLICATION_PDF_VALUE, pdf);
        uk.gov.hmcts.reform.ccd.document.am.model.Document document = testDocument();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        Mockito.when(caseDocumentClient.uploadDocuments(anyString(), anyString(), anyString(), anyString(), anyList()))
            .thenReturn(uploadResponse);
        Mockito.doNothing().when(caseDocumentClient).deleteDocument(anyString(), anyString(), any(UUID.class), anyBoolean());
    }

    private static uk.gov.hmcts.reform.ccd.document.am.model.Document testDocument() {
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link binaryLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        binaryLink.href = "http://test.link";
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link selfLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        selfLink.href = "http://test.link";

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Links links = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        links.binary = binaryLink;
        links.self = selfLink;

        uk.gov.hmcts.reform.ccd.document.am.model.Document document = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();
        document.links = links;
        document.originalDocumentName = "Confidential_test.pdf";
        return document;
    }


    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentArePresentLegalProfQuarantineDocsList() {
        CaseData caseData =  CaseData.builder()
            .legalProfQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder()
                                                                          .documentUploadedDate(LocalDateTime.now())
                                                                          .document(Document.builder().build())
                                                                          .build())))
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();

        Assert.assertTrue(!reviewDocumentService.getDynamicListElements(caseData).isEmpty());
    }

    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForCafcassQuarantineDocsList() {
        CaseData caseData =  CaseData.builder()
            .cafcassQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder()
                                                                        .documentUploadedDate(LocalDateTime.now())
                                                                        .document(Document.builder().build())
                                                                        .cafcassQuarantineDocument(Document.builder()
                                                                                                       .documentFileName("filename")
                                                                                                       .build())
                                                                        .build())))
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();

        Assert.assertTrue(!reviewDocumentService.getDynamicListElements(caseData).isEmpty());
    }

    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForBulkscanDocuments() {
        CaseData caseData =  CaseData.builder()
            .scannedDocuments(List.of(element(
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
            .courtStaffQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder()
                                                                           .documentUploadedDate(LocalDateTime.now())
                                                                           .document(Document.builder().build())
                                                                           .courtStaffQuarantineDocument(Document.builder()
                                                                                                             .documentFileName("filename")
                                                                                                             .build())
                                                                           .build())))
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();

        Assert.assertTrue(!reviewDocumentService.getDynamicListElements(caseData).isEmpty());
    }

    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForCitizenUploadQuarantineDocsList() {
        CaseData caseData =  CaseData.builder()
            .citizenUploadQuarantineDocsList(List.of(element(UploadedDocuments.builder()
                                                                              .dateCreated(LocalDate.now())
                                                                              .citizenDocument(Document.builder()
                                                                                                   .documentFileName("filename")
                                                                                                   .build())
                                                                              .build())))
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();

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
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes).build())
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();

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
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes).build())
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();

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
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes).build())
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();

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
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes).build())
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
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes).build()).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetails(caseData,caseDataMap);
        Assert.assertNotNull(caseDataMap.get("docToBeReviewed"));
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForLegalProfQuarantineDocsWhenYesIsSelected() {

        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                       quarantineLegalDoc));

        CaseData caseData =  CaseData.builder()
            .legalProfQuarantineDocsList(quarantineDocsList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .confidentialDocuments(new ArrayList<>())
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(QuarantineLegalDoc.builder()
                            .categoryId(
                                "cafcassQuarantineDocument")
                            .notes("test")
                            .documentUploadedDate(
                                LocalDateTime.now())
                            .document(Document.builder().build())
                            .build());
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseData.getReviewDocuments().getConfidentialDocuments());
        List<Element<QuarantineLegalDoc>>  confidentialDocs =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get(CONFIDENTIAL_DOCUMENTS);
        Assert.assertEquals(YesOrNo.Yes, confidentialDocs.get(0).getValue().getIsConfidential());
        Assert.assertEquals(YesOrNo.No, confidentialDocs.get(0).getValue().getIsRestricted());
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForCafcassQuarantineDocsWhenYesIsSelected() {

        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .cafcassQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                       quarantineLegalDoc));

        CaseData caseData =  CaseData.builder()
            .cafcassQuarantineDocsList(quarantineDocsList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .confidentialDocuments(new ArrayList<>())
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(QuarantineLegalDoc.builder()
                            .categoryId(
                                "cafcassQuarantineDocument")
                            .notes("test")
                            .documentUploadedDate(
                                LocalDateTime.now())
                            .document(Document.builder().build())
                            .build());
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseData.getReviewDocuments().getConfidentialDocuments());
        List<Element<QuarantineLegalDoc>>  confidentialDocs =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get(CONFIDENTIAL_DOCUMENTS);
        Assert.assertEquals(YesOrNo.Yes, confidentialDocs.get(0).getValue().getIsConfidential());
        Assert.assertEquals(YesOrNo.No, confidentialDocs.get(0).getValue().getIsRestricted());
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForCourtStaffQuarantineDocsWhenYesIsSelected() {

        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .courtStaffQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                       quarantineLegalDoc));

        CaseData caseData =  CaseData.builder()
            .courtStaffQuarantineDocsList(quarantineDocsList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .confidentialDocuments(new ArrayList<>())
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseData.getReviewDocuments().getConfidentialDocuments());
        List<Element<QuarantineLegalDoc>>  confidentialDocs =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get(CONFIDENTIAL_DOCUMENTS);
        Assert.assertEquals(YesOrNo.Yes, confidentialDocs.get(0).getValue().getIsConfidential());
        Assert.assertEquals(YesOrNo.No, confidentialDocs.get(0).getValue().getIsRestricted());
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
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .citizenUploadDocListConfTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();
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
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .legalProfUploadDocListDocTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(QuarantineLegalDoc.builder()
                            .categoryId(
                                "cafcassQuarantineDocument")
                            .notes("test")
                            .documentUploadedDate(
                                LocalDateTime.now())
                            .document(Document.builder().build())
                            .build());
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
        CaseData caseData = CaseData.builder()
            .cafcassQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .cafcassUploadDocListDocTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(QuarantineLegalDoc.builder()
                            .categoryId(
                                "cafcassQuarantineDocument")
                            .notes("test")
                            .documentUploadedDate(
                                LocalDateTime.now())
                            .document(Document.builder().build())
                            .build());
        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );
        Assert.assertNotNull(caseData.getReviewDocuments().getCafcassUploadDocListDocTab());
        List<Element<QuarantineLegalDoc>> listQuarantineLegalDoc = (List<Element<QuarantineLegalDoc>>) caseDataMap.get(
            "cafcassUploadDocListDocTab");

        Assert.assertEquals(
            caseData.getReviewDocuments().getCafcassUploadDocListDocTab().get(0).getValue().getCategoryId(),
            listQuarantineLegalDoc.get(0).getValue().getCategoryId()
        );
        Assert.assertEquals(
            caseData.getReviewDocuments().getCafcassUploadDocListDocTab().get(0).getValue().getNotes(),
            listQuarantineLegalDoc.get(0).getValue().getNotes()
        );
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForCourtStaffQuarantineDocsWhenNoIsSelected() {

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData =  CaseData.builder()
            .courtStaffQuarantineDocsList(documentList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .courtStaffUploadDocListDocTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();
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
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .citizenUploadedDocListDocTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();
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
            .citizenUploadQuarantineDocsList(List.of(element(UploadedDocuments.builder().build())))
            .cafcassQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder()
                                                                        .documentUploadedDate(LocalDateTime.now())
                                                                        .document(Document.builder().build())
                                                                        .cafcassQuarantineDocument(Document.builder()
                                                                                                       .documentFileName("filename")
                                                                                                       .build())
                                                                        .build())))
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes).build())
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);
        Assert.assertNotNull(response);
        Assert.assertEquals(DOCUMENT_SUCCESSFULLY_REVIEWED, response.getBody().getConfirmationHeader());
        Assert.assertEquals(REVIEW_YES, response.getBody().getConfirmationBody());
    }

    @Test
    public void testReviewResultWhenNoOptionSelected() {

        CaseData caseData =  CaseData.builder()
            .legalProfQuarantineDocsList(List.of(element))
            .citizenUploadQuarantineDocsList(List.of(element(UploadedDocuments.builder().build())))
            .cafcassQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder()
                                                                        .documentUploadedDate(LocalDateTime.now())
                                                                        .document(Document.builder().build())
                                                                        .cafcassQuarantineDocument(Document.builder()
                                                                                                       .documentFileName("filename")
                                                                                                       .build())
                                                                        .build())))
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no).build())
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);
        Assert.assertNotNull(response);
        Assert.assertEquals(DOCUMENT_SUCCESSFULLY_REVIEWED, response.getBody().getConfirmationHeader());
        Assert.assertEquals(REVIEW_NO, response.getBody().getConfirmationBody());
    }

    @Test
    public void testReviewResultWhenDoNotKnowOptionSelected() {

        CaseData caseData =  CaseData.builder()
            .id(123)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.notSure).build())
            .build();
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);

        Assert.assertNotNull(response);
        Assert.assertEquals(DOCUMENT_IN_REVIEW, response.getBody().getConfirmationHeader());
        Assert.assertEquals(REVIEW_NOT_SURE, response.getBody().getConfirmationBody());
    }

    @Test
    public void testReviewResultWhenCaseTypeApplicationC100() {

        CaseData caseData =  CaseData.builder()
            .id(123)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.notSure).build())
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
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes).build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);
        Assert.assertNotNull(response);
        Assert.assertEquals(DOCUMENT_SUCCESSFULLY_REVIEWED, response.getBody().getConfirmationHeader());
        Assert.assertEquals(REVIEW_YES, response.getBody().getConfirmationBody());
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForScanDocWhenNoIsSelected() {

        List<Element<ScannedDocument>> scannedDocs = new ArrayList<>();
        scannedDocs.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
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

        ));
        CaseData caseData =  CaseData.builder()
            .scannedDocuments(scannedDocs)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(QuarantineLegalDoc.builder()
                            .categoryId(
                                "cafcassQuarantineDocument")
                            .notes("test")
                            .documentUploadedDate(
                                LocalDateTime.now())
                            .document(Document.builder().build())
                            .build());
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseDataMap.get("scannedDocuments"));
        List<Element<ScannedDocument>>  listScannedDocuments =
            (List<Element<ScannedDocument>>) caseDataMap.get("scannedDocuments");

        /*Assert.assertEquals(caseData.getScannedDocuments().get(0).getValue().controlNumber,
                            listScannedDocuments.get(0).getValue().getControlNumber());

        Assert.assertEquals(caseData.getScannedDocuments().get(0).getValue().exceptionRecordReference,
                            listScannedDocuments.get(0).getValue().getExceptionRecordReference());*/

    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForScanDocWhenYesIsSelected() {

        List<Element<ScannedDocument>> scannedDocs = new ArrayList<>();
        scannedDocs.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
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

        ));
        CaseData caseData =  CaseData.builder()
            .scannedDocuments(scannedDocs)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(QuarantineLegalDoc.builder()
                            .categoryId(
                                "cafcassQuarantineDocument")
                            .notes("test")
                            .documentUploadedDate(
                                LocalDateTime.now())
                            .document(Document.builder().build())
                            .build());
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));
        Assert.assertNotNull(caseDataMap.get("scannedDocuments"));
        List<Element<ScannedDocument>>  listScannedDocuments =
            (List<Element<ScannedDocument>>) caseDataMap.get("scannedDocuments");

        /*Assert.assertEquals(caseData.getScannedDocuments().get(0).getValue().controlNumber,
                            listScannedDocuments.get(0).getValue().getControlNumber());

        Assert.assertEquals(caseData.getScannedDocuments().get(0).getValue().exceptionRecordReference,
                            listScannedDocuments.get(0).getValue().getExceptionRecordReference());*/

    }

    @Test
    public void testReviewForLegalProfDocsMoveToConfidentialDocsInConfTab() {
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                 quarantineLegalDoc));

        CaseData caseData =  CaseData.builder()
            .legalProfQuarantineDocsList(quarantineDocsList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseDataMap.get(CONFIDENTIAL_DOCUMENTS));
        List<Element<QuarantineLegalDoc>>  confidentialDocs =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get(CONFIDENTIAL_DOCUMENTS);
        Assert.assertNotNull(confidentialDocs.get(0).getValue().getMiamCertificateDocument());
        Assert.assertEquals(YesOrNo.Yes, confidentialDocs.get(0).getValue().getIsConfidential());
        Assert.assertEquals(YesOrNo.No, confidentialDocs.get(0).getValue().getIsRestricted());
    }

    @Test
    public void testReviewForLegalProfDocsMoveToRestrictedDocsInConfTab() {
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.Yes)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                       quarantineLegalDoc));

        CaseData caseData =  CaseData.builder()
            .legalProfQuarantineDocsList(quarantineDocsList)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseDataMap.get(RESTRICTED_DOCUMENTS));
        List<Element<QuarantineLegalDoc>>  restrictedDocs =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get(RESTRICTED_DOCUMENTS);
        Assert.assertNotNull(restrictedDocs.get(0).getValue().getMiamCertificateDocument());
        Assert.assertEquals(YesOrNo.Yes, restrictedDocs.get(0).getValue().getIsConfidential());
        Assert.assertEquals(YesOrNo.Yes, restrictedDocs.get(0).getValue().getIsRestricted());
    }
}
