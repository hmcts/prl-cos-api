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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.enums.awaitinginformation.AwaitingInformationReasonEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AwaitingInformation;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.AwaitingInformationService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWAITING_INFORMATION_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_STATUS;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.util.TestConstants.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.util.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.prl.util.TestConstants.TEST_SERVICE_AUTH_TOKEN;


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

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    private Map<String, Object> createMockCaseDataWithAwaitingInformation() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("id", 12345678L);

        AwaitingInformation awaitingInfo = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(5))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

        caseData.put(AWAITING_INFORMATION_DETAILS, awaitingInfo);
        caseData.put(CASE_STATUS, "Awaiting information");

        return caseData;
    }

    @Test
    public void testSubmitAwaitingInformationSuccess() throws Exception {
        String url = "/submit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(awaitingInformationService.addToCase(any()))
            .thenReturn(createMockCaseDataWithAwaitingInformation());

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

    @Test
    public void testSubmitAwaitingInformationUnauthorized() throws Exception {
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
    public void testPopulateHeaderUnauthorized() throws Exception {
        String url = "/populate-header-awaiting-information";
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
    public void testValidateAwaitingInformationSuccess() throws Exception {
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(awaitingInformationService.addToCase(any()))
            .thenReturn(createMockCaseDataWithAwaitingInformation());

        mockMvc.perform(
                post(url)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testSubmitAwaitingInformationWithCorrectHeaders() throws Exception {
        String url = "/submit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(TEST_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(true);
        when(awaitingInformationService.addToCase(any()))
            .thenReturn(createMockCaseDataWithAwaitingInformation());

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .header("Accept", APPLICATION_JSON.toString())
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testPopulateHeaderWithCorrectHeaders() throws Exception {
        String url = "/populate-header-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(TEST_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .header("Accept", APPLICATION_JSON.toString())
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testValidateAwaitingInformationWithValidJson() throws Exception {
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(awaitingInformationService.addToCase(any()))
            .thenReturn(createMockCaseDataWithAwaitingInformation());

        mockMvc.perform(
                post(url)
                    .header("Accept", APPLICATION_JSON.toString())
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testSubmitAwaitingInformationWithDifferentTokens() throws Exception {
        String url = "/submit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");
        String token1 = "Bearer token1";
        String token2 = "Bearer token2";
        String serviceToken1 = "serviceToken1";
        String serviceToken2 = "serviceToken2";

        when(authorisationService.isAuthorized(token1, serviceToken1)).thenReturn(true);
        when(authorisationService.isAuthorized(token2, serviceToken2)).thenReturn(false);
        when(awaitingInformationService.addToCase(any()))
            .thenReturn(createMockCaseDataWithAwaitingInformation());

        // First request with valid tokens
        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, token1)
                    .header(SERVICE_AUTHORISATION_HEADER, serviceToken1)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

        // Second request with invalid tokens
        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, token2)
                    .header(SERVICE_AUTHORISATION_HEADER, serviceToken2)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isInternalServerError())
            .andReturn();
    }

    @Test
    public void testAllEndpointsSequentially() throws Exception {
        String submitUrl = "/submit-awaiting-information";
        String populateHeaderUrl = "/populate-header-awaiting-information";
        String validateUrl = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(awaitingInformationService.addToCase(any()))
            .thenReturn(createMockCaseDataWithAwaitingInformation());

        // Test submit endpoint
        mockMvc.perform(
                post(submitUrl)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

        // Test populate header endpoint
        mockMvc.perform(
                post(populateHeaderUrl)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

        // Test validate endpoint
        mockMvc.perform(
                post(validateUrl)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testSubmitAwaitingInformationMissingAuthHeader() throws Exception {
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
    public void testPopulateHeaderMissingServiceAuthHeader() throws Exception {
        String url = "/populate-header-awaiting-information";
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
    public void testSubmitAwaitingInformationResponseContentType() throws Exception {
        String url = "/submit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(awaitingInformationService.addToCase(any()))
            .thenReturn(createMockCaseDataWithAwaitingInformation());

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

    @Test
    public void testValidateAwaitingInformationResponseContentType() throws Exception {
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(awaitingInformationService.addToCase(any()))
            .thenReturn(createMockCaseDataWithAwaitingInformation());

        mockMvc.perform(
                post(url)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}

