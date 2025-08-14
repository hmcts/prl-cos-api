package uk.gov.hmcts.reform.prl.controllers.noticeofchnage;


import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class NoticeOfChangeControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    NoticeOfChangePartiesService noticeOfChangePartiesService;

    @MockBean
    AuthorisationService authorisationService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testAboutToSubmitNoCRequest() throws Exception {
        String url = "/noc/aboutToSubmitNoCRequest";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        Mockito.when(noticeOfChangePartiesService.aboutToSubmitAdminRemoveLegalRepresentative(any(), any()))
            .thenReturn(new HashMap<>());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testSubmittedNoCRequest() throws Exception {
        String url = "/noc/submittedNoCRequest";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        /*Mockito.when(noticeOfChangePartiesService.submittedAdminRemoveLegalRepresentative(any()))
            .thenReturn(new HashMap<>());*/

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testAboutToStartStopRepresentation() throws Exception {
        String url = "/noc/aboutToStartStopRepresentation";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        Mockito.when(noticeOfChangePartiesService.populateAboutToStartAdminRemoveLegalRepresentative(any(), any()))
            .thenReturn(new HashMap<>());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testAboutToSubmitStopRepresentation() throws Exception {
        String url = "/noc/aboutToSubmitStopRepresentation";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        Mockito.when(noticeOfChangePartiesService.aboutToSubmitAdminRemoveLegalRepresentative(any(), any()))
            .thenReturn(new HashMap<>());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testSubmittedStopRepresentation() throws Exception {
        String url = "/noc/submittedStopRepresentation";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        /*Mockito.when(noticeOfChangePartiesService.submittedAdminRemoveLegalRepresentative(any()))
            .thenReturn(new HashMap<>());*/

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testAboutToStartAdminRemoveLegalRepresentative() throws Exception {
        String url = "/noc/aboutToStartAdminRemoveLegalRepresentative";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        Mockito.when(noticeOfChangePartiesService.populateAboutToStartAdminRemoveLegalRepresentative(any(), any()))
            .thenReturn(new HashMap<>());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testAboutToSubmitAdminRemoveLegalRepresentative() throws Exception {
        String url = "/noc/aboutToSubmitAdminRemoveLegalRepresentative";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        Mockito.when(noticeOfChangePartiesService.aboutToSubmitAdminRemoveLegalRepresentative(any(), any()))
            .thenReturn(new HashMap<>());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testSubmittedAdminRemoveLegalRepresentative() throws Exception {
        String url = "/noc/submittedAdminRemoveLegalRepresentative";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}
