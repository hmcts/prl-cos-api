package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.clients.ccd.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.documents.DocumentResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.citizen.DeleteDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.GenerateAndUploadDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.citizen.UploadedDocumentRequest;
import uk.gov.hmcts.reform.prl.models.dto.notify.UploadDocumentEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UploadDocumentService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.any;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;



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
    @Mock
    SystemUserService systemUserService;
    @Mock
    CoreCaseDataService coreCaseDataService;
    private String bearerToken;

    private final String userToken = "Bearer testToken";

    private final String systemUpdateUserId = "systemUserID";

    private EventRequestData eventRequestData;

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
    public void testUploadCitizenStatementDocument() throws Exception {
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
        CaseData caseData = CaseData.builder().id(Long.parseLong("1656350492135029")).caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantsFL401(applicant).respondentsFL401(respondent).state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .citizenUploadedDocumentList(listOfUploadedDocuments)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(Long.parseLong("1656350492135029"))
            .data(stringObjectMap)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(uploadService.uploadCitizenDocument(
            authToken,
            uploadedDocumentRequest
        )).thenReturn(uploadedDocuments);

        UserInfo userInfo = UserInfo.builder()
            .uid("123456")
            .build();

        //when(idamClient.getUserInfo(authToken)).thenReturn(userInfo);
        eventRequestData = EventRequestData.builder()
            .eventId(CaseEvent.CITIZEN_UPLOADED_DOCUMENT.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUpdateUserId)
            .userToken(userToken)
            .build();

        when(systemUserService.getSysUserToken()).thenReturn(userToken);
        when(systemUserService.getUserId(userToken)).thenReturn(systemUpdateUserId);
        when(coreCaseDataService.eventRequest(CaseEvent.CITIZEN_UPLOADED_DOCUMENT, systemUpdateUserId)).thenReturn(eventRequestData);

        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .token(bearerToken).build();
        when(coreCaseDataService.startUpdate(
            userToken,eventRequestData, String.valueOf(caseData.getId()),false))
            .thenReturn(startEventResponse);
        CaseData caseDataUpdated = CaseUtils.getCaseDataFromStartUpdateEventResponse(startEventResponse, objectMapper);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .data(stringObjectMap)
            .build();
        Mockito.lenient().when(coreCaseDataService.createCaseDataContent(startEventResponse, any(CaseData.class))).thenReturn(caseDataContent);
        Mockito.lenient().when(coreCaseDataService.submitUpdate(userToken, eventRequestData, caseDataContent,String.valueOf(caseData.getId()), true))
            .thenReturn(caseDetails);

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
}
