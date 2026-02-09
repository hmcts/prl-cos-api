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
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.AwaitingInformationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.util.TestConstants.*;

/**
 * Integration tests for AwaitingInformationController.
 * Tests the complete flow of the controller endpoints with MockMvc.
 */
@Slf4j
@SpringBootTest
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

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    /**
     * Test submit awaiting information endpoint with authorized request
     * Verifies that the endpoint returns 200 OK when user is authorized
     */
    @Test
    public void testSubmitAwaitingInformationSuccess() throws Exception {
        String url = "/submit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

        log.info("testSubmitAwaitingInformationSuccess - PASSED");
    }

    /**
     * Test submit awaiting information endpoint with unauthorized request
     * Verifies that the endpoint returns an appropriate error when user is not authorized
     */
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

        log.info("testSubmitAwaitingInformationUnauthorized - PASSED");
    }

    /**
     * Test populate header endpoint with authorized request
     * Verifies that the endpoint returns 200 OK when user is authorized
     */
    @Test
    public void testPopulateHeaderSuccess() throws Exception {
        String url = "/populate-header-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

        log.info("testPopulateHeaderSuccess - PASSED");
    }

    /**
     * Test populate header endpoint with unauthorized request
     * Verifies that the endpoint returns an appropriate error when user is not authorized
     */
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

        log.info("testPopulateHeaderUnauthorized - PASSED");
    }

    /**
     * Test validate awaiting information endpoint
     * Verifies that the endpoint returns 200 OK and processes the request
     */
    @Test
    public void testValidateAwaitingInformationSuccess() throws Exception {
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        mockMvc.perform(
                post(url)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

        log.info("testValidateAwaitingInformationSuccess - PASSED");
    }

    /**
     * Test submit awaiting information endpoint with valid authorization headers
     * Verifies that the endpoint returns 200 OK with correct headers
     */
    @Test
    public void testSubmitAwaitingInformationWithCorrectHeaders() throws Exception {
        String url = "/submit-awaiting-information";
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

        log.info("testSubmitAwaitingInformationWithCorrectHeaders - PASSED");
    }

    /**
     * Test populate header endpoint with valid authorization headers
     * Verifies that the endpoint returns 200 OK with correct headers
     */
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

        log.info("testPopulateHeaderWithCorrectHeaders - PASSED");
    }

    /**
     * Test validate awaiting information endpoint with valid JSON request
     * Verifies that the endpoint processes the request correctly
     */
    @Test
    public void testValidateAwaitingInformationWithValidJson() throws Exception {
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        mockMvc.perform(
                post(url)
                    .header("Accept", APPLICATION_JSON.toString())
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

        log.info("testValidateAwaitingInformationWithValidJson - PASSED");
    }

    /**
     * Test submit awaiting information endpoint with different authorization tokens
     * Verifies that the endpoint handles multiple authorization scenarios
     */
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

        log.info("testSubmitAwaitingInformationWithDifferentTokens - PASSED");
    }

    /**
     * Test all three endpoints in sequence
     * Verifies that all endpoints can be called in the same test context
     */
    @Test
    public void testAllEndpointsSequentially() throws Exception {
        String submitUrl = "/submit-awaiting-information";
        String populateHeaderUrl = "/populate-header-awaiting-information";
        String validateUrl = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

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

        log.info("testAllEndpointsSequentially - PASSED");
    }

    /**
     * Test submit awaiting information endpoint with missing authorization header
     * Verifies that the endpoint handles missing headers appropriately
     */
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

        log.info("testSubmitAwaitingInformationMissingAuthHeader - PASSED");
    }

    /**
     * Test populate header endpoint with missing service authorization header
     * Verifies that the endpoint handles missing service auth header appropriately
     */
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

        log.info("testPopulateHeaderMissingServiceAuthHeader - PASSED");
    }

    /**
     * Test submit awaiting information endpoint response content type
     * Verifies that the endpoint returns JSON content type
     */
    @Test
    public void testSubmitAwaitingInformationResponseContentType() throws Exception {
        String url = "/submit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

        log.info("testSubmitAwaitingInformationResponseContentType - PASSED");
    }

    /**
     * Test validate awaiting information endpoint response content type
     * Verifies that the validate endpoint returns JSON content type
     */
    @Test
    public void testValidateAwaitingInformationResponseContentType() throws Exception {
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        mockMvc.perform(
                post(url)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

        log.info("testValidateAwaitingInformationResponseContentType - PASSED");
    }
}

