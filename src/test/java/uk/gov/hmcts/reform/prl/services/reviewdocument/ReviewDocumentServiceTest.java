package uk.gov.hmcts.reform.prl.services.reviewdocument;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabsService;

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
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONFIDENTIAL_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_PROFESSIONAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESTRICTED_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MULTIPART_FILE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
@SuppressWarnings({"java:S1607"})
public class ReviewDocumentServiceTest {

    public static final String DOCUMENT_SUCCESSFULLY_REVIEWED = "# Document successfully reviewed";
    public static final String DOCUMENT_IN_REVIEW = "# Document review in progress";
    private static final String REVIEW_YES = "### You have successfully reviewed this document"
        + System.lineSeparator()
        + "This document can only be seen by court staff and the judiciary. "
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
    AuthTokenGenerator authTokenGenerator;
    @Mock
    SystemUserService systemUserService;
    @Mock
    CaseDocumentClient caseDocumentClient;

    @Mock
    AllTabsService allTabsService;

    @Mock
    AllTabServiceImpl allTabServiceImpl;

    @Mock
    ManageDocumentsService manageDocumentsService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    private EmailService emailService;

    private final String authorization = "authToken";
    Element element;
    Document document;
    QuarantineLegalDoc quarantineLegalDoc;
    private String authToken;
    private String s2sToken;

    Document caseDoc;

    Document confidentialDoc;

    QuarantineLegalDoc quarantineConfidentialDoc;

    QuarantineLegalDoc quarantineCaseDoc;

    QuarantineLegalDoc bulkScanQuarantineDoc;

