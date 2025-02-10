package uk.gov.hmcts.reform.prl.controllers.serviceofdocuments;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
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
import uk.gov.hmcts.reform.prl.services.serviceofdocuments.ServiceOfDocumentsService;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ServiceOfDocumentsConfCheckControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    AuthorisationService authorisationService;

    @MockBean
    ServiceOfDocumentsService serviceOfDocumentsService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Ignore
    @Test
    public void testSodConfCheckAboutToStart() throws Exception {
        String url = "/sod-conf-check/about-to-start";
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

    @Ignore
    @Test
    public void testSodConfCheckAboutToSubmit() throws Exception {
        String url = "/sod-conf-check/about-to-submit";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        Mockito.when(serviceOfDocumentsService.handleConfCheckAboutToSubmit(any())).thenReturn(new HashMap<>());

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

    @Ignore
    @Test
    public void testSodConfCheckSubmitted() throws Exception {
        String url = "/sod-conf-check/submitted";
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
