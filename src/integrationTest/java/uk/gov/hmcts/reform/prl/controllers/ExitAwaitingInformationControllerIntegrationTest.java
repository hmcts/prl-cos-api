package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
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
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ExitAwaitingInformationService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_STATUS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ExitAwaitingInformationControllerIntegrationTest {

    private static final String AUTHORISATION_HEADER = "Authorization";
    private static final String TEST_AUTH_TOKEN = "Bearer testAuthToken";
    private static final String TEST_SERVICE_AUTH_TOKEN = "testServiceAuthToken";
    private static final String URL = "/submit-exit-awaiting-information";
    private static final long TEST_CASE_ID = 12345678L;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorisationService authorisationService;

    @MockBean
    private ExitAwaitingInformationService exitAwaitingInformationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
        when(featureToggleService.isExitAwaitingInformationEnabled()).thenReturn(true);
    }

    @Test
    public void shouldSubmitExitAwaitingInformationSuccessfully() throws Exception {
        when(authorisationService.isAuthorized(TEST_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN)).thenReturn(true);
        when(exitAwaitingInformationService.updateCase(any())).thenReturn(updatedCaseData());

        mockMvc.perform(
                post(URL)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORIZATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(validCallbackRequestJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.state").value(State.CASE_ISSUED.getValue()))
            .andExpect(jsonPath("$.data.caseStatus.state").value(State.CASE_ISSUED.getLabel()))
            .andExpect(jsonPath("$.data.existingField").value("existingValue"));

        verify(exitAwaitingInformationService).updateCase(any());
    }

    @Test
    public void shouldRejectSubmitExitAwaitingInformationWithoutAuthorizationHeader() throws Exception {
        mockMvc.perform(
                post(URL)
                    .header(SERVICE_AUTHORIZATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(validCallbackRequestJson()))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectSubmitExitAwaitingInformationWithoutServiceAuthorizationHeader() throws Exception {
        mockMvc.perform(
                post(URL)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(validCallbackRequestJson()))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectSubmitExitAwaitingInformationWithUnauthorizedTokens() throws Exception {
        when(authorisationService.isAuthorized(TEST_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN)).thenReturn(false);

        ServletException exception = assertThrows(ServletException.class, () -> mockMvc.perform(
                post(URL)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORIZATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(validCallbackRequestJson())));

        assertNotNull(exception.getCause());
        assertEquals("Invalid Client", exception.getCause().getMessage());
    }

    private String validCallbackRequestJson() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("existingField", "existingValue");

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("submit-exit-awaiting-information")
            .caseDetails(CaseDetails.builder()
                             .id(TEST_CASE_ID)
                             .state(State.AWAITING_INFORMATION.getValue())
                             .data(caseData)
                             .build())
            .caseDetailsBefore(CaseDetails.builder()
                                   .id(TEST_CASE_ID)
                                   .state(State.AWAITING_INFORMATION.getValue())
                                   .data(new HashMap<>(caseData))
                                   .build())
            .build();

        return objectMapper.writeValueAsString(callbackRequest);
    }

    private Map<String, Object> updatedCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("existingField", "existingValue");
        caseData.put(STATE_FIELD, State.CASE_ISSUED.getValue());
        caseData.put(CASE_STATUS, Map.of("state", State.CASE_ISSUED.getLabel()));
        return caseData;
    }
}
