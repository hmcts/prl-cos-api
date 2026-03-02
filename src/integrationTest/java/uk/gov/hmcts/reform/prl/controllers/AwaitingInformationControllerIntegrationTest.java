package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.AwaitingInformationService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_STATUS;

@Slf4j
@SpringBootTest(properties = {
    "feature.toggle.awaitingInformationEnabled=true"
})
@RunWith(SpringRunner.class)
@ContextConfiguration
public class AwaitingInformationControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private AuthorisationService authorisationService;

    @MockBean
    private AwaitingInformationService awaitingInformationService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String AUTHORISATION_HEADER = "Authorization";
    private static final String SERVICE_AUTHORISATION_HEADER = "Service-Authorization";
    private static final String TEST_AUTH_TOKEN = "Bearer testAuthToken";
    private static final String TEST_SERVICE_AUTH_TOKEN = "testServiceAuthToken";

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(true);
    }

    private Map<String, Object> createMockCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("id", 12345678L);
        caseData.put(CASE_STATUS, CaseStatus.builder().state("Awaiting information").build());
        return caseData;
    }

    // Tests for /submit-awaiting-information endpoint

    @Test
    public void shouldSubmitAwaitingInformationSuccessfully() throws Exception {
        String url = "/submit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(awaitingInformationService.addToCase(any())).thenReturn(createMockCaseData());

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").exists())
            .andReturn();
    }

    @Test
    public void shouldRejectSubmitAwaitingInformationWithoutAuthorizationHeader() throws Exception {
        String url = "/submit-awaiting-information";
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
    public void shouldRejectSubmitAwaitingInformationWithoutServiceAuthorizationHeader() throws Exception {
        String url = "/submit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    @Test
    public void shouldRejectSubmitAwaitingInformationWithUnauthorizedTokens() throws Exception {
        String url = "/submit-awaiting-information";
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

    @Test
    public void shouldSubmitAwaitingInformationWithValidHeaders() throws Exception {
        String url = "/submit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(TEST_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN)).thenReturn(true);
        when(awaitingInformationService.addToCase(any())).thenReturn(createMockCaseData());

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.id").value(12345678))
            .andReturn();
    }

    @Test
    public void shouldHandleSubmitAwaitingInformationWithAdditionalCaseData() throws Exception {
        Map<String, Object> caseData = createMockCaseData();
        caseData.put("applicantName", "John Doe");
        caseData.put("respondentName", "Jane Doe");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(awaitingInformationService.addToCase(any())).thenReturn(caseData);

        String url = "/submit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.applicantName").value("John Doe"))
            .andExpect(jsonPath("$.data.respondentName").value("Jane Doe"))
            .andReturn();
    }

    // Tests for /validate-awaiting-information endpoint

    @Test
    public void shouldValidateAwaitingInformationSuccessfully() throws Exception {
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(awaitingInformationService.validate(any(uk.gov.hmcts.reform.ccd.client.model.CallbackRequest.class)))
            .thenReturn(new ArrayList<>());

        mockMvc.perform(
                post(url)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").isArray())
            .andReturn();
    }

    @Test
    public void shouldValidateAwaitingInformationWithErrors() throws Exception {
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        List<String> errorList = new ArrayList<>();
        errorList.add("Please enter a future date");

        when(awaitingInformationService.validate(any(uk.gov.hmcts.reform.ccd.client.model.CallbackRequest.class)))
            .thenReturn(errorList);

        mockMvc.perform(
                post(url)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors[0]").value("Please enter a future date"))
            .andReturn();
    }

    @Test
    public void shouldValidateAwaitingInformationWithMultipleErrors() throws Exception {

        List<String> errorList = new ArrayList<>();
        errorList.add("Please enter a future date");
        errorList.add("Review date cannot be more than 12 months away");

        when(awaitingInformationService.validate(any(uk.gov.hmcts.reform.ccd.client.model.CallbackRequest.class)))
            .thenReturn(errorList);

        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");
        mockMvc.perform(
                post(url)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors.length()").value(2))
            .andReturn();
    }

    @Test
    public void shouldValidateAwaitingInformationReturnEmptyErrorsWhenFeatureToggleDisabled() throws Exception {
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(false);

        mockMvc.perform(
                post(url)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").isEmpty())
            .andReturn();
    }

    @Test
    public void shouldValidateAwaitingInformationWithCorrectContentType() throws Exception {
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(awaitingInformationService.validate(any(uk.gov.hmcts.reform.ccd.client.model.CallbackRequest.class)))
            .thenReturn(new ArrayList<>());

        mockMvc.perform(
                post(url)
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldHandleValidateAwaitingInformationWithoutContentType() throws Exception {
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(awaitingInformationService.validate(any(uk.gov.hmcts.reform.ccd.client.model.CallbackRequest.class)))
            .thenReturn(new ArrayList<>());

        mockMvc.perform(
                post(url)
                    .content(jsonRequest))
            .andExpect(status().isUnsupportedMediaType())
            .andReturn();
    }

    // Integration workflow tests

    @Test
    public void shouldHandleCompleteAwaitingInformationWorkflow() throws Exception {
        String submitUrl = "/submit-awaiting-information";
        String validateUrl = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(awaitingInformationService.addToCase(any())).thenReturn(createMockCaseData());
        when(awaitingInformationService.validate(any(uk.gov.hmcts.reform.ccd.client.model.CallbackRequest.class)))
            .thenReturn(new ArrayList<>());

        // Submit awaiting information
        mockMvc.perform(
                post(submitUrl)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

        // Validate awaiting information
        mockMvc.perform(
                post(validateUrl)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldHandleSequentialValidationCalls() throws Exception {
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(awaitingInformationService.validate(any(uk.gov.hmcts.reform.ccd.client.model.CallbackRequest.class)))
            .thenReturn(new ArrayList<>());

        // First validation call
        mockMvc.perform(
                post(url)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

        // Second validation call
        mockMvc.perform(
                post(url)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldHandleMultipleSubmitCalls() throws Exception {
        String url = "/submit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(awaitingInformationService.addToCase(any())).thenReturn(createMockCaseData());

        // First submit
        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

        // Second submit
        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}

