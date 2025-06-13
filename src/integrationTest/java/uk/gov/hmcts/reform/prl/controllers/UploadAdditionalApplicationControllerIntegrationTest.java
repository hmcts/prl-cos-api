package uk.gov.hmcts.reform.prl.controllers;

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
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UploadAdditionalApplicationService;

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
public class UploadAdditionalApplicationControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    UploadAdditionalApplicationService uploadAdditionalApplicationService;

    @MockitoBean
    AuthorisationService authorisationService;

    @MockitoBean
    SystemUserService systemUserService;

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testPrePopulateApplicants() throws Exception {
        String url = "/pre-populate-applicants";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(uploadAdditionalApplicationService.populateHearingList(anyString(), any())).thenReturn(new HashMap<>());

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
    public void testUploadAdditionalApplicationAboutToSubmit() throws Exception {
        String url = "/upload-additional-application/about-to-submit";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(uploadAdditionalApplicationService.calculateAdditionalApplicationsFee(
            anyString(),
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
    public void testUploadAdditionalApplicationMidEventCalculateFee() throws Exception {
        String url = "/upload-additional-application/mid-event/calculate-fee";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(uploadAdditionalApplicationService.calculateAdditionalApplicationsFee(
            anyString(),
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
    public void testUploadAdditionalApplicationSubmitted() throws Exception {
        String url = "/upload-additional-application/submitted";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

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
    public void testUploadAdditionalApplicationMidEvent() throws Exception {
        String url = "/upload-additional-application/mid-event";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(systemUserService.getSysUserToken()).thenReturn("systemAuthToken");
        when(uploadAdditionalApplicationService.populateHearingList(anyString(), any())).thenReturn(new HashMap<>());

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
