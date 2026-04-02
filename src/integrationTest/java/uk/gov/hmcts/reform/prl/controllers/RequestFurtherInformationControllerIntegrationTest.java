package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;
import uk.gov.hmcts.reform.prl.services.RequestFurtherInformationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class RequestFurtherInformationControllerIntegrationTest {

    private static final String AUTHORISATION_HEADER = "Authorization";
    private static final String TEST_AUTH_TOKEN = "Bearer testAuthToken";
    private static final String TEST_SERVICE_AUTH_TOKEN = "testServiceAuthToken";
    private static final String SUBMIT_URL = "/submit-request-further-information";
    private static final String VALIDATE_URL = "/validate-request-further-information";
    private static final long TEST_CASE_ID = 12345678L;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorisationService authorisationService;

    @MockBean
    private RequestFurtherInformationService requestFurtherInformationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Before
    public void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(true);
    }

    @Test
    public void shouldSubmitAwaitingInformationSuccessfully() throws Exception {
        when(authorisationService.isAuthorized(TEST_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN)).thenReturn(true);
        when(requestFurtherInformationService.addToCase(any())).thenReturn(submittedCaseData());

        mockMvc.perform(
                post(SUBMIT_URL)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORIZATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(validCallbackRequestJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(TEST_CASE_ID))
            .andExpect(jsonPath("$.data.caseStatus.state").value("Awaiting information"))
            .andExpect(jsonPath("$.data.applicantName").value("John Doe"))
            .andExpect(jsonPath("$.data.respondentName").value("Jane Doe"));

        verify(requestFurtherInformationService).addToCase(any());
    }

    @Test
    public void shouldRejectSubmitAwaitingInformationWithoutAuthorizationHeader() throws Exception {
        mockMvc.perform(
                post(SUBMIT_URL)
                    .header(SERVICE_AUTHORIZATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(validCallbackRequestJson()))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectSubmitAwaitingInformationWithoutServiceAuthorizationHeader() throws Exception {
        mockMvc.perform(
                post(SUBMIT_URL)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(validCallbackRequestJson()))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectSubmitAwaitingInformationWithUnauthorizedTokens() throws Exception {
        when(authorisationService.isAuthorized(TEST_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN)).thenReturn(false);

        ServletException exception = assertThrows(ServletException.class, () -> mockMvc.perform(
                post(SUBMIT_URL)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORIZATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(validCallbackRequestJson())));

        assertNotNull(exception.getCause());
        assertEquals("Invalid Client", exception.getCause().getMessage());
    }

    @Test
    public void shouldValidateAwaitingInformationSuccessfully() throws Exception {
        when(requestFurtherInformationService.validate(any(CallbackRequest.class))).thenReturn(new ArrayList<>());

        mockMvc.perform(
                post(VALIDATE_URL)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(validCallbackRequestJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    public void shouldValidateAwaitingInformationWithErrors() throws Exception {
        when(requestFurtherInformationService.validate(any(CallbackRequest.class)))
            .thenReturn(List.of("Please enter a future date"));

        mockMvc.perform(
                post(VALIDATE_URL)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(validCallbackRequestJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors[0]").value("Please enter a future date"));
    }

    @Test
    public void shouldReturnEmptyValidationErrorsWhenFeatureToggleDisabled() throws Exception {
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(false);

        mockMvc.perform(
                post(VALIDATE_URL)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(validCallbackRequestJson()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    public void shouldRejectValidationWithoutContentType() throws Exception {
        mockMvc.perform(
                post(VALIDATE_URL)
                    .content(validCallbackRequestJson()))
            .andExpect(status().isUnsupportedMediaType());
    }

    private String validCallbackRequestJson() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("id", TEST_CASE_ID);
        caseData.put("reviewDate", "2030-01-01");
        caseData.put("existingField", "existingValue");

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("request-further-information")
            .caseDetails(CaseDetails.builder()
                             .id(TEST_CASE_ID)
                             .state("AWAITING_INFORMATION")
                             .data(caseData)
                             .build())
            .caseDetailsBefore(CaseDetails.builder()
                                   .id(TEST_CASE_ID)
                                   .state("CASE_ISSUED")
                                   .data(new HashMap<>(caseData))
                                   .build())
            .build();

        return objectMapper.writeValueAsString(callbackRequest);
    }

    private Map<String, Object> submittedCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("id", TEST_CASE_ID);
        caseData.put("applicantName", "John Doe");
        caseData.put("respondentName", "Jane Doe");
        caseData.put(CASE_STATUS, CaseStatus.builder().state("Awaiting information").build());
        return caseData;
    }
}
