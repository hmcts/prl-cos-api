package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.StmtOfServImplService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class StatementOfServiceControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    AuthorisationService authorisationService;

    @MockBean
    StmtOfServImplService stmtOfServImplService;

    @Autowired
    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    @Test
    public void testSosAboutToStart() throws Exception {
        String url = "/Statement-of-service-about-to-start";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(authorisationService.authoriseService(anyString())).thenReturn(true);
        when(stmtOfServImplService.retrieveRespondentsList(any())).thenReturn(new HashMap<>());

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
    public void testSosAboutToSubmit() throws Exception {
        String url = "/Statement-of-service-about-to-submit";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(authorisationService.authoriseService(anyString())).thenReturn(true);
        when(stmtOfServImplService.handleSosAboutToSubmit(any(), anyString())).thenReturn(new HashMap<>());

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
    public void testSosSubmitConfirmation() throws Exception {
        String url = "/Statement-of-service-confirmation";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(authorisationService.authoriseService(anyString())).thenReturn(true);

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
    public void testCitizenSoaSubmit() throws Exception {
        String url = "/12345/67890/save-statement-of-service-by-citizen";
        String jsonRequest = ResourceLoader.loadJson("requests/service-of-application.json");

        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(authorisationService.authoriseService(anyString())).thenReturn(true);

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
