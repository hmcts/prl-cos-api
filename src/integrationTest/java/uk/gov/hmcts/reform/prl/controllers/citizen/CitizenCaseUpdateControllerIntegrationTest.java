package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.AwpProcessHwfPaymentService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenCaseUpdateService;

import java.util.Map;

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
public class CitizenCaseUpdateControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CitizenCaseUpdateService citizenCaseUpdateService;

    @MockitoBean
    AuthorisationService authorisationService;

    @MockitoBean
    CaseService caseService;

    @MockitoBean
    AwpProcessHwfPaymentService awpProcessHwfPaymentService;

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    @Test
    public void testUpdatePartyDetails() throws Exception {
        String url = "/citizen/12345/67890/update-party-details";
        String jsonRequest = ResourceLoader.loadJson("requests/citizen-update-case.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(citizenCaseUpdateService.updateCitizenPartyDetails(anyString(), anyString(), anyString(), any()))
            .thenReturn(CaseDetails.builder().build());

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testSaveDraftCitizenApplication() throws Exception {
        String url = "/citizen/12345/save-c100-draft-application";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(citizenCaseUpdateService.saveDraftCitizenApplication(anyString(), any(), anyString())).thenReturn(
            CaseDetails.builder()
                .data(Map.of("caseId", 123L))
                .id(123L)
                .build());

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testSubmitC100Application() throws Exception {
        String url = "/citizen/12345/67890/submit-c100-application";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(citizenCaseUpdateService.submitCitizenC100Application(anyString(), anyString(), anyString(), any()))
            .thenReturn(CaseDetails.builder()
                            .data(Map.of("caseId", 123L))
                            .id(123L)
                            .build());

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testDeleteApplicationCitizen() throws Exception {
        String url = "/citizen/12345/delete-application";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(citizenCaseUpdateService.deleteApplication(
            anyString(),
            any(),
            anyString()
        )).thenReturn(CaseDetails.builder()
                          .data(Map.of("caseId", 123L))
                          .id(123L)
                          .build());

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testWithdrawCase() throws Exception {
        String url = "/citizen/12345/withdraw";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(citizenCaseUpdateService.withdrawCase(any(), anyString(), anyString())).thenReturn(CaseDetails.builder()
                                                                                                    .data(Map.of(
                                                                                                        "caseId",
                                                                                                        123L
                                                                                                    ))
                                                                                                    .id(123L)
                                                                                                    .build());

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testSaveCitizenAwpApplication() throws Exception {
        String url = "/citizen/12345/save-citizen-awp-application";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(citizenCaseUpdateService.saveCitizenAwpApplication(
            anyString(),
            anyString(),
            any()
        )).thenReturn(CaseDetails.builder()
                          .data(Map.of("caseId", 123L))
                          .id(123L)
                          .build());

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testProcessAwpWithHwfPayment() throws Exception {
        String url = "/citizen/awp-process-hwf-payment";

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }
}
