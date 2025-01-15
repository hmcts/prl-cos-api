package uk.gov.hmcts.reform.prl.controllers.cafcass;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassCdamService;

import java.util.List;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.util.TestConstants.CAFCASS_DOCUMENT_DOWNLOAD_ENDPOINT;
import static uk.gov.hmcts.reform.prl.util.TestConstants.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.util.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.prl.util.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CafcassDocumentManagementControllerIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private AuthorisationService authorisationService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    CafcassCdamService cafcassCdamService;

    @MockBean
    SystemUserService systemUserService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenValidDocumentIdDocumentManagementControllerReturnsOkStatus() throws Exception {
        final UUID documentId = randomUUID();

        final ResponseEntity<Resource> response = ResponseEntity.status(OK).contentType(APPLICATION_PDF).build();

        Mockito.when(authorisationService.authoriseService(any())).thenReturn(true);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        Mockito.when(authorisationService.authoriseUser(any())).thenReturn(true);

        //create new userInfo object using its constructor with 6 arguments
        UserInfo userInfo = new UserInfo("userId", "email", "forename", "surname",
                                         "roles", List.of("caseworker-privatelaw-cafcass"));
        Mockito.when(authorisationService.getUserInfo()).thenReturn(userInfo);
        Mockito.when(cafcassCdamService.getDocument(anyString(), anyString(), any())).thenReturn(response);
        Mockito.when(systemUserService.getSysUserToken()).thenReturn("sysUserToken");

        mockMvc.perform(
                get(CAFCASS_DOCUMENT_DOWNLOAD_ENDPOINT, documentId)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenInvalidDocumentIdDocumentManagementControllerReturnsBadRequestStatus() throws Exception {
        final UUID documentId = randomUUID();

        final ResponseEntity<Resource> response = ResponseEntity.status(BAD_REQUEST).contentType(APPLICATION_PDF).build();

        Mockito.when(authorisationService.authoriseService(any())).thenReturn(true);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        Mockito.when(authorisationService.authoriseUser(any())).thenReturn(true);

        //create new userInfo object using its constructor with 6 arguments
        UserInfo userInfo = new UserInfo("userId", "email", "forename", "surname",
                                         "roles", List.of("caseworker-privatelaw-cafcass"));
        Mockito.when(authorisationService.getUserInfo()).thenReturn(userInfo);
        Mockito.when(cafcassCdamService.getDocument(anyString(), anyString(), any())).thenReturn(response);
        Mockito.when(systemUserService.getSysUserToken()).thenReturn("sysUserToken");

        mockMvc.perform(
                        get(CAFCASS_DOCUMENT_DOWNLOAD_ENDPOINT, documentId)
                                .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                                .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                                .accept(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
