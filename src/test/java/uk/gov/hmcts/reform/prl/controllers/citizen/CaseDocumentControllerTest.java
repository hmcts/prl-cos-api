package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.framework.exceptions.DocumentGenerationException;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.documents.DocumentResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DeleteDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.UploadedDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.notify.UploadDocumentEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.UploadDocumentService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_UPLOADED_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUCCESS;


@RunWith(MockitoJUnitRunner.class)
public class CaseDocumentControllerTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "TestS2sToken";
    @InjectMocks
    private CaseDocumentController caseDocumentController;

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private DocumentGenService documentGenService;
    @Mock
    private UploadDocumentService uploadService;
    @Mock
    private IdamClient idamClient;
    private GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest;

    @Mock
    private EmailService emailService;

    @Mock
    CaseService caseService;

    @Mock
    private AuthorisationService authorisationService;

    @Before
    public void setUp() {

        generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest.builder()
            .values(Map.of("fileName", "test.docx"))
            .build();
    }

    @Test
    public void testNotifyOtherPartiesRespondedentCA() throws Exception {
        User user = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fb").build();
        PartyDetails applicant = PartyDetails.builder().user(user).email("test@hmcts.net").firstName("test").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        User user1 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fa").build();
        PartyDetails respondent = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondent);

        CaseData casedata = CaseData.builder().id(123456).caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).applicants(
            applicantList).respondents(respondentList).build();


        Method method = CaseDocumentController.class.getDeclaredMethod(
            "notifyOtherParties",
            String.class,
            CaseData.class
        );
        method.setAccessible(true);
        method.invoke(caseDocumentController, "577346ec-5c58-491d-938a-112c4bff06fb", casedata);
        UploadDocumentEmail emailTemplateVars = UploadDocumentEmail.builder().caseReference("123456").name("test").build();
        verify(emailService).send(
            "test@hmcts.net",
            EmailTemplateNames.DOCUMENT_UPLOADED,
            emailTemplateVars,
            LanguagePreference.english
        );
    }

    @Test
    public void testNotifyOtherPartiesRespondedentsCA() throws Exception {
        User user = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fb").build();
        PartyDetails applicant = PartyDetails.builder().user(user).email("test@hmcts.net").firstName("test").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        User user1 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fa").build();
        PartyDetails respondent = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        User user2 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fc").build();
        PartyDetails respondent1 = PartyDetails.builder().user(user2).email("test1@hmcts.net").firstName("test").build();
        Element<PartyDetails> wrappedRespondent1 = Element.<PartyDetails>builder().value(respondent1).build();
        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondent);
        respondentList.add(wrappedRespondent1);

        CaseData casedata = CaseData.builder().id(123456).caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).applicants(
            applicantList).respondents(respondentList).build();


        Method method = CaseDocumentController.class.getDeclaredMethod(
            "notifyOtherParties",
            String.class,
            CaseData.class
        );
        method.setAccessible(true);
        method.invoke(caseDocumentController, "577346ec-5c58-491d-938a-112c4bff06fb", casedata);
        UploadDocumentEmail emailTemplateVars = UploadDocumentEmail.builder().caseReference("123456").name("test").build();
        verify(emailService).send(
            "test@hmcts.net",
            EmailTemplateNames.DOCUMENT_UPLOADED,
            emailTemplateVars,
            LanguagePreference.english
        );
        verify(emailService).send(
            "test1@hmcts.net",
            EmailTemplateNames.DOCUMENT_UPLOADED,
            emailTemplateVars,
            LanguagePreference.english
        );
    }

    @Test
    public void testNotifyOtherPartiesApplicantCA() throws Exception {
        User user = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fb").build();
        PartyDetails applicant = PartyDetails.builder().user(user).email("test@hmcts.net").firstName("test").build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        User user1 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fa").build();
        PartyDetails respondent = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondent);

        CaseData casedata = CaseData.builder().id(123456).caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).applicants(
            applicantList).respondents(respondentList).build();


        Method method = CaseDocumentController.class.getDeclaredMethod(
            "notifyOtherParties",
            String.class,
            CaseData.class
        );
        method.setAccessible(true);
        method.invoke(caseDocumentController, "577346ec-5c58-491d-938a-112c4bff06fa", casedata);
        UploadDocumentEmail emailTemplateVars = UploadDocumentEmail.builder().caseReference("123456").name("test").build();
        verify(emailService).send(
            "test@hmcts.net",
            EmailTemplateNames.DOCUMENT_UPLOADED,
            emailTemplateVars,
            LanguagePreference.english
        );
    }

    @Test
    public void testNotifyOtherPartiesApplicantDA() throws Exception {
        User user = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fb").build();
        PartyDetails applicant = PartyDetails.builder().user(user).email("test@hmcts.net").firstName("test").build();

        User user1 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fa").build();
        PartyDetails respondent = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();

        CaseData casedata = CaseData.builder().id(123456).caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE).applicantsFL401(
            applicant).respondentsFL401(respondent).build();


        Method method = CaseDocumentController.class.getDeclaredMethod(
            "notifyOtherParties",
            String.class,
            CaseData.class
        );
        method.setAccessible(true);
        method.invoke(caseDocumentController, "577346ec-5c58-491d-938a-112c4bff06fa", casedata);
        UploadDocumentEmail emailTemplateVars = UploadDocumentEmail.builder().caseReference("123456").name("test").build();
        verify(emailService).send(
            "test@hmcts.net",
            EmailTemplateNames.DOCUMENT_UPLOADED,
            emailTemplateVars,
            LanguagePreference.english
        );
    }

    @Test
    public void testNotifyOtherPartiesRespondentDA() throws Exception {
        User user = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fb").build();
        PartyDetails applicant = PartyDetails.builder().user(user).email("test@hmcts.net").firstName("test").build();

        User user1 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fa").build();
        PartyDetails respondent = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();

        CaseData casedata = CaseData.builder().id(123456).caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE).applicantsFL401(
            applicant).respondentsFL401(respondent).build();


        Method method = CaseDocumentController.class.getDeclaredMethod(
            "notifyOtherParties",
            String.class,
            CaseData.class
        );
        method.setAccessible(true);
        method.invoke(caseDocumentController, "577346ec-5c58-491d-938a-112c4bff06fb", casedata);
        UploadDocumentEmail emailTemplateVars = UploadDocumentEmail.builder().caseReference("123456").name("test").build();
        verify(emailService).send(
            "test@hmcts.net",
            EmailTemplateNames.DOCUMENT_UPLOADED,
            emailTemplateVars,
            LanguagePreference.english
        );
    }

    @Test
    public void testDocumentUpload() throws IOException {
        //Given
        MultipartFile mockFile = mock(MultipartFile.class);
        Document mockDocument = Document.builder().build();
        DocumentResponse documentResponse = DocumentResponse
                .builder()
                .status("SUCCESS")
                .document(mockDocument)
                .build();

        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);
        when(documentGenService.uploadDocument(authToken, mockFile)).thenReturn(documentResponse);

        //When
        ResponseEntity<?> response = caseDocumentController
                .uploadCitizenDocument(authToken, s2sToken, mockFile);
        //Then
        assertEquals(documentResponse, response.getBody());
    }

    @Test
    public void testGenerateCitizenStatementDocumentt() throws Exception {
        HashMap<String,String> map = new HashMap<>();
        map.put("caseId","1656350492135029");
        map.put("state","AWAITING_SUBMISSION_TO_HMCTS");
        map.put("documentType","test");
        map.put("partyName","test");
        map.put("partyId","test");
        Document document = Document.builder().documentUrl("")
            .documentFileName("test")
            .build();
        UploadedDocuments uploadedDocuments = UploadedDocuments.builder()
            .documentType("test")
            .partyName("test")
            .documentRequestedByCourt(YesOrNo.Yes)
            .parentDocumentType("Parent")
            .citizenDocument(document)
            .build();
        Element<UploadedDocuments> uploadedDocumentsElement1 = Element.<UploadedDocuments>builder().value(
            uploadedDocuments).build();
        List<Element<UploadedDocuments>> listOfUploadedDocuments = new ArrayList<>(List.of(
            uploadedDocumentsElement1
        ));
        GenerateAndUploadDocumentRequest generateAndUploadDocumentRequest = GenerateAndUploadDocumentRequest
            .builder().values(map).build();
        User user = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fb").build();
        PartyDetails applicant = PartyDetails.builder().user(user).email("test@hmcts.net").firstName("test").build();
        User user1 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fa").build();
        PartyDetails respondent = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        CaseData casedata = CaseData.builder().id(165635049).caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE).applicantsFL401(
                applicant).respondentsFL401(respondent).state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .citizenUploadedDocumentList(listOfUploadedDocuments)
            .build();
        Map<String, Object> stringObjectMap = casedata.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(
            Long.parseLong("1656350492135029")).state("AWAITING_SUBMISSION_TO_HMCTS").data(stringObjectMap).build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        when(coreCaseDataApi.getCase(authToken, s2sToken, "1656350492135029")).thenReturn(
            caseDetails);
        when(documentGenService.generateCitizenStatementDocument(authToken, generateAndUploadDocumentRequest, 2)).thenReturn(uploadedDocuments);
        //When
        assertNotNull(caseDocumentController.generateCitizenStatementDocument(generateAndUploadDocumentRequest,authToken,s2sToken));
    }

    @Test
    public void testUploadCitizenStatementDocument() {
        HashMap<String,String> map = new HashMap<>();
        map.put("caseId","1656350492135029");
        map.put("state","AWAITING_SUBMISSION_TO_HMCTS");
        map.put("documentType","test");
        map.put("partyName","test");
        map.put("partyId","test");
        Document document = Document.builder().documentUrl("")
            .documentFileName("test")
            .build();
        UploadedDocumentRequest uploadedDocumentRequest = UploadedDocumentRequest.builder()
            .caseId("1656350492135029")
            .documentType("test")
            .partyId("test")
            .partyName("test")
            .documentRequestedByCourt(YesOrNo.Yes)
            .parentDocumentType("Parent")
            .build();
        UploadedDocuments uploadedDocuments = UploadedDocuments.builder()
            .documentType("test")
            .partyName("test")
            .documentRequestedByCourt(YesOrNo.Yes)
            .parentDocumentType("Parent")
            .citizenDocument(document)
            .build();


        Element<UploadedDocuments> uploadedDocumentsElement1 = Element.<UploadedDocuments>builder().value(
            uploadedDocuments).build();
        List<Element<UploadedDocuments>> listOfUploadedDocuments = new ArrayList<>(List.of(
            uploadedDocumentsElement1));
        User user = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fb").build();
        PartyDetails applicant = PartyDetails.builder().user(user).email("test@hmcts.net").firstName("test").build();
        User user1 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fa").build();
        PartyDetails respondent = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        CaseData casedata = CaseData.builder().id(165635049).caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE).applicantsFL401(
                applicant).respondentsFL401(respondent).state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .citizenUploadedDocumentList(listOfUploadedDocuments)
            .build();
        Map<String, Object> stringObjectMap = casedata.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            Long.parseLong("1656350492135029")).state("AWAITING_SUBMISSION_TO_HMCTS").data(stringObjectMap).build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        when(coreCaseDataApi.getCase(authToken, s2sToken, "1656350492135029")).thenReturn(
            caseDetails);
        when(uploadService.uploadCitizenDocument(
            authToken,
            uploadedDocumentRequest
        )).thenReturn(uploadedDocuments);
        StartEventResponse startEventResponse = StartEventResponse.builder().eventId("")
            .token("")
            .caseDetails(caseDetails)
            .build();
        UserInfo userInfo = UserInfo.builder()
            .uid("123456")
            .build();
        when(idamClient.getUserInfo(authToken)).thenReturn(userInfo);
        when(coreCaseDataApi.startEventForCitizen(authToken, s2sToken, "123456", JURISDICTION, CASE_TYPE, "1656350492135029",
                                                                                CITIZEN_UPLOADED_DOCUMENT)).thenReturn(startEventResponse);
        ResponseEntity responseEntity = caseDocumentController.uploadCitizenStatementDocument(authToken,s2sToken,uploadedDocumentRequest);
        assertNotNull(responseEntity);
    }

    @Test
    public void testDeleteCitizenStatementDocument() throws Exception {
        HashMap<String,String> map = new HashMap<>();
        map.put("caseId","1656350492135029");
        map.put("state","AWAITING_SUBMISSION_TO_HMCTS");
        map.put("documentType","test");
        map.put("partyName","test");
        map.put("partyId","test");
        map.put("documentId","123455");
        map.put("id","123455");
        Document document = Document.builder().documentUrl("")
            .documentFileName("test")
            .build();
        DeleteDocumentRequest deleteDocumentRequest = DeleteDocumentRequest.builder()
            .values(map).build();
        UploadedDocuments uploadedDocuments = UploadedDocuments.builder()
            .documentType("test")
            .partyName("test")
            .documentRequestedByCourt(YesOrNo.Yes)
            .parentDocumentType("Parent")
            .citizenDocument(document)
            .build();


        Element<UploadedDocuments> uploadedDocumentsElement1 = Element.<UploadedDocuments>builder().id(UUID.randomUUID()).value(
            uploadedDocuments).build();
        List<Element<UploadedDocuments>> listOfUploadedDocuments = List.of(
            uploadedDocumentsElement1
        );

        User user = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fb").build();
        PartyDetails applicant = PartyDetails.builder().user(user).email("test@hmcts.net").firstName("test").build();
        User user1 = User.builder().idamId("577346ec-5c58-491d-938a-112c4bff06fa").build();
        PartyDetails respondent = PartyDetails.builder().user(user1).email("test@hmcts.net").firstName("test").build();
        CaseData casedata = CaseData.builder().id(165635049).caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE).applicantsFL401(
                applicant).respondentsFL401(respondent).state(State.AWAITING_SUBMISSION_TO_HMCTS).citizenUploadedDocumentList(listOfUploadedDocuments)
               .build();
        Map<String, Object> stringObjectMap = casedata.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(
            Long.parseLong("1656350492135029")).state("AWAITING_SUBMISSION_TO_HMCTS").data(stringObjectMap).build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        when(coreCaseDataApi.getCase(authToken, s2sToken, "1656350492135029")).thenReturn(
            caseDetails);
        String deleteStatus = caseDocumentController.deleteCitizenStatementDocument(deleteDocumentRequest,authToken,s2sToken);
        assertNotNull(deleteStatus);
    }

    @Test (expected = RuntimeException.class)
    public void testDocumentUploadNotAuthorised() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);

        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.FALSE);

        caseDocumentController
            .uploadCitizenDocument(authToken, s2sToken, mockFile);
    }

    @Test
    public void testDeleteDocument() {
        //Given
        DocumentResponse documentResponse = DocumentResponse
                .builder()
                .status("SUCCESS")
                .build();

        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);
        when(documentGenService.deleteDocument(authToken, "TEST_DOCUMENT_ID")).thenReturn(documentResponse);

        //When
        ResponseEntity<?> response = caseDocumentController
                .deleteDocument(authToken, s2sToken, "TEST_DOCUMENT_ID");
        //Then
        assertEquals(documentResponse, response.getBody());
    }

    @Test (expected = RuntimeException.class)
    public void testDeleteDocumentNotAuthorised() {
        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.FALSE);

        caseDocumentController
            .deleteDocument(authToken, s2sToken, "TEST_DOCUMENT_ID");
    }

    @Test
    public void testDownloadDocument() throws Exception {
        //Given
        Resource expectedResource = new ClassPathResource("documents/document.pdf");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, OK);

        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);
        when(documentGenService.downloadDocument(authToken, "TEST_DOCUMENT_ID")).thenReturn(expectedResponse);

        //When
        ResponseEntity<?> response = caseDocumentController
            .downloadDocument(authToken, s2sToken, "TEST_DOCUMENT_ID");
        //Then
        assertEquals(OK, response.getStatusCode());
    }

    @Test (expected = RuntimeException.class)
    public void testDownloadDocumentNotAuthorised() throws Exception {
        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.FALSE);

        caseDocumentController
            .downloadDocument(authToken, s2sToken, "TEST_DOCUMENT_ID");
    }

    @Test (expected = RuntimeException.class)
    public void testGenerateDocumentThrowInvalidClientException() {
        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.FALSE);

        caseDocumentController.citizenGenerateDocument(authToken, s2sToken, DocumentRequest.builder().build());
    }

    @Test
    public void testGenerateDocumentThrowDocumentGenerationException() {
        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);
        when(caseDocumentController.citizenGenerateDocument(authToken, s2sToken, DocumentRequest.builder().build()))
            .thenThrow(DocumentGenerationException.class);

        ResponseEntity<Object> responseEntity = caseDocumentController.citizenGenerateDocument(authToken, s2sToken,
                                                                                               DocumentRequest.builder().build());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Error in generating a document", responseEntity.getBody());
    }

    @Test
    public void testGenerateDocument() throws DocumentGenerationException {
        //Given
        DocumentRequest documentRequest = DocumentRequest.builder()
            .caseId("123")
            .categoryId("positionStatements")
            .partyName("appf appl")
            .partyType("applicant")
            .restrictDocumentDetails("test details")
            .freeTextStatements("free text to generate document")
            .build();

        DocumentResponse mockDocumentResponse = DocumentResponse.builder()
            .status(SUCCESS)
            .document(Document.builder().build()).build();

        //When
        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);
        when(documentGenService.generateAndUploadDocument(authToken, documentRequest)).thenReturn(mockDocumentResponse);

        //Action
        ResponseEntity<?> response = caseDocumentController.citizenGenerateDocument(authToken, s2sToken, documentRequest);

        //Then
        assertEquals(OK, response.getStatusCode());
        assertEquals(DocumentResponse.class, response.getBody().getClass());
        assertNotNull(response.getBody());
    }

    @Test (expected = RuntimeException.class)
    public void testSubmitCitizenDocumentsThrowInvalidClientException() {
        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.FALSE);

        caseDocumentController.citizenSubmitDocuments(authToken, s2sToken, DocumentRequest.builder().build());
    }

    @Test
    public void testUploadAndMoveDocumentsToQuarantine() {
        //Given
        DocumentRequest documentRequest = DocumentRequest.builder()
            .caseId("123")
            .documents(Collections.singletonList(Document.builder().build()))
            .build();

        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();

        //When
        when(authorisationService.authoriseUser(authToken)).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(s2sToken)).thenReturn(Boolean.TRUE);
        when(documentGenService.citizenSubmitDocuments(authToken, documentRequest)).thenReturn(caseDetails);

        //Action
        ResponseEntity<?> response = caseDocumentController.citizenSubmitDocuments(authToken, s2sToken, documentRequest);

        //Then
        assertEquals(OK, response.getStatusCode());
        assertNotNull(response);
        assertEquals(SUCCESS, response.getBody());
    }
}
