package uk.gov.hmcts.reform.prl.controllers.barrister;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASEWORKER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;

@Slf4j
@SpringBootTest(properties = {
    "feature.toggle.barristerFeatureEnabled=true"
})
@RunWith(SpringRunner.class)
@ContextConfiguration
public class BarristerControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AuthorisationService authorisationService;
    @MockBean
    UserService userService;
    @MockBean
    OrganisationService organisationService;

    private static final String AUTH_TOKEN = "auth-token";
    private static final String SERVICE_TOKEN = "service-token";

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.findAndRegisterModules();
    }

    @Test
    public void testBarristerControllerAboutToStart() throws Exception {
        String url = "/barrister/add/about-to-start";
        String jsonRequest = ResourceLoader.loadJson("controller/barristerAboutToStartCallBackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(organisationService.findUserOrganisation(any())).thenReturn(Optional.of(Organisations.builder()
                                                                                         .organisationIdentifier("orgId")
                                                                                         .build()));
        when(userService.getUserDetails(AUTH_TOKEN)).thenReturn(UserDetails.builder().roles(List.of(CASEWORKER)).build());

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, AUTH_TOKEN)
                    .header(SERVICE_AUTHORIZATION_HEADER, SERVICE_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testBarristerControllerRemoveAboutToStart() throws Exception {
        String url = "/barrister/remove/about-to-start";
        String jsonRequest = ResourceLoader.loadJson("controller/barristerAboutToStartCallBackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(organisationService.findUserOrganisation(any())).thenReturn(Optional.of(Organisations.builder()
                                                                                         .organisationIdentifier("orgId")
                                                                                         .build()));
        when(userService.getUserDetails(any())).thenReturn(UserDetails.builder().roles(List.of(CASEWORKER)).build());

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, AUTH_TOKEN)
                    .header(SERVICE_AUTHORIZATION_HEADER, SERVICE_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testBarristerControllerAddSubmitted() throws Exception {
        String url = "/barrister/add/submitted";
        String jsonRequest = ResourceLoader.loadJson("controller/barristerSubmittedCallBackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

        when(userService.getUserDetails(any())).thenReturn(UserDetails.builder().roles(List.of(CASEWORKER)).build());

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, AUTH_TOKEN)
                    .header(SERVICE_AUTHORIZATION_HEADER, SERVICE_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testRemoveBarristerSubmitted() throws Exception {
        String url = "/barrister/remove/submitted";
        String jsonRequest = ResourceLoader.loadJson("controller/barristerSubmittedCallBackRequest.json");
        when(authorisationService.isAuthorized(anyString(), anyString()))
            .thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, AUTH_TOKEN)
                    .header(SERVICE_AUTHORIZATION_HEADER, SERVICE_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}
