package uk.gov.hmcts.reform.prl.services.reviewdocument;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.YesNoNotSure;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.ScannedDocument;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.BulkPrintService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationPostService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.notifications.NotificationService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.ManageDocumentsCategoryConstants.RESPONDENT_C1A_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONFIDENTIAL_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURTNAV;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_PROFESSIONAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESTRICTED_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MULTIPART_FILE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TEST_UUID;
import static uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum.CAFCASS_CYMRU;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"java:S1607"})
public class ReviewDocumentServiceTest {

    public static final String DOCUMENT_SUCCESSFULLY_REVIEWED = "# Document successfully reviewed";
    public static final String DOCUMENT_IN_REVIEW = "# Document review in progress";
    private static final String REVIEW_YES = "### You have successfully reviewed this document"
        + System.lineSeparator()
        + "This document can only be seen by court staff and the judiciary. "
        + "You can view it in case file view and the confidential details tab.";
    private static final String REVIEW_NO = "### You have successfully reviewed this document"
        + System.lineSeparator()
        + " This document is visible to all parties and can be viewed in the case documents tab.";
    private static final String REVIEW_NOT_SURE = "### You need to confirm if the uploaded document needs to be restricted"
        + System.lineSeparator()
        + "If you are not sure, you can use <a href=\"/cases/case-details/123/trigger/sendOrReplyToMessages/sendOrReplyToMessages1\">Send and reply to messages</a> to get further information about whether"
        + " the document needs to be restricted.";

    private final UUID testUuid = UUID.fromString(TEST_UUID);

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

    @Mock
    private ServiceOfApplicationPostService serviceOfApplicationPostService;
    @Mock
    private BulkPrintService bulkPrintService;
    @Mock
    private DocumentGenService documentGenService;
    @Mock
    private DocumentLanguageService documentLanguageService;
    @Mock
    private DgsService dgsService;

    @Mock
    private SendgridService sendgridService;

    @Mock
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    private NotificationService notificationService;

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

