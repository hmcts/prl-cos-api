package uk.gov.hmcts.reform.prl.controllers;

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
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ExitAwaitingInformationService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_STATUS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;

@SpringBootTest(properties = {
    "feature.toggle.exitAwaitingInformationEnabled=true"
})
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ExitAwaitingInformationControllerIntegrationTest {

    private static final String AUTHORISATION_HEADER = "Authorization";
    private static final String SERVICE_AUTHORISATION_HEADER = "Service-Authorization";
    private static final String TEST_AUTH_TOKEN = "Bearer testAuthToken";
    private static final String TEST_SERVICE_AUTH_TOKEN = "testServiceAuthToken";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private AuthorisationService authorisationService;

    @MockBean
    private ExitAwaitingInformationService exitAwaitingInformationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        when(featureToggleService.isExitAwaitingInformationEnabled()).thenReturn(true);
    }

    private Map<String, Object> createMockCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("id", 12345678L);
        caseData.put(STATE_FIELD, "CASE_ISSUED");
        caseData.put(CASE_STATUS, CaseStatus.builder().state("Case Issued").build());
        return caseData;
    }

    @Test
    public void shouldSubmitExitAwaitingInformationSuccessfully() throws Exception {
        String url = "/submit-exit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(exitAwaitingInformationService.updateCase(any())).thenReturn(createMockCaseData());

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(12345678))
            .andExpect(jsonPath("$.data.state").value("CASE_ISSUED"))
            .andReturn();
    }

    @Test
    public void shouldRejectSubmitExitAwaitingInformationWithoutAuthorizationHeader() throws Exception {
        String url = "/submit-exit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        mockMvc.perform(
                post(url)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    public void shouldRejectSubmitExitAwaitingInformationWithUnauthorizedTokens() throws Exception {
        String url = "/submit-exit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "invalidToken")
                    .header(SERVICE_AUTHORISATION_HEADER, "invalidServiceToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }
}