    @Before
    public void init() {

        objectMapper.registerModule(new JavaTimeModule());


        element = Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(QuarantineLegalDoc.builder()
                       .categoryId("MIAMCertificate")
                       .notes("test")
                       .isConfidential(YesOrNo.Yes)
                       .isRestricted(YesOrNo.No)
                       .restrictedDetails("test")
                       .uploaderRole("Legal professional")
                       .uploadedBy("Legal professional")
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
        when(caseDocumentClient
                 .getDocumentBinary(Mockito.anyString(), Mockito.anyString(), Mockito.any(UUID.class)))
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

        ReflectionTestUtils.setField(manageDocumentsService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(manageDocumentsService, "systemUserService", systemUserService);
        ReflectionTestUtils.setField(manageDocumentsService, "authTokenGenerator", authTokenGenerator);
        ReflectionTestUtils.setField(manageDocumentsService, "caseDocumentClient", caseDocumentClient);


        doCallRealMethod().when(manageDocumentsService).moveDocumentsToRespectiveCategoriesNew(any(), any(), any(), any(), any());
        doCallRealMethod().when(manageDocumentsService).getRestrictedOrConfidentialKey(any());
        doCallRealMethod().when(manageDocumentsService).getQuarantineDocumentForUploader(any(),any());
        doCallRealMethod().when(manageDocumentsService).moveToConfidentialOrRestricted(any(),any(),any(),any());

        confidentialDoc = Document.builder()
            .documentFileName("Confidential_test.pdf")
            .documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15")
            .build();

        caseDoc = Document.builder()
            .documentFileName("test.pdf")
            .documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15")
            .build();

        quarantineConfidentialDoc = QuarantineLegalDoc.builder()
            .documentParty(DocumentPartyEnum.APPLICANT.getDisplayedValue())
            .categoryId("MIAMCertificate")
            .miamCertificateDocument(confidentialDoc)
            .documentUploadedDate(LocalDateTime.now())
            .build();

        quarantineCaseDoc = QuarantineLegalDoc.builder()
            .documentParty(DocumentPartyEnum.APPLICANT.getDisplayedValue())
            .categoryId("MIAMCertificate")
            .miamCertificateDocument(caseDoc)
            .documentUploadedDate(LocalDateTime.now())
            .build();

        bulkScanQuarantineDoc = QuarantineLegalDoc.builder()
            .url(caseDoc)
            .documentUploadedDate(LocalDateTime.now())
            .controlNumber("123")
            .deliveryDate(LocalDateTime.now())
            .exceptionRecordReference("EXREF")
            .type("Other")
            .subtype("test")
            .build();

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
        HashMap<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .legalProfQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder().uploaderRole(LEGAL_PROFESSIONAL)
                                                                                            .documentUploadedDate(
                                                                                                LocalDateTime.now())
                                                                                            .document(Document.builder().build())
                                                                                            .build())))
                                           .build())

            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();

        Assert.assertTrue(!reviewDocumentService.fetchDocumentDynamicListElements(caseData, caseDataUpdated).isEmpty());
    }

    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForCafcassQuarantineDocsList() {
        HashMap<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .cafcassQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder().uploaderRole(CAFCASS)
                                                                   .documentUploadedDate(LocalDateTime.now())
                                                                   .document(Document.builder().build())
                                                                   .cafcassQuarantineDocument(Document.builder()
                                                                                                  .documentFileName(
                                                                                                      "filename")
                                                                                                  .build())
                                                                   .build())))
                    .build()
            )
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineConfidentialDoc);
        Assert.assertTrue(!reviewDocumentService.fetchDocumentDynamicListElements(caseData, caseDataUpdated).isEmpty());
    }

    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForBulkscanDocuments() {
        HashMap<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData =  CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder().build())
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

        Assert.assertTrue(!reviewDocumentService.fetchDocumentDynamicListElements(caseData, caseDataUpdated).isEmpty());
    }

    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForCourtStaffQuarantineDocsList() {
        HashMap<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .courtStaffQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder().uploaderRole(COURT_STAFF)
                                                                      .documentUploadedDate(LocalDateTime.now())
                                                                      .document(Document.builder().build())
                                                                      .courtStaffQuarantineDocument(Document.builder()
                                                                                                        .documentFileName(
                                                                                                            "filename")
                                                                                                        .build())
                                                                      .build())))
                    .build()
            )
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();

        Assert.assertTrue(!reviewDocumentService.fetchDocumentDynamicListElements(caseData, caseDataUpdated).isEmpty());
    }

    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForCitizenQuarantineDocsList() {
        HashMap<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .citizenQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder().uploaderRole(CITIZEN)
                                                                      .documentUploadedDate(LocalDateTime.now())
                                                                      .document(Document.builder().build())
                                                                      .citizenQuarantineDocument(Document.builder()
                                                                                                        .documentFileName(
                                                                                                            "filename")
                                                                                                        .build())
                                                                      .build())))
                    .build()
            )
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();

        Assert.assertTrue(!reviewDocumentService.fetchDocumentDynamicListElements(caseData, caseDataUpdated).isEmpty());
    }

    @Test
    public void testReviewDocumentListIsNotEmptyWhenDocumentsAreNotPresent() {
        HashMap<String, Object> caseDataUpdated = new HashMap<>();
        Assert.assertTrue(reviewDocumentService.fetchDocumentDynamicListElements(CaseData.builder()
                                                                    .documentManagementDetails(DocumentManagementDetails.builder().build()).build(),
                                                                                 caseDataUpdated).isEmpty());
    }

    @Test
    public void testGetDocumentDetailsWhenUploadedByLegalProfessional() {

        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .restrictedDetails("test details")
            .uploaderRole(LEGAL_PROFESSIONAL)
            .build();

        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .tempQuarantineDocumentList(List.of(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),quarantineLegalDoc)))
                    .build()
            )
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build()).build()).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetailsNew(caseData, caseDataMap);

        Assert.assertNotNull(caseDataMap.get("docToBeReviewed"));
        Assert.assertNotNull(caseDataMap.get("reviewDoc"));
        Document reviewDoc = (Document)caseDataMap.get("reviewDoc");
        Assert.assertEquals("test.pdf", reviewDoc.getDocumentFileName());

    }

    @Test
    public void testGetDocumentDetailsWhenUploadedByCafcassProfessional() {

        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .cafcassQuarantineDocument(document)
            .restrictedDetails("test details")
            .uploaderRole(CAFCASS)
            .build();

        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .tempQuarantineDocumentList(List.of(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),quarantineLegalDoc)))
                    .build()
            )
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build()).build()).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetailsNew(caseData, caseDataMap);

        Assert.assertNotNull(caseDataMap.get("docToBeReviewed"));
        Assert.assertNotNull(caseDataMap.get("reviewDoc"));
        Document reviewDoc = (Document)caseDataMap.get("reviewDoc");
        Assert.assertEquals("test.pdf", reviewDoc.getDocumentFileName());
    }

    @Test
    public void testGetDocumentDetailsWhenUploadedByCourtStaffProfessional() {
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .courtStaffQuarantineDocument(document)
            .restrictedDetails("test details")
            .uploaderRole(COURT_STAFF)
            .build();

        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .tempQuarantineDocumentList(List.of(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),quarantineLegalDoc)))
                    .build()
            )
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build()).build()).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetailsNew(caseData, caseDataMap);
        Assert.assertNotNull(caseDataMap.get("docToBeReviewed"));
        Assert.assertNotNull(caseDataMap.get("reviewDoc"));
        Document reviewDoc = (Document)caseDataMap.get("reviewDoc");
        Assert.assertEquals("test.pdf", reviewDoc.getDocumentFileName());
    }

    @Test
    public void testGetDocumentDetailsWhenUploadedByCitizen() {
        Element element = Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(UploadedDocuments.builder()
                       .citizenDocument(Document.builder()
                                            .build())
                       .uploadedBy("Legal professional")
                       .partyName("test")
                       .documentType("test").build()).build();
        QuarantineLegalDoc quarantineLegalDoc1 = QuarantineLegalDoc.builder().uploaderRole("Legal professional").build();
        Element<QuarantineLegalDoc> quarantineLegalDocElement =  Element.<QuarantineLegalDoc>builder()
            .id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(quarantineLegalDoc1).build();
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .citizenUploadQuarantineDocsList(List.of(element))
                    .tempQuarantineDocumentList(List.of(quarantineLegalDocElement))
                    .build()
            )
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build())
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes).build())
            .build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetailsNew(caseData, caseDataMap);
        Assert.assertNotNull(caseDataMap.get("docToBeReviewed"));
    }

    @Test
    public void testGetDocumentDetailsWhenUploadedByBulkscan() {
        QuarantineLegalDoc quarantineLegalDoc1 = QuarantineLegalDoc.builder().uploaderRole("Legal professional").build();
        Element<QuarantineLegalDoc> quarantineLegalDocElement =  Element.<QuarantineLegalDoc>builder()
            .id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(quarantineLegalDoc1).build();

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
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .tempQuarantineDocumentList(List.of(quarantineLegalDocElement)).build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build())
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes).build()).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetailsNew(caseData,caseDataMap);
        Assert.assertNotNull(caseDataMap.get("docToBeReviewed"));
    }

    @Ignore
    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForCitizenUploadQuarantineDocsWhenNoIsSelected() {
        Element element = Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(UploadedDocuments.builder().dateCreated(LocalDate.now())
                       .citizenDocument(document)
                       .isApplicant("yes")
                       .build()).build();
        List<Element<UploadedDocuments>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .citizenUploadQuarantineDocsList(documentList)
                                           .build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .citizenUploadedDocListDocTab(new ArrayList<>()).build())
            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        List<Element<UploadedDocuments>>  citizenUploadDocListConfTab =
            (List<Element<UploadedDocuments>>)caseDataMap.get("citizenUploadDocListConfTab");

        Assert.assertNotNull(caseDataMap.get("citizenUploadDocListConfTab"));
        Assert.assertEquals(1, citizenUploadDocListConfTab.size());
        Assert.assertEquals(document.getDocumentFileName(), citizenUploadDocListConfTab.get(0).getValue().getCitizenDocument().getDocumentFileName());
    }


    @Test
    public void testReviewResultWhenYesOptionSelected() {

        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .legalProfQuarantineDocsList(List.of(element))
                    .citizenUploadQuarantineDocsList(List.of(element(UploadedDocuments.builder().build())))
                    .cafcassQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder()
                                                                   .documentUploadedDate(LocalDateTime.now())
                                                                   .document(Document.builder().build())
                                                                   .cafcassQuarantineDocument(Document.builder()
                                                                                                  .documentFileName(
                                                                                                      "filename")
                                                                                                  .build())
                                                                   .build())))
                    .build()
            )
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

        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .legalProfQuarantineDocsList(List.of(element))
                    .citizenUploadQuarantineDocsList(List.of(element(UploadedDocuments.builder().build())))
                    .cafcassQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder()
                                                                   .documentUploadedDate(
                                                                       LocalDateTime.now())
                                                                   .document(Document.builder().build())
                                                                   .cafcassQuarantineDocument(
                                                                       Document.builder()
                                                                           .documentFileName(
                                                                               "filename")
                                                                           .build())
                                                                   .build())))
                    .build())
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

        CaseData caseData = CaseData.builder()
            .id(123)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.notSure).build())
            .build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
             EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDetails, caseData, null);
        when(allTabServiceImpl.getStartUpdateForSpecificEvent(anyString(), anyString()))
            .thenReturn(startAllTabsUpdateDataContent);


        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);

        Assert.assertNotNull(response);
        Assert.assertEquals(DOCUMENT_IN_REVIEW, response.getBody().getConfirmationHeader());
        Assert.assertEquals(REVIEW_NOT_SURE, response.getBody().getConfirmationBody());
    }

    @Test
    public void testReviewResultWhenCaseTypeApplicationC100() {

        CaseData caseData = CaseData.builder()
            .id(123)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.notSure).build())
            .build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
                      EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDetails, caseData, null);
        when(allTabServiceImpl.getStartUpdateForSpecificEvent(anyString(), anyString()))
            .thenReturn(startAllTabsUpdateDataContent);
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);
        Assert.assertNotNull(response);
        Assert.assertEquals(DOCUMENT_IN_REVIEW, response.getBody().getConfirmationHeader());
        Assert.assertEquals(REVIEW_NOT_SURE, response.getBody().getConfirmationBody());
    }


    @Ignore
    @Test
    public void testReviewResultWhenAllAreEmpty() {

        CaseData caseData =  CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.notSure).build())
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .legalProfQuarantineDocsList(new ArrayList<>())
                                           .citizenUploadQuarantineDocsList(new ArrayList<>())
                                           .cafcassQuarantineDocsList(new ArrayList<>())
                                           .citizenQuarantineDocsList(new ArrayList<>())
                                           .build()).build();
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);
        Assert.assertNotNull(response);
        Assert.assertEquals(DOCUMENT_IN_REVIEW, response.getBody().getConfirmationHeader());
        Assert.assertEquals(REVIEW_NOT_SURE, response.getBody().getConfirmationBody());
    }

    @Test
    public void testReviewResultWhenAllQuarantineDocListIsEmpty() {

        CaseData caseData = CaseData.builder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes).build())
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDetails, caseData, null);
        when(allTabServiceImpl.getStartUpdateForSpecificEvent(anyString(), anyString()))
            .thenReturn(startAllTabsUpdateDataContent);
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
                .url(document)
                .controlNumber("123")
                .deliveryDate(LocalDateTime.now())
                .exceptionRecordReference("EXREF")
                .type("Other")
                .subtype("test")
                .fileName("filename")
                .build()

        ));
        authToken = "auth-token";
        s2sToken = "s2sToken";
        Mockito.when(systemUserService.getSysUserToken()).thenReturn(authToken);
        Mockito.when(authTokenGenerator.generate()).thenReturn(s2sToken);
        CaseData caseData =  CaseData.builder()
            .scannedDocuments(scannedDocs)
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(bulkScanQuarantineDoc);
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseDataMap.get("bulkScannedDocListDocTab"));
        Assert.assertTrue(caseData.getScannedDocuments().isEmpty());

        List<Element<QuarantineLegalDoc>>  bulkScannedDocListDocTab =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get("bulkScannedDocListDocTab");

        Assert.assertNotNull(caseDataMap.get("bulkScannedDocListDocTab"));
        Assert.assertEquals(1, bulkScannedDocListDocTab.size());
        Assert.assertNotNull(bulkScannedDocListDocTab.get(0).getValue().getUrl());
        Assert.assertNull(bulkScannedDocListDocTab.get(0).getValue().getCategoryId());
        Assert.assertEquals("123", bulkScannedDocListDocTab.get(0).getValue().getControlNumber());
        Assert.assertEquals("EXREF", bulkScannedDocListDocTab.get(0).getValue().getExceptionRecordReference());
    }

    @Test
    public void testReviewProcessOfDocumentToConfidentialTabForScanDocWhenYesIsSelected() {

        List<Element<ScannedDocument>> scannedDocs = new ArrayList<>();
        scannedDocs.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
            ScannedDocument.builder()
                .scannedDate(LocalDateTime.now())
                .url(document)
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
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(bulkScanQuarantineDoc);
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertTrue(caseData.getScannedDocuments().isEmpty());

        Assert.assertNotNull(caseDataMap.get(CONFIDENTIAL_DOCUMENTS));
        List<Element<QuarantineLegalDoc>>  confidentialDocs =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get(CONFIDENTIAL_DOCUMENTS);
        Assert.assertNotNull(confidentialDocs.get(0).getValue().getUrl());
        Assert.assertNull(confidentialDocs.get(0).getValue().getCategoryId());
        Assert.assertEquals("123", confidentialDocs.get(0).getValue().getControlNumber());
        Assert.assertEquals("EXREF", confidentialDocs.get(0).getValue().getExceptionRecordReference());

    }

    //LegalProfessional
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
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .legalProfQuarantineDocsList(quarantineDocsList)
                                           .build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineConfidentialDoc);

        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));


        Assert.assertNotNull(caseDataMap.get(CONFIDENTIAL_DOCUMENTS));
        List<Element<QuarantineLegalDoc>>  restrictedDocs =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get(CONFIDENTIAL_DOCUMENTS);
        Assert.assertNotNull(restrictedDocs.get(0).getValue().getMiamCertificateDocument());
        Assert.assertEquals(YesOrNo.Yes, restrictedDocs.get(0).getValue().getIsConfidential());
        Assert.assertEquals(YesOrNo.No, restrictedDocs.get(0).getValue().getIsRestricted());
    }

    @Test
    public void testReviewProcessForLegalProfDocsMoveToRestrictedDocsInConfTab() {

        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .categoryId("MIAMCertificate")
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.Yes)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                       quarantineLegalDoc));

        CaseData caseData =  CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .legalProfQuarantineDocsList(quarantineDocsList)
                                           .build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineConfidentialDoc);
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseDataMap.get(RESTRICTED_DOCUMENTS));
        List<Element<QuarantineLegalDoc>>  restrictedDocs =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get(RESTRICTED_DOCUMENTS);
        Assert.assertNotNull(restrictedDocs.get(0).getValue().getMiamCertificateDocument());
        Assert.assertEquals(YesOrNo.Yes, restrictedDocs.get(0).getValue().getIsConfidential());
        Assert.assertEquals(YesOrNo.Yes, restrictedDocs.get(0).getValue().getIsRestricted());
    }

    @Test
    public void testReviewProcessForLegalProfWhenDecisionNo() {

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .legalProfQuarantineDocsList(documentList)
                    .build()
            )
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .legalProfUploadDocListDocTab(new ArrayList<>()).build()).build();
        Map<String, Object> caseDataMap = new HashMap<>();

        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineCaseDoc);
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());

        List<Element<QuarantineLegalDoc>>  legalProfUploadDocListDocTab =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get("legalProfUploadDocListDocTab");

        Assert.assertNotNull(caseDataMap.get("legalProfUploadDocListDocTab"));
        Assert.assertEquals(1, legalProfUploadDocListDocTab.size());
        Assert.assertEquals("test.pdf", legalProfUploadDocListDocTab.get(0).getValue().getMiamCertificateDocument().getDocumentFileName());
        Assert.assertEquals("MIAMCertificate", legalProfUploadDocListDocTab.get(0).getValue().getCategoryId());

    }

    @Test
    public void testSendEmailProcessForLegalProfRespondentApplicationWhenDecisionNo() {
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId("respondentApplication")
            .courtStaffQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .solicitorRepresentedPartyName("name")
            .build();
        quarantineDocsList.add(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                       quarantineLegalDoc));
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .legalProfQuarantineDocsList(quarantineDocsList)
                    .build()
            )
            .serviceOfApplication(ServiceOfApplication.builder().soaCafcassCymruEmail("testEmail@mail.com").build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .legalProfUploadDocListDocTab(new ArrayList<>()).build()).build();
        Map<String, Object> caseDataMap = new HashMap<>();

        doNothing().when(emailService).send(anyString(),any(),any(),any());
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineCaseDoc);
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());

        List<Element<QuarantineLegalDoc>>  legalProfUploadDocListDocTab =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get("legalProfUploadDocListDocTab");

        Assert.assertNotNull(caseDataMap.get("legalProfUploadDocListDocTab"));
        Assert.assertEquals(1, legalProfUploadDocListDocTab.size());
        Assert.assertEquals("test.pdf", legalProfUploadDocListDocTab.get(0).getValue().getMiamCertificateDocument().getDocumentFileName());
        Assert.assertEquals("respondentApplication", legalProfUploadDocListDocTab.get(0).getValue().getCategoryId());

    }

    @Test
    public void testSendEmailProcessForLegalRespondentC1AResponseWhenDecisionNo() {
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId("respondentC1AResponse")
            .courtStaffQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .solicitorRepresentedPartyName("name")
            .build();
        quarantineDocsList.add(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                       quarantineLegalDoc));
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .legalProfQuarantineDocsList(quarantineDocsList)
                    .build()
            )
            .serviceOfApplication(ServiceOfApplication.builder().soaCafcassCymruEmail("testEmail@mail.com").build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .legalProfUploadDocListDocTab(new ArrayList<>()).build()).build();
        Map<String, Object> caseDataMap = new HashMap<>();

        doNothing().when(emailService).send(anyString(),any(),any(),any());
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineCaseDoc);
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());

        List<Element<QuarantineLegalDoc>>  legalProfUploadDocListDocTab =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get("legalProfUploadDocListDocTab");

        Assert.assertNotNull(caseDataMap.get("legalProfUploadDocListDocTab"));
        Assert.assertEquals(1, legalProfUploadDocListDocTab.size());
        Assert.assertEquals("test.pdf", legalProfUploadDocListDocTab.get(0).getValue().getMiamCertificateDocument().getDocumentFileName());
        Assert.assertEquals("respondentC1AResponse", legalProfUploadDocListDocTab.get(0).getValue().getCategoryId());

    }

    @Test
    public void testSendEmailProcessForLegalRespondentC1AApplicationWhenDecisionNo() {
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId("respondentC1AApplication")
            .courtStaffQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .solicitorRepresentedPartyName("name")
            .build();
        quarantineDocsList.add(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                       quarantineLegalDoc));
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .legalProfQuarantineDocsList(quarantineDocsList)
                    .build()
            )
            .serviceOfApplication(ServiceOfApplication.builder().soaCafcassCymruEmail("testEmail@mail.com").build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .legalProfUploadDocListDocTab(new ArrayList<>()).build()).build();
        Map<String, Object> caseDataMap = new HashMap<>();

        doNothing().when(emailService).send(anyString(),any(),any(),any());
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineCaseDoc);
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());

        List<Element<QuarantineLegalDoc>>  legalProfUploadDocListDocTab =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get("legalProfUploadDocListDocTab");

        Assert.assertNotNull(caseDataMap.get("legalProfUploadDocListDocTab"));
        Assert.assertEquals(1, legalProfUploadDocListDocTab.size());
        Assert.assertEquals("test.pdf", legalProfUploadDocListDocTab.get(0).getValue().getMiamCertificateDocument().getDocumentFileName());
        Assert.assertEquals("respondentC1AApplication", legalProfUploadDocListDocTab.get(0).getValue().getCategoryId());

    }

    @Test
    public void testSendEmailProcessForCitizenRespondentApplicationWhenDecisionNo() {
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId("respondentApplication")
            .courtStaffQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .uploadedBy("name")
            .build();
        quarantineDocsList.add(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                       quarantineLegalDoc));
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .citizenQuarantineDocsList(quarantineDocsList)
                    .build()
            )
            .serviceOfApplication(ServiceOfApplication.builder().soaCafcassCymruEmail("testEmail@mail.com").build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .legalProfUploadDocListDocTab(new ArrayList<>()).build()).build();
        Map<String, Object> caseDataMap = new HashMap<>();

        doNothing().when(emailService).send(anyString(),any(),any(),any());
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineCaseDoc);
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());

        List<Element<QuarantineLegalDoc>>  citizenUploadedDocListDocTab =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get("citizenUploadedDocListDocTab");

        Assert.assertNotNull(caseDataMap.get("citizenUploadedDocListDocTab"));
        Assert.assertEquals(1, citizenUploadedDocListDocTab.size());
        Assert.assertEquals("test.pdf", citizenUploadedDocListDocTab.get(0).getValue().getMiamCertificateDocument().getDocumentFileName());
        Assert.assertEquals("respondentApplication", citizenUploadedDocListDocTab.get(0).getValue().getCategoryId());

    }

    @Test
    public void testSendEmailProcessForCitizenRespondentC1AApplicationWhenDecisionNo() {
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId("respondentC1AApplication")
            .courtStaffQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .uploadedBy("name")
            .build();
        quarantineDocsList.add(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                       quarantineLegalDoc));
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .citizenQuarantineDocsList(quarantineDocsList)
                    .build()
            )
            .serviceOfApplication(ServiceOfApplication.builder().soaCafcassCymruEmail("testEmail@mail.com").build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .legalProfUploadDocListDocTab(new ArrayList<>()).build()).build();
        Map<String, Object> caseDataMap = new HashMap<>();

        doNothing().when(emailService).send(anyString(),any(),any(),any());
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineCaseDoc);
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());

        List<Element<QuarantineLegalDoc>>  citizenUploadedDocListDocTab =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get("citizenUploadedDocListDocTab");

        Assert.assertNotNull(caseDataMap.get("citizenUploadedDocListDocTab"));
        Assert.assertEquals(1, citizenUploadedDocListDocTab.size());
        Assert.assertEquals("test.pdf", citizenUploadedDocListDocTab.get(0).getValue().getMiamCertificateDocument().getDocumentFileName());
        Assert.assertEquals("respondentC1AApplication", citizenUploadedDocListDocTab.get(0).getValue().getCategoryId());

    }

    @Test
    public void testSendEmailProcessForCitizenRespondentC1AResponseApplicationWhenDecisionNo() {
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId("respondentC1AResponse")
            .courtStaffQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .uploadedBy("name")
            .build();
        quarantineDocsList.add(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                       quarantineLegalDoc));
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .citizenQuarantineDocsList(quarantineDocsList)
                    .build()
            )
            .serviceOfApplication(ServiceOfApplication.builder().soaCafcassCymruEmail("testEmail@mail.com").build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .legalProfUploadDocListDocTab(new ArrayList<>()).build()).build();
        Map<String, Object> caseDataMap = new HashMap<>();

        doNothing().when(emailService).send(anyString(),any(),any(),any());
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineCaseDoc);
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());

        List<Element<QuarantineLegalDoc>>  citizenUploadedDocListDocTab =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get("citizenUploadedDocListDocTab");

        Assert.assertNotNull(caseDataMap.get("citizenUploadedDocListDocTab"));
        Assert.assertEquals(1, citizenUploadedDocListDocTab.size());
        Assert.assertEquals("test.pdf", citizenUploadedDocListDocTab.get(0).getValue().getMiamCertificateDocument().getDocumentFileName());
        Assert.assertEquals("respondentC1AResponse", citizenUploadedDocListDocTab.get(0).getValue().getCategoryId());

    }


    //Court
    @Test
    public void testReviewForCourtStaffDocsMoveToConfidentialDocsInConfTab() {
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
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .courtStaffQuarantineDocsList(quarantineDocsList)
                                           .build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineConfidentialDoc);

        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseDataMap.get(CONFIDENTIAL_DOCUMENTS));
        List<Element<QuarantineLegalDoc>>  restrictedDocs =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get(CONFIDENTIAL_DOCUMENTS);
        Assert.assertNotNull(restrictedDocs.get(0).getValue().getMiamCertificateDocument());
        Assert.assertEquals(YesOrNo.Yes, restrictedDocs.get(0).getValue().getIsConfidential());
        Assert.assertEquals(YesOrNo.No, restrictedDocs.get(0).getValue().getIsRestricted());

    }

    @Test
    public void testReviewProcessForCourtStaffDocsMoveToRestrictedDocsInConfTab() {

        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .courtStaffQuarantineDocument(document)
            .categoryId("MIAMCertificate")
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.Yes)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                       quarantineLegalDoc));

        CaseData caseData =  CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .courtStaffQuarantineDocsList(quarantineDocsList)
                                           .build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineConfidentialDoc);
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseDataMap.get(RESTRICTED_DOCUMENTS));
        List<Element<QuarantineLegalDoc>>  restrictedDocs =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get(RESTRICTED_DOCUMENTS);
        Assert.assertNotNull(restrictedDocs.get(0).getValue().getMiamCertificateDocument());
        Assert.assertEquals(YesOrNo.Yes, restrictedDocs.get(0).getValue().getIsConfidential());
        Assert.assertEquals(YesOrNo.Yes, restrictedDocs.get(0).getValue().getIsRestricted());
    }

    @Test
    public void testReviewProcessForCourtStaffWhenDecisionNo() {

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .courtStaffQuarantineDocsList(documentList)
                                           .build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .courtStaffUploadDocListDocTab(new ArrayList<>()).build()).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineCaseDoc);
        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        List<Element<QuarantineLegalDoc>>  courtStaffUploadDocListDocTab =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get("courtStaffUploadDocListDocTab");

        Assert.assertNotNull(caseDataMap.get("courtStaffUploadDocListDocTab"));
        Assert.assertEquals(1, courtStaffUploadDocListDocTab.size());
        Assert.assertEquals("test.pdf", courtStaffUploadDocListDocTab.get(0).getValue().getMiamCertificateDocument().getDocumentFileName());
        Assert.assertEquals("MIAMCertificate", courtStaffUploadDocListDocTab.get(0).getValue().getCategoryId());
    }

    //Cafcass
    @Test
    public void testReviewForCafcassDocsMoveToConfidentialDocsInConfTab() {
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
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .cafcassQuarantineDocsList(quarantineDocsList)
                                           .build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineConfidentialDoc);

        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseDataMap.get(CONFIDENTIAL_DOCUMENTS));
        List<Element<QuarantineLegalDoc>>  restrictedDocs =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get(CONFIDENTIAL_DOCUMENTS);
        Assert.assertNotNull(restrictedDocs.get(0).getValue().getMiamCertificateDocument());
        Assert.assertEquals(YesOrNo.Yes, restrictedDocs.get(0).getValue().getIsConfidential());
        Assert.assertEquals(YesOrNo.No, restrictedDocs.get(0).getValue().getIsRestricted());

    }

    @Test
    public void testReviewProcessForCafcassDocsMoveToRestrictedDocsInConfTab() {

        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .cafcassQuarantineDocument(document)
            .categoryId("MIAMCertificate")
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.Yes)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                       quarantineLegalDoc));

        CaseData caseData =  CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .cafcassQuarantineDocsList(quarantineDocsList)
                                           .build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineConfidentialDoc);
        reviewDocumentService.processReviewDocument(caseDataMap, caseData, UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));

        Assert.assertNotNull(caseDataMap.get(RESTRICTED_DOCUMENTS));
        List<Element<QuarantineLegalDoc>>  restrictedDocs =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get(RESTRICTED_DOCUMENTS);
        Assert.assertNotNull(restrictedDocs.get(0).getValue().getMiamCertificateDocument());
        Assert.assertEquals(YesOrNo.Yes, restrictedDocs.get(0).getValue().getIsConfidential());
        Assert.assertEquals(YesOrNo.Yes, restrictedDocs.get(0).getValue().getIsRestricted());
    }

    @Test
    public void testReviewProcessForCafcassWhenDecisionNo() {

        List<Element<QuarantineLegalDoc>> documentList = new ArrayList<>();
        documentList.add(element);
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .cafcassQuarantineDocsList(documentList)
                    .build()
            )
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .cafcassUploadDocListDocTab(new ArrayList<>()).build()).build();
        Map<String, Object> caseDataMap = new HashMap<>();

        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineCaseDoc);
        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        List<Element<QuarantineLegalDoc>>  cafcassUploadDocListDocTab =
            (List<Element<QuarantineLegalDoc>>)caseDataMap.get("cafcassUploadDocListDocTab");

        Assert.assertNotNull(caseDataMap.get("cafcassUploadDocListDocTab"));
        Assert.assertEquals(1, cafcassUploadDocListDocTab.size());
        Assert.assertEquals("test.pdf", cafcassUploadDocListDocTab.get(0).getValue().getMiamCertificateDocument().getDocumentFileName());
        Assert.assertEquals("MIAMCertificate", cafcassUploadDocListDocTab.get(0).getValue().getCategoryId());
    }

}
