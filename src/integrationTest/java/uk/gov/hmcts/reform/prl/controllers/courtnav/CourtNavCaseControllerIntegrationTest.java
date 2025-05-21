package uk.gov.hmcts.reform.prl.controllers.courtnav;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.mapper.courtnav.FL401ApplicationMapper;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassUploadDocService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.courtnav.CourtNavCaseService;
import uk.gov.hmcts.reform.prl.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.util.ServiceAuthenticationGenerator;

import java.util.List;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.util.TestConstants.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.util.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.prl.util.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CourtNavCaseControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String caseEndpoint = "/case";

    private static final String VALID_REQUEST_BODY = "requests/courtnav-request.json";

    @Autowired
    CaseService caseService;

    @Autowired
    IdamTokenGenerator idamTokenGenerator;

    @Autowired
    ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @MockBean
    private AuthorisationService authorisationService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    SystemUserService systemUserService;

    @MockBean
    CafcassUploadDocService cafcassUploadDocService;

    @MockBean
    FL401ApplicationMapper fl401ApplicationMapper;

    @MockBean
    CourtNavCaseService courtNavCaseService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testCreateCaseEndpoint1() throws Exception {

        String caseId = "12345";
        String typeOfDocument = "testDocument";
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, "test content".getBytes());

        Mockito.when(authorisationService.authoriseService(any())).thenReturn(true);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        Mockito.when(authorisationService.authoriseUser(any())).thenReturn(true);
        Mockito.when(authorisationService.getUserInfo()).thenReturn(new UserInfo("userId", "email", "forename", "surname",
                                                                                 "roles", List.of("caseworker-privatelaw-cafcass")));
        Mockito.when(systemUserService.getSysUserToken()).thenReturn("sysUserToken");

        mockMvc.perform(MockMvcRequestBuilders.multipart("/" + caseId + "/document")
                            .file(file)
                            .param("typeOfDocument", typeOfDocument)
                            .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                            .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    }

    @Test
    public void testCreateCase() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);

        Mockito.when(authorisationService.authoriseUser(any())).thenReturn(true);
        Mockito.when(authorisationService.authoriseService(any())).thenReturn(true);
        Mockito.when(fl401ApplicationMapper.mapCourtNavData(any(), any())).thenReturn(CaseData.builder().build());
        Mockito.when(courtNavCaseService.createCourtNavCase(any(), any())).thenReturn(CaseDetails.builder().id(12345L).build());
        Mockito.doNothing().when(courtNavCaseService).refreshTabs(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.post("/case")
                            .header(AUTHORIZATION, TEST_AUTH_TOKEN)
                            .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
            .andExpect(status().isCreated())
            .andReturn();
    }

    @Test
    public void testCreateCaseReturn400() throws Exception {
        Mockito.when(authorisationService.authoriseUser(any())).thenReturn(true);
        Mockito.when(authorisationService.authoriseService(any())).thenReturn(true);
        Mockito.when(fl401ApplicationMapper.mapCourtNavData(any(), any())).thenReturn(CaseData.builder().build());
        Mockito.when(courtNavCaseService.createCourtNavCase(any(), any())).thenReturn(CaseDetails.builder().id(12345L).build());
        Mockito.doNothing().when(courtNavCaseService).refreshTabs(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.post("/case")
                            .header(AUTHORIZATION, TEST_AUTH_TOKEN)
                            .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andReturn();
    }

}