    @BeforeEach
    void init() {

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

        byte[] pdf = new byte[]{1, 2, 3, 4, 5};
        MultipartFile file = new InMemoryMultipartFile(SOA_MULTIPART_FILE,
                                                       "Confidential_" + document.getDocumentFileName(),
                                                       APPLICATION_PDF_VALUE, pdf
        );
        uk.gov.hmcts.reform.ccd.document.am.model.Document document = testDocument();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        Mockito.when(caseDocumentClient.uploadDocuments(anyString(), anyString(), anyString(), anyString(), anyList()))
            .thenReturn(uploadResponse);
        Mockito.doNothing().when(caseDocumentClient).deleteDocument(
            anyString(),
            anyString(),
            any(UUID.class),
            anyBoolean()
        );

        ReflectionTestUtils.setField(manageDocumentsService, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(manageDocumentsService, "systemUserService", systemUserService);
        ReflectionTestUtils.setField(manageDocumentsService, "authTokenGenerator", authTokenGenerator);
        ReflectionTestUtils.setField(manageDocumentsService, "caseDocumentClient", caseDocumentClient);
        ReflectionTestUtils.setField(manageDocumentsService, "notificationService", notificationService);


        doCallRealMethod().when(manageDocumentsService).moveDocumentsToRespectiveCategoriesNew(
            any(),
            any(),
            any(),
            any(),
            any()
        );
        doCallRealMethod().when(manageDocumentsService).getRestrictedOrConfidentialKey(any());
        doCallRealMethod().when(manageDocumentsService).getQuarantineDocumentForUploader(any(), any());
        doCallRealMethod().when(manageDocumentsService).moveToConfidentialOrRestricted(any(), any(), any(), any());

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

        when(manageDocumentsService.getQuarantineDocumentForUploader(
            anyString(), any(QuarantineLegalDoc.class))).thenReturn(caseDoc);

        doNothing().when(notificationService).sendNotifications(any(CaseData.class), any(QuarantineLegalDoc.class), anyString());
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
    void testReviewDocumentListIsNotEmptyWhenDocumentArePresentLegalProfQuarantineDocsList() {
        HashMap<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .legalProfQuarantineDocsList(List.of(element(QuarantineLegalDoc.builder().uploaderRole(
                                                   LEGAL_PROFESSIONAL)
                                                                                            .documentUploadedDate(
                                                                                                LocalDateTime.now())
                                                                                            .document(Document.builder().build())
                                                                                            .build())))
                                           .build())

            .citizenUploadedDocumentList(List.of(element(UploadedDocuments.builder().build()))).build();

        assertFalse(reviewDocumentService.fetchDocumentDynamicListElements(caseData, caseDataUpdated).isEmpty());
    }

    @Test
    void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForCafcassQuarantineDocsList() {
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
        assertFalse(reviewDocumentService.fetchDocumentDynamicListElements(caseData, caseDataUpdated).isEmpty());
    }

    @Test
    void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForBulkscanDocuments() {
        HashMap<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseData.builder()
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

        assertFalse(reviewDocumentService.fetchDocumentDynamicListElements(caseData, caseDataUpdated).isEmpty());
    }

    @Test
    void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForCourtStaffQuarantineDocsList() {
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

        assertFalse(reviewDocumentService.fetchDocumentDynamicListElements(caseData, caseDataUpdated).isEmpty());
    }

    @Test
    void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForCitizenQuarantineDocsList() {
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

        assertFalse(reviewDocumentService.fetchDocumentDynamicListElements(caseData, caseDataUpdated).isEmpty());
    }

    @Test
    void testReviewDocumentListIsNotEmptyWhenDocumentsAreNotPresent() {
        HashMap<String, Object> caseDataUpdated = new HashMap<>();
        assertTrue(reviewDocumentService.fetchDocumentDynamicListElements(
            CaseData.builder()
                .documentManagementDetails(DocumentManagementDetails.builder().build()).build(),
            caseDataUpdated
        ).isEmpty());
    }

    @Test
    void testGetDocumentDetailsWhenUploadedByLegalProfessional() {

        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .restrictedDetails("test details")
            .uploaderRole(LEGAL_PROFESSIONAL)
            .build();

        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .tempQuarantineDocumentList(List.of(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                                                quarantineLegalDoc)))
                    .build()
            )
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build()).build()).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetailsNew(caseData, caseDataMap);

        assertNotNull(caseDataMap.get("docToBeReviewed"));
        assertNotNull(caseDataMap.get("reviewDoc"));
        Document reviewDoc = (Document) caseDataMap.get("reviewDoc");
        assertEquals("test.pdf", reviewDoc.getDocumentFileName());

    }

    @Test
    void testGetDocumentDetailsWhenUploadedByCafcassProfessional() {

        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .cafcassQuarantineDocument(document)
            .restrictedDetails("test details")
            .uploaderRole(CAFCASS)
            .build();

        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .tempQuarantineDocumentList(List.of(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                                                quarantineLegalDoc)))
                    .build()
            )
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build()).build()).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetailsNew(caseData, caseDataMap);

        assertNotNull(caseDataMap.get("docToBeReviewed"));
        assertNotNull(caseDataMap.get("reviewDoc"));
        Document reviewDoc = (Document) caseDataMap.get("reviewDoc");
        assertEquals("test.pdf", reviewDoc.getDocumentFileName());
    }

    @Test
    void testGetDocumentDetailsWhenUploadedByCourtStaffProfessional() {
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .courtStaffQuarantineDocument(document)
            .restrictedDetails("test details")
            .uploaderRole(COURT_STAFF)
            .build();

        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .tempQuarantineDocumentList(List.of(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                                                quarantineLegalDoc)))
                    .build()
            )
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build()).build()).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetailsNew(caseData, caseDataMap);
        assertNotNull(caseDataMap.get("docToBeReviewed"));
        assertNotNull(caseDataMap.get("reviewDoc"));
        Document reviewDoc = (Document) caseDataMap.get("reviewDoc");
        assertEquals("test.pdf", reviewDoc.getDocumentFileName());
    }

    @Test
    void testGetDocumentDetailsWhenUploadedByCitizen() {
        Element element = Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(UploadedDocuments.builder()
                       .citizenDocument(Document.builder()
                                            .build())
                       .uploadedBy("Citizen")
                       .partyName("test")
                       .documentType("test").build()).build();
        QuarantineLegalDoc quarantineLegalDoc1 = QuarantineLegalDoc.builder().uploadedBy("Citizen").build();
        Element<QuarantineLegalDoc> quarantineLegalDocElement = Element.<QuarantineLegalDoc>builder()
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
        assertNotNull(caseDataMap.get("docToBeReviewed"));
    }

    @Test
    void testGetDocumentDetailsWhenUploadedByBulkscan() {
        QuarantineLegalDoc quarantineLegalDoc1 = QuarantineLegalDoc.builder().uploaderRole("Legal professional").build();
        Element<QuarantineLegalDoc> quarantineLegalDocElement = Element.<QuarantineLegalDoc>builder()
            .id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(quarantineLegalDoc1).build();

        Element element1 = Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
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
        CaseData caseData = CaseData.builder()
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
        reviewDocumentService.getReviewedDocumentDetailsNew(caseData, caseDataMap);
        assertNotNull(caseDataMap.get("docToBeReviewed"));
    }

    @Disabled
    @Test
    void testReviewProcessOfDocumentToConfidentialTabForCitizenUploadQuarantineDocsWhenNoIsSelected() {
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

        List<Element<UploadedDocuments>> citizenUploadDocListConfTab =
            (List<Element<UploadedDocuments>>) caseDataMap.get("citizenUploadDocListConfTab");

        assertNotNull(caseDataMap.get("citizenUploadDocListConfTab"));
        assertEquals(1, citizenUploadDocListConfTab.size());
        assertEquals(
            document.getDocumentFileName(),
            citizenUploadDocListConfTab.getFirst().getValue().getCitizenDocument().getDocumentFileName()
        );
    }


    @Test
    void testReviewResultWhenYesOptionSelected() {

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
        assertNotNull(response);
        assertEquals(DOCUMENT_SUCCESSFULLY_REVIEWED, response.getBody().getConfirmationHeader());
        assertEquals(REVIEW_YES, response.getBody().getConfirmationBody());
    }

    @Test
    void testReviewResultWhenNoOptionSelected() {

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
        assertNotNull(response);
        assertEquals(DOCUMENT_SUCCESSFULLY_REVIEWED, response.getBody().getConfirmationHeader());
        assertEquals(REVIEW_NO, response.getBody().getConfirmationBody());
    }

    @Test
    void testReviewResultWhenDoNotKnowOptionSelected() {

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
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseDetails,
                                                                                                        caseData,
                                                                                                        null
        );
        when(allTabServiceImpl.getStartUpdateForSpecificEvent(anyString(), anyString()))
            .thenReturn(startAllTabsUpdateDataContent);


        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);

        assertNotNull(response);
        assertEquals(DOCUMENT_IN_REVIEW, response.getBody().getConfirmationHeader());
        assertEquals(REVIEW_NOT_SURE, response.getBody().getConfirmationBody());
    }

    @Test
    void testReviewResultWhenCaseTypeApplicationC100() {

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
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseDetails,
                                                                                                        caseData,
                                                                                                        null
        );
        when(allTabServiceImpl.getStartUpdateForSpecificEvent(anyString(), anyString()))
            .thenReturn(startAllTabsUpdateDataContent);
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);
        assertNotNull(response);
        assertEquals(DOCUMENT_IN_REVIEW, response.getBody().getConfirmationHeader());
        assertEquals(REVIEW_NOT_SURE, response.getBody().getConfirmationBody());
    }


    @Disabled
    @Test
    void testReviewResultWhenAllAreEmpty() {

        CaseData caseData = CaseData.builder()
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
        assertNotNull(response);
        assertEquals(DOCUMENT_IN_REVIEW, response.getBody().getConfirmationHeader());
        assertEquals(REVIEW_NOT_SURE, response.getBody().getConfirmationBody());
    }

    @Test
    void testReviewResultWhenAllQuarantineDocListIsEmpty() {

        CaseData caseData = CaseData.builder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes).build())
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseDetails,
                                                                                                        caseData,
                                                                                                        null
        );
        when(allTabServiceImpl.getStartUpdateForSpecificEvent(anyString(), anyString()))
            .thenReturn(startAllTabsUpdateDataContent);
        ResponseEntity<SubmittedCallbackResponse> response = reviewDocumentService.getReviewResult(caseData);
        assertNotNull(response);
        assertEquals(DOCUMENT_SUCCESSFULLY_REVIEWED, response.getBody().getConfirmationHeader());
        assertEquals(REVIEW_YES, response.getBody().getConfirmationBody());
    }

    @Test
    void testReviewProcessOfDocumentToConfidentialTabForScanDocWhenNoIsSelected() {

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
        CaseData caseData = CaseData.builder()
            .scannedDocuments(scannedDocs)
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(bulkScanQuarantineDoc);
        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        assertNotNull(caseDataMap.get("bulkScannedDocListDocTab"));
        assertTrue(caseData.getScannedDocuments().isEmpty());

        List<Element<QuarantineLegalDoc>> bulkScannedDocListDocTab =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get("bulkScannedDocListDocTab");

        assertNotNull(caseDataMap.get("bulkScannedDocListDocTab"));
        assertEquals(1, bulkScannedDocListDocTab.size());
        assertNotNull(bulkScannedDocListDocTab.getFirst().getValue().getUrl());
        assertNull(bulkScannedDocListDocTab.getFirst().getValue().getCategoryId());
        assertEquals("123", bulkScannedDocListDocTab.getFirst().getValue().getControlNumber());
        assertEquals("EXREF", bulkScannedDocListDocTab.getFirst().getValue().getExceptionRecordReference());
    }

    @Test
    void testReviewProcessOfDocumentToConfidentialTabForScanDocWhenYesIsSelected() {

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
        CaseData caseData = CaseData.builder()
            .scannedDocuments(scannedDocs)
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(bulkScanQuarantineDoc);
        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        assertTrue(caseData.getScannedDocuments().isEmpty());

        assertNotNull(caseDataMap.get(CONFIDENTIAL_DOCUMENTS));
        List<Element<QuarantineLegalDoc>> confidentialDocs =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get(CONFIDENTIAL_DOCUMENTS);
        assertNotNull(confidentialDocs.getFirst().getValue().getUrl());
        assertNull(confidentialDocs.getFirst().getValue().getCategoryId());
        assertEquals("123", confidentialDocs.getFirst().getValue().getControlNumber());
        assertEquals("EXREF", confidentialDocs.getFirst().getValue().getExceptionRecordReference());

    }

    //LegalProfessional
    @Test

    void testReviewForLegalProfDocsMoveToConfidentialDocsInConfTab() {
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
            quarantineLegalDoc
        ));

        CaseData caseData = CaseData.builder()
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

        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );


        assertNotNull(caseDataMap.get(CONFIDENTIAL_DOCUMENTS));
        List<Element<QuarantineLegalDoc>> restrictedDocs =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get(CONFIDENTIAL_DOCUMENTS);
        assertNotNull(restrictedDocs.getFirst().getValue().getMiamCertificateDocument());
        assertEquals(YesOrNo.Yes, restrictedDocs.getFirst().getValue().getIsConfidential());
        assertEquals(YesOrNo.No, restrictedDocs.getFirst().getValue().getIsRestricted());
    }

    @Test
    void testReviewProcessForLegalProfDocsMoveToRestrictedDocsInConfTab() {

        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .categoryId("MIAMCertificate")
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.Yes)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
            quarantineLegalDoc
        ));

        CaseData caseData = CaseData.builder()
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
        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        assertNotNull(caseDataMap.get(RESTRICTED_DOCUMENTS));
        List<Element<QuarantineLegalDoc>> restrictedDocs =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get(RESTRICTED_DOCUMENTS);
        assertNotNull(restrictedDocs.getFirst().getValue().getMiamCertificateDocument());
        assertEquals(YesOrNo.Yes, restrictedDocs.getFirst().getValue().getIsConfidential());
        assertEquals(YesOrNo.Yes, restrictedDocs.getFirst().getValue().getIsRestricted());
    }

    @Test
    void testReviewProcessForLegalProfWhenDecisionNo() {

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
        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());

        List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTab =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get("legalProfUploadDocListDocTab");

        assertNotNull(caseDataMap.get("legalProfUploadDocListDocTab"));
        assertEquals(1, legalProfUploadDocListDocTab.size());
        assertEquals(
            "test.pdf",
            legalProfUploadDocListDocTab.getFirst().getValue().getMiamCertificateDocument().getDocumentFileName()
        );
        assertEquals("MIAMCertificate", legalProfUploadDocListDocTab.getFirst().getValue().getCategoryId());

    }

    @Test
    void testSendEmailProcessForLegalProfRespAppRespC1aWhenDecisionNo() {
        for (String categoryId : List.of("respondentApplication", "respondentC1AResponse", "respondentC1AApplication")) {
            List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
            quarantineLegalDoc = quarantineLegalDoc.toBuilder()
                .categoryId(categoryId)
                .courtStaffQuarantineDocument(document)
                .isConfidential(YesOrNo.Yes)
                .isRestricted(YesOrNo.No)
                .restrictedDetails("test details")
                .solicitorRepresentedPartyName("name")
                .build();
            quarantineDocsList.add(element(
                UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                quarantineLegalDoc
            ));
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

            doNothing().when(emailService).send(anyString(), any(), any(), any());
            when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
                .thenReturn(quarantineCaseDoc);
            reviewDocumentService.processReviewDocument(
                caseDataMap,
                caseData,
                UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
            );

            assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());

            List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTab =
                (List<Element<QuarantineLegalDoc>>) caseDataMap.get("legalProfUploadDocListDocTab");

            assertNotNull(caseDataMap.get("legalProfUploadDocListDocTab"));
            assertEquals(1, legalProfUploadDocListDocTab.size());
            assertEquals(
                "test.pdf",
                legalProfUploadDocListDocTab.getFirst().getValue().getMiamCertificateDocument().getDocumentFileName()
            );
            assertEquals(categoryId, legalProfUploadDocListDocTab.getFirst().getValue().getCategoryId());
        }
    }

    @Test
    void testSendEmailProcessForCitizenRespondentApplicationWhenDecisionNo() {
        for (String categoryId : List.of("respondentApplication", "respondentC1AResponse", "respondentC1AApplication")) {

            List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
            quarantineLegalDoc = quarantineLegalDoc.toBuilder()
                .categoryId(categoryId)
                .courtStaffQuarantineDocument(document)
                .isConfidential(YesOrNo.Yes)
                .isRestricted(YesOrNo.No)
                .restrictedDetails("test details")
                .uploadedBy("name")
                .build();
            quarantineDocsList.add(element(
                UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                quarantineLegalDoc
            ));
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

            doNothing().when(emailService).send(anyString(), any(), any(), any());
            when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
                .thenReturn(quarantineCaseDoc);
            reviewDocumentService.processReviewDocument(
                caseDataMap,
                caseData,
                UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
            );

            assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());

            List<Element<QuarantineLegalDoc>> citizenUploadedDocListDocTab =
                (List<Element<QuarantineLegalDoc>>) caseDataMap.get("citizenUploadedDocListDocTab");

            assertNotNull(caseDataMap.get("citizenUploadedDocListDocTab"));
            assertEquals(1, citizenUploadedDocListDocTab.size());
            assertEquals(
                "test.pdf",
                citizenUploadedDocListDocTab.getFirst().getValue().getMiamCertificateDocument().getDocumentFileName()
            );
            assertEquals(
                categoryId,
                citizenUploadedDocListDocTab.getFirst().getValue().getCategoryId()
            );
        }
    }

    //Court
    @Test
    void testReviewForCourtStaffDocsMoveToConfidentialDocsInConfTab() {
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .courtStaffQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
            quarantineLegalDoc
        ));

        CaseData caseData = CaseData.builder()
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

        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        assertNotNull(caseDataMap.get(CONFIDENTIAL_DOCUMENTS));
        List<Element<QuarantineLegalDoc>> restrictedDocs =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get(CONFIDENTIAL_DOCUMENTS);
        assertNotNull(restrictedDocs.getFirst().getValue().getMiamCertificateDocument());
        assertEquals(YesOrNo.Yes, restrictedDocs.getFirst().getValue().getIsConfidential());
        assertEquals(YesOrNo.No, restrictedDocs.getFirst().getValue().getIsRestricted());

    }

    @Test
    void testReviewProcessForCourtStaffDocsMoveToRestrictedDocsInConfTab() {

        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .courtStaffQuarantineDocument(document)
            .categoryId("MIAMCertificate")
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.Yes)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
            quarantineLegalDoc
        ));

        CaseData caseData = CaseData.builder()
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
        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        assertNotNull(caseDataMap.get(RESTRICTED_DOCUMENTS));
        List<Element<QuarantineLegalDoc>> restrictedDocs =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get(RESTRICTED_DOCUMENTS);
        assertNotNull(restrictedDocs.getFirst().getValue().getMiamCertificateDocument());
        assertEquals(YesOrNo.Yes, restrictedDocs.getFirst().getValue().getIsConfidential());
        assertEquals(YesOrNo.Yes, restrictedDocs.getFirst().getValue().getIsRestricted());
    }

    @Test
    void testReviewProcessForCourtStaffWhenDecisionNo() {

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

        List<Element<QuarantineLegalDoc>> courtStaffUploadDocListDocTab =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get("courtStaffUploadDocListDocTab");

        assertNotNull(caseDataMap.get("courtStaffUploadDocListDocTab"));
        assertEquals(1, courtStaffUploadDocListDocTab.size());
        assertEquals(
            "test.pdf",
            courtStaffUploadDocListDocTab.getFirst().getValue().getMiamCertificateDocument().getDocumentFileName()
        );
        assertEquals("MIAMCertificate", courtStaffUploadDocListDocTab.getFirst().getValue().getCategoryId());
    }

    //Cafcass
    @Test
    void testReviewForCafcassDocsMoveToConfidentialDocsInConfTab() {
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .cafcassQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
            quarantineLegalDoc
        ));

        CaseData caseData = CaseData.builder()
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

        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        assertNotNull(caseDataMap.get(CONFIDENTIAL_DOCUMENTS));
        List<Element<QuarantineLegalDoc>> restrictedDocs =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get(CONFIDENTIAL_DOCUMENTS);
        assertNotNull(restrictedDocs.getFirst().getValue().getMiamCertificateDocument());
        assertEquals(YesOrNo.Yes, restrictedDocs.getFirst().getValue().getIsConfidential());
        assertEquals(YesOrNo.No, restrictedDocs.getFirst().getValue().getIsRestricted());

    }

    @Test
    void testReviewProcessForCafcassDocsMoveToRestrictedDocsInConfTab() {

        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .cafcassQuarantineDocument(document)
            .categoryId("MIAMCertificate")
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.Yes)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
            quarantineLegalDoc
        ));

        CaseData caseData = CaseData.builder()
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
        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        assertNotNull(caseDataMap.get(RESTRICTED_DOCUMENTS));
        List<Element<QuarantineLegalDoc>> restrictedDocs =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get(RESTRICTED_DOCUMENTS);
        assertNotNull(restrictedDocs.getFirst().getValue().getMiamCertificateDocument());
        assertEquals(YesOrNo.Yes, restrictedDocs.getFirst().getValue().getIsConfidential());
        assertEquals(YesOrNo.Yes, restrictedDocs.getFirst().getValue().getIsRestricted());
    }

    @Test
    void testReviewProcessForCafcassWhenDecisionNo() {

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

        List<Element<QuarantineLegalDoc>> cafcassUploadDocListDocTab =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get("cafcassUploadDocListDocTab");

        assertNotNull(caseDataMap.get("cafcassUploadDocListDocTab"));
        assertEquals(1, cafcassUploadDocListDocTab.size());
        assertEquals(
            "test.pdf",
            cafcassUploadDocListDocTab.getFirst().getValue().getMiamCertificateDocument().getDocumentFileName()
        );
        assertEquals("MIAMCertificate", cafcassUploadDocListDocTab.getFirst().getValue().getCategoryId());
    }

    @Test
    void testReviewDocumentListIsNotEmptyWhenDocumentArePresentForCourtNavQuarantineDocsList() {
        HashMap<String, Object> caseDataUpdated = new HashMap<>();
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .courtNavQuarantineDocumentList(List.of(element(QuarantineLegalDoc.builder().uploaderRole(COURTNAV)
                                                                        .uploadedBy(COURTNAV)
                                                                        .documentUploadedDate(LocalDateTime.now())
                                                                        .document(Document.builder().build())
                                                                        .courtNavQuarantineDocument(Document.builder()
                                                                                                        .documentFileName(
                                                                                                            "filename")
                                                                                                        .build())
                                                                        .build())))
                    .build()
            )
            .courtNavUploadedDocs(List.of(element(UploadedDocuments.builder().build()))).build();

        assertFalse(reviewDocumentService.fetchDocumentDynamicListElements(caseData, caseDataUpdated).isEmpty());
    }

    @Test
    void testGetDocumentDetailsWhenUploadedByCourtNav() {
        element = Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(UploadedDocuments.builder()
                       .citizenDocument(Document.builder()
                                            .build())
                       .uploadedBy(COURTNAV)
                       .partyName("test")
                       .documentType("test").build()).build();
        QuarantineLegalDoc quarantineLegalDoc1 = QuarantineLegalDoc.builder()
            .uploadedBy(COURTNAV)
            .uploaderRole(COURTNAV).build();
        Element<QuarantineLegalDoc> quarantineLegalDocElement = Element.<QuarantineLegalDoc>builder()
            .id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(quarantineLegalDoc1).build();
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .courtNavQuarantineDocumentList(List.of(element))
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
        assertNotNull(caseDataMap.get("docToBeReviewed"));
    }

    @Test
    void testReviewForCourtNavDocsMoveToConfidentialDocsInConfTab() {
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .courtNavQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
            quarantineLegalDoc
        ));
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .courtNavQuarantineDocumentList(quarantineDocsList)
                                           .build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineConfidentialDoc);

        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        assertNotNull(caseDataMap.get(CONFIDENTIAL_DOCUMENTS));
        List<Element<QuarantineLegalDoc>> restrictedDocs =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get(CONFIDENTIAL_DOCUMENTS);
        assertNotNull(restrictedDocs.getFirst().getValue().getMiamCertificateDocument());
        assertEquals(YesOrNo.Yes, restrictedDocs.getFirst().getValue().getIsConfidential());
        assertEquals(YesOrNo.No, restrictedDocs.getFirst().getValue().getIsRestricted());
    }

    @Test
    void testSendResponsePostSubmissionWhenC1AResponseSubmittedWhenDecisionNo() {
        PartyDetails applicant = PartyDetails.builder().partyId(testUuid).build();
        PartyDetails applicant2 = PartyDetails.builder()
            .partyId(UUID.fromString("00000000-0000-0000-0000-000000000001")).build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedApplicant2 = Element.<PartyDetails>builder().value(applicant2).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        applicantList.add(wrappedApplicant2);
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId("respondentC1AResponse")
            .citizenQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .uploadedBy("name")
            .uploaderRole(CITIZEN)
            .build();
        quarantineDocsList.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
            quarantineLegalDoc
        ));

        doNothing().when(emailService).send(anyString(), any(), any(), any());
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineCaseDoc);
        when(documentLanguageService.docGenerateLang(any())).thenReturn(DocumentLanguage.builder()
                                                                            .isGenWelsh(true)
                                                                            .isGenEng(true)
                                                                            .build());
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .citizenQuarantineDocsList(quarantineDocsList)
                    .build()
            )
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicantList)
            .serviceOfApplication(ServiceOfApplication.builder().soaCafcassCymruEmail("testEmail@mail.com").build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .legalProfUploadDocListDocTab(new ArrayList<>()).build()).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());

        List<Element<QuarantineLegalDoc>> citizenUploadedDocListDocTab =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get("citizenUploadedDocListDocTab");

        assertNotNull(caseDataMap.get("citizenUploadedDocListDocTab"));
        assertEquals(1, citizenUploadedDocListDocTab.size());
        assertEquals(
            "test.pdf",
            citizenUploadedDocListDocTab.getFirst().getValue().getMiamCertificateDocument().getDocumentFileName()
        );
        assertEquals("respondentC1AResponse", citizenUploadedDocListDocTab.getFirst().getValue().getCategoryId());

    }

    @Test
    void testSendResponsePostSubmissionWhenC7ResponseSubmittedWhenDecisionNo() {
        PartyDetails applicant = PartyDetails.builder().partyId(testUuid).build();
        PartyDetails applicant2 = PartyDetails.builder()
            .partyId(UUID.fromString("00000000-0000-0000-0000-000000000001")).build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedApplicant2 = Element.<PartyDetails>builder().value(applicant2).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        applicantList.add(wrappedApplicant2);
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId(RESPONDENT_APPLICATION)
            .citizenQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .uploadedBy("name")
            .uploaderRole(CITIZEN)
            .build();
        quarantineDocsList.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
            quarantineLegalDoc
        ));

        doNothing().when(emailService).send(anyString(), any(), any(), any());
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineCaseDoc);
        when(documentLanguageService.docGenerateLang(any())).thenReturn(DocumentLanguage.builder()
                                                                            .isGenWelsh(true)
                                                                            .isGenEng(true)
                                                                            .build());
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .citizenQuarantineDocsList(quarantineDocsList)
                    .build()
            )
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicantList)
            .serviceOfApplication(ServiceOfApplication.builder().soaCafcassCymruEmail("testEmail@mail.com").build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .legalProfUploadDocListDocTab(new ArrayList<>()).build()).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());

        List<Element<QuarantineLegalDoc>> citizenUploadedDocListDocTab =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get("citizenUploadedDocListDocTab");

        assertNotNull(caseDataMap.get("citizenUploadedDocListDocTab"));
        assertEquals(1, citizenUploadedDocListDocTab.size());
        assertEquals(
            "test.pdf",
            citizenUploadedDocListDocTab.getFirst().getValue().getMiamCertificateDocument().getDocumentFileName()
        );
        assertEquals(RESPONDENT_APPLICATION, citizenUploadedDocListDocTab.getFirst().getValue().getCategoryId());

    }

    @Test
    void testReviewForCitizenDocsMoveToConfidentialDocsInConfTab() {
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .citizenQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .build();
        quarantineDocsList.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
            quarantineLegalDoc
        ));
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .citizenQuarantineDocsList(quarantineDocsList)
                                           .build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.yes)
                                 .build())
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineConfidentialDoc);

        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        assertNotNull(caseDataMap.get(CONFIDENTIAL_DOCUMENTS));
        List<Element<QuarantineLegalDoc>> restrictedDocs =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get(CONFIDENTIAL_DOCUMENTS);
        assertNotNull(restrictedDocs.getFirst().getValue().getMiamCertificateDocument());
        assertEquals(YesOrNo.Yes, restrictedDocs.getFirst().getValue().getIsConfidential());
        assertEquals(YesOrNo.No, restrictedDocs.getFirst().getValue().getIsRestricted());

    }

    @Test
    void testSendResponsePostSubmissionWhenRespondentC1ApplicationWithDecisionNo() throws Exception {
        PartyDetails applicant = PartyDetails.builder().partyId(testUuid)
            .contactPreferences(ContactPreferences.post)
            .address(Address.builder()
                         .addressLine1("test address")
                         .build())
            .build();
        PartyDetails applicant2 = PartyDetails.builder()
            .partyId(UUID.fromString("00000000-0000-0000-0000-000000000001")).build();
        PartyDetails applicant3 = PartyDetails.builder()
            .partyId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
            .contactPreferences(ContactPreferences.email)
            .build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        Element<PartyDetails> wrappedApplicant2 = Element.<PartyDetails>builder().value(applicant2).build();
        Element<PartyDetails> wrappedApplicant3 = Element.<PartyDetails>builder().value(applicant3).build();
        List<Element<PartyDetails>> applicantList = new ArrayList<>();
        applicantList.add(wrappedApplicant);
        applicantList.add(wrappedApplicant2);
        applicantList.add(wrappedApplicant3);
        List<Element<QuarantineLegalDoc>> quarantineDocsList = new ArrayList<>();
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .categoryId(RESPONDENT_C1A_APPLICATION)
            .citizenQuarantineDocument(document)
            .isConfidential(YesOrNo.Yes)
            .isRestricted(YesOrNo.No)
            .restrictedDetails("test details")
            .uploadedBy("name")
            .uploaderRole(CITIZEN)
            .build();
        quarantineDocsList.add(element(
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
            quarantineLegalDoc
        ));

        doNothing().when(emailService).send(anyString(), any(), any(), any());
        when(objectMapper.convertValue((Object) any(), (Class<Object>) any()))
            .thenReturn(quarantineCaseDoc);
        List<Document> coverLetterDocs = new ArrayList<>();
        coverLetterDocs.add(Document.builder().build());
        when(serviceOfApplicationPostService.getCoverSheets(any(), any(), any(), any(), any())).thenReturn(
            coverLetterDocs);
        when(documentLanguageService.docGenerateLang(any())).thenReturn(DocumentLanguage.builder()
                                                                            .isGenWelsh(true)
                                                                            .isGenEng(true)
                                                                            .build());
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .citizenQuarantineDocsList(quarantineDocsList)
                    .build()
            )
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicantList)
            .serviceOfApplication(ServiceOfApplication.builder().soaCafcassCymruEmail("testEmail@mail.com").build())
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDecisionYesOrNo(YesNoNotSure.no)
                                 .legalProfUploadDocListDocTab(new ArrayList<>()).build()).build();
        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.processReviewDocument(
            caseDataMap,
            caseData,
            UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355")
        );

        assertNotNull(caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());

        List<Element<QuarantineLegalDoc>> citizenUploadedDocListDocTab =
            (List<Element<QuarantineLegalDoc>>) caseDataMap.get("citizenUploadedDocListDocTab");

        assertNotNull(caseDataMap.get("citizenUploadedDocListDocTab"));
        assertEquals(1, citizenUploadedDocListDocTab.size());
        assertEquals(
            "test.pdf",
            citizenUploadedDocListDocTab.getFirst().getValue().getMiamCertificateDocument().getDocumentFileName()
        );
        assertEquals(RESPONDENT_C1A_APPLICATION, citizenUploadedDocListDocTab.getFirst().getValue().getCategoryId());

    }

    @Test
    void testGetDocumentDetailsWhenUploadedByCourtNavAndUploaderRoleNull() {
        element = Element.builder().id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(UploadedDocuments.builder()
                       .citizenDocument(Document.builder()
                                            .build())
                       .uploadedBy(COURTNAV)
                       .partyName("test")
                       .documentType("test").build()).build();
        QuarantineLegalDoc quarantineLegalDoc1 = QuarantineLegalDoc.builder()
            .uploadedBy(COURTNAV).build();
        Element<QuarantineLegalDoc> quarantineLegalDocElement = Element.<QuarantineLegalDoc>builder()
            .id(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
            .value(quarantineLegalDoc1).build();
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .courtNavQuarantineDocumentList(List.of(element))
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
        assertNotNull(caseDataMap.get("docToBeReviewed"));
    }

    @Test
    void testGetDocumentDetailsWhenUploadedByCafcassCymruAndUploderRolenull() {

        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .cafcassQuarantineDocument(document)
            .restrictedDetails("test details")
            .documentParty(CAFCASS_CYMRU.getDisplayedValue())
            .build();

        CaseData caseData = CaseData.builder()
            .documentManagementDetails(
                DocumentManagementDetails.builder()
                    .tempQuarantineDocumentList(List.of(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                                                quarantineLegalDoc)))
                    .build()
            )
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDocsDynamicList(DynamicList.builder().value(
                                     DynamicListElement.builder()
                                         .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                 ).build()).build()).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        reviewDocumentService.getReviewedDocumentDetailsNew(caseData, caseDataMap);

        assertNotNull(caseDataMap.get("docToBeReviewed"));
        assertNotNull(caseDataMap.get("reviewDoc"));
        Document reviewDoc = (Document) caseDataMap.get("reviewDoc");
        assertEquals("test.pdf", reviewDoc.getDocumentFileName());
    }
}
