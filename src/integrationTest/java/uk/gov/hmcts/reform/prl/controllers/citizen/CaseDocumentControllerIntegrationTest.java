package uk.gov.hmcts.reform.prl.controllers.citizen;


import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.documents.DocumentResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.UploadDocumentService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenDocumentService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseDocumentControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    DocumentGenService documentGenService;

    @MockBean
    UploadDocumentService uploadService;

    @MockBean
    AuthorisationService authorisationService;

    @MockBean
    CoreCaseDataApi coreCaseDataApi;
    @MockBean
    IdamClient idamClient;

    @MockBean
    CaseService caseService;

    @MockBean
    EmailService emailService;

    @MockBean
    CitizenDocumentService citizenDocumentService;


    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testDeleteCitizenStatementDocument() throws Exception {


        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        Map<String, Object> map = new HashMap<>();
        map.put("caseId", "123");
        map.put("citizenUploadedDocumentList", List.of(element(UploadedDocuments.builder().build())));


        when(coreCaseDataApi.getCase(anyString(), anyString(), anyString())).thenReturn(CaseDetails.builder()
                                                                                            .id(123L)
                                                                                            .data(map)
                                                                                            .build());

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        String url = "/delete-citizen-statement-document";
        String jsonRequest = "{\"values\": {\"caseId\": \"123\", \"documentId\": \"456\"}}";

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testGenerateCitizenStatementDocument() throws Exception {

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        Map<String, Object> map = new HashMap<>();
        map.put("caseId", "123");
        map.put("citizenUploadedDocumentList", List.of(element(UploadedDocuments.builder().build())));
        map.put("partyId", "7663081e-778d-4317-b278-7642b740d317");

        String caseDetails = ResourceLoader.loadJson("requests/c100-respondent-solicitor-c1adraft-generate.json");

        CallbackRequest callbackRequest = CcdObjectMapper.getObjectMapper().readValue(
            caseDetails,
            CallbackRequest.class
        );

        when(coreCaseDataApi.getCase(
            anyString(),
            anyString(),
            anyString()
        )).thenReturn(callbackRequest.getCaseDetails());

        when(documentGenService.generateCitizenStatementDocument(anyString(), any(), anyInt()))
            .thenReturn(UploadedDocuments.builder()
                            .citizenDocument(Document.builder()
                                                 .documentFileName("test.pdf")
                                                 .build())
                            .build());

        String url = "/generate-citizen-statement-document";
        String jsonRequest = "{"
            + "\"values\": {"
            + "\"caseId\": \"123\","
            + "\"documentType\": \"statement\","
            + "\"partyName\": \"John Doe\","
            + "\"partyId\": \"7663081e-778d-4317-b278-7642b740d317\""
            + "}"
            + "}";

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testUploadCitizenStatementDocument() throws Exception {

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(authorisationService.authoriseService(anyString())).thenReturn(true);

        when(documentGenService.uploadDocument(anyString(), any())).thenReturn(DocumentResponse.builder()
                                                                                   .document(Document.builder()
                                                                                                 .documentFileName(
                                                                                                     "test.pdf")
                                                                                                 .build())
                                                                                   .build());

        when(uploadService.uploadCitizenDocument(anyString(), any())).thenReturn(UploadedDocuments.builder()
                                                                                     .citizenDocument(Document.builder()
                                                                                                          .documentFileName(
                                                                                                              "test.pdf")
                                                                                                          .build())
                                                                                     .build());

        when(idamClient.getUserInfo(anyString())).thenReturn(UserInfo.builder()
                                                                 .sub("123")
                                                                 .uid("123")
                                                                 .build());
        when(coreCaseDataApi.startEventForCitizen(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(StartEventResponse.builder()
                            .token("123")
                            .build());
        Map<String, Object> map = new HashMap<>();
        map.put("caseId",123L);
        map.put("citizenUploadedDocumentList", List.of(element(UploadedDocuments.builder().build())));
        map.put("state", "SUBMITTED_PAID");
        map.put("createdDate", "2023-03-29T00:27:28.863787");
        map.put("lastModifiedDate", "2023-03-29T00:27:28.863787");
        map.put("respondentsFL401", PartyDetails.builder()
                .user(User.builder()
                          .idamId("123")
                          .build())
            .build());
        map.put("applicantsFL401", PartyDetails.builder()
            .user(User.builder()
                      .idamId("123")
                      .build())
            .build());

        when(coreCaseDataApi.getCase(anyString(), anyString(), anyString())).thenReturn(CaseDetails.builder()
                                                                                            .id(123L)
                                                                                            .data(map)
                                                                                            .build());
        String url = "/upload-citizen-statement-document";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "test content".getBytes()
        );

        String jsonRequest = "{"
            + "\"caseId\": \"123\","
            + "\"documentType\": \"statement\","
            + "\"partyName\": \"John Doe\","
            + "\"partyId\": \"7663081e-778d-4317-b278-7642b740d317\","
            + "\"isApplicant\": \"true\","
            + "\"documentRequestedByCourt\": \"YES\""
            + "}";

        mockMvc.perform(
                multipart(url)
                    .file(file)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    //.contentType(APPLICATION_JSON)
                    .param("caseId", "123")
                    .param("documentType", "statement")
                    .param("partyName", "John Doe")
                    .param("partyId", "7663081e-778d-4317-b278-7642b740d317")
                    .param("isApplicant", "true")
                    .param("documentRequestedByCourt", "Yes"))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testUploadCitizenDocument() throws Exception {

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(authorisationService.authoriseService(anyString())).thenReturn(true);

        when(documentGenService.uploadDocument(anyString(), any())).thenReturn(DocumentResponse.builder()
                                                                                   .document(Document.builder()
                                                                                                 .documentFileName(
                                                                                                     "test.pdf")
                                                                                                 .build())
                                                                                   .build());

        String url = "/upload-citizen-document";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "test content".getBytes()
        );

        mockMvc.perform(
                multipart(url)
                    .file(file)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testDeleteDocument() throws Exception {

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(authorisationService.authoriseService(anyString())).thenReturn(true);

        when(documentGenService.deleteDocument(anyString(), anyString())).thenReturn(DocumentResponse.builder()
                                                                                         .status("Success")
                                                                                         .build());

        String url = "/123/delete";

        mockMvc.perform(
                delete(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testDownloadDocument() throws Exception {

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(authorisationService.authoriseService(anyString())).thenReturn(true);

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "test content".getBytes()
        );

        when(documentGenService.downloadDocument(
            anyString(),
            anyString()
        )).thenReturn(new ResponseEntity<>(file.getResource(), HttpStatus.OK));

        String url = "/123/download";

        mockMvc.perform(
                get(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testCitizenGenerateDocument() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(authorisationService.authoriseService(anyString())).thenReturn(true);

        DocumentResponse documentResponse = DocumentResponse.builder()
            .document(Document.builder().documentFileName("test.pdf").build())
            .build();

        when(documentGenService.generateAndUploadDocument(anyString(), any())).thenReturn(documentResponse);

        String url = "/citizen-generate-document";
        String jsonRequest = "{"
            + "\"caseId\": \"123\","
            + "\"partyName\": \"John Doe\","
            + "\"partyId\": \"7663081e-778d-4317-b278-7642b740d317\""
            + "}";

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testCitizenSubmitDocuments() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(authorisationService.authoriseService(anyString())).thenReturn(true);

        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();
        when(citizenDocumentService.citizenSubmitDocuments(anyString(), any())).thenReturn(caseDetails);

        String url = "/citizen-submit-documents";
        String jsonRequest = "{"
            + "\"caseId\": \"123\","
            + "\"partyName\": \"John Doe\","
            + "\"partyId\": \"7663081e-778d-4317-b278-7642b740d317\""
            + "}";

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}
