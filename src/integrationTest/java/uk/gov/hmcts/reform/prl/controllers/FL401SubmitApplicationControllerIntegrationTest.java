package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.FL401SubmitApplicationService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.validators.FL401StatementOfTruthAndSubmitChecker;

import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class FL401SubmitApplicationControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    FL401StatementOfTruthAndSubmitChecker fl401StatementOfTruthAndSubmitChecker;

    @MockitoBean
    LocationRefDataService locationRefDataService;

    @MockitoBean
    FL401SubmitApplicationService fl401SubmitApplicationService;

    @MockitoBean
    AuthorisationService authorisationService;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    @Test
    public void testFl401SubmitApplicationValidation() throws Exception {
        String url = "/fl401-submit-application-validation";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(locationRefDataService.getDaCourtLocations(anyString())).thenReturn(new ArrayList<>());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testFl401GenerateDocumentSubmitApplication() throws Exception {
        String url = "/fl401-generate-document-submit-application";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(fl401SubmitApplicationService.fl401GenerateDocumentSubmitApplication(
            anyString(),
            any(),
            any()
        )).thenReturn(new HashMap<>());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testFl401SubmitApplicationSendNotification() throws Exception {
        String url = "/fl401-submit-application-send-notification";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(fl401SubmitApplicationService.fl401SendApplicationNotification(
            anyString(),
            any()
        )).thenReturn(CaseData.builder().build());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}
