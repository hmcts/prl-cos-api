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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenEmailService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class CitizenCallbackControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    AllTabServiceImpl allTabsService;

    @MockitoBean
    CoreCaseDataApi coreCaseDataApi;

    @MockitoBean
    AuthTokenGenerator authTokenGenerator;

    @MockitoBean
    SystemUserService systemUserService;

    @MockitoBean
    CitizenEmailService citizenEmailService;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    @Test
    public void testCitizenCaseCreationCallbackSubmitted() throws Exception {
        String url = "/citizen-case-creation-callback/submitted";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(systemUserService.getSysUserToken()).thenReturn("testSysUserToken");
        when(authTokenGenerator.generate()).thenReturn("testAuthToken");

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testUpdateCitizenApplication() throws Exception {
        String url = "/update-citizen-application";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(allTabsService.updateAllTabsIncludingConfTab(anyString())).thenReturn(CaseDetails.builder()
                                                                                       .data(Map.of("caseId", 123L))
                                                                                       .id(123L)
                                                                                       .build());
        doNothing().when(citizenEmailService).sendCitizenCaseSubmissionEmail(anyString(), any());

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testSendNotificationsOnCaseWithdrawn() throws Exception {
        String url = "/citizen-case-withdrawn-notification";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        doNothing().when(citizenEmailService).sendCitizenCaseWithdrawalEmail(anyString(), any());

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}
