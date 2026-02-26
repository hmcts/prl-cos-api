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
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(true);
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

    private Map<String, Object> createMockCaseDataWithInvalidDate() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("id", 12345678L);

        AwaitingInformation awaitingInfo = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().minusDays(1))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

        caseData.put(AWAITING_INFORMATION_DETAILS, awaitingInfo);
        caseData.put(CASE_STATUS, "Awaiting information");

        return caseData;
    }

    // Tests for submitAwaitingInformation endpoint
    @Test
    public void shouldSubmitAwaitingInformationSuccessfully() throws Exception {

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(awaitingInformationService.addToCase(any()))
            .thenReturn(createMockCaseDataWithAwaitingInformation());

        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");
        String url = "/submit-awaiting-information";
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
    public void shouldSubmitAwaitingInformationWithValidHeaders() throws Exception {


        when(authorisationService.isAuthorized(TEST_AUTH_TOKEN, TEST_SERVICE_AUTH_TOKEN))
            .thenReturn(true);
        when(awaitingInformationService.addToCase(any()))
            .thenReturn(createMockCaseDataWithAwaitingInformation());
        String url = "/submit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");
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
    public void shouldRejectSubmitAwaitingInformationWithUnauthorizedTokens() throws Exception {


        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        String url = "/submit-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");
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
    public void shouldRejectSubmitAwaitingInformationWithMissingAuthorizationHeader() throws Exception {
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
    public void shouldRejectSubmitAwaitingInformationWithMissingServiceAuthorizationHeader() throws Exception {
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

    // Tests for validateUrgentCaseCreation endpoint
    @Test
    public void shouldValidateAwaitingInformationSuccessfully() throws Exception {
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
    public void shouldValidateAwaitingInformationWithValidJson() throws Exception {
        when(awaitingInformationService.addToCase(any()))
            .thenReturn(createMockCaseDataWithAwaitingInformation());
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");
        mockMvc.perform(
                post(url)
                    .header("Accept", APPLICATION_JSON.toString())
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldValidateAwaitingInformationReturnErrorForInvalidDate() throws Exception {
        when(awaitingInformationService.addToCase(any()))
            .thenReturn(createMockCaseDataWithInvalidDate());
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");
        mockMvc.perform(
                post(url)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldValidateAwaitingInformationWithCorrectContentType() throws Exception {
        when(awaitingInformationService.addToCase(any()))
            .thenReturn(createMockCaseDataWithAwaitingInformation());
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");
        mockMvc.perform(
                post(url)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldHandleValidateAwaitingInformationWithMultipleReasonTypes() throws Exception {


        AwaitingInformationReasonEnum[] reasons = {
            AwaitingInformationReasonEnum.applicantFurtherInformation,
            AwaitingInformationReasonEnum.applicantClarifyConfidentialDetails,
            AwaitingInformationReasonEnum.dwpHmrcWhereaboutsUnknown,
            AwaitingInformationReasonEnum.respondentFurtherInformation
        };

        for (AwaitingInformationReasonEnum reason : reasons) {
            Map<String, Object> caseData = new HashMap<>();
            caseData.put("id", 12345678L);
            AwaitingInformation awaitingInfo = AwaitingInformation.builder()
                .reviewDate(LocalDate.now().plusDays(5))
                .awaitingInformationReasonEnum(reason)
                .build();
            caseData.put(AWAITING_INFORMATION_DETAILS, awaitingInfo);
            caseData.put(CASE_STATUS, "Awaiting information");

            when(awaitingInformationService.addToCase(any())).thenReturn(caseData);
            String url = "/validate-awaiting-information";
            String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");
            mockMvc.perform(
                    post(url)
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();
        }
    }

    @Test
    public void shouldValidateAwaitingInformationWithNullReviewDate() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("id", 12345678L);
        AwaitingInformation awaitingInfo = AwaitingInformation.builder()
            .reviewDate(null)
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();
        caseData.put(AWAITING_INFORMATION_DETAILS, awaitingInfo);

        when(awaitingInformationService.addToCase(any())).thenReturn(caseData);
        String url = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");
        mockMvc.perform(
                post(url)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldHandleSubmitAndValidateInSequence() throws Exception {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(awaitingInformationService.addToCase(any()))
            .thenReturn(createMockCaseDataWithAwaitingInformation());
        String submitUrl = "/submit-awaiting-information";
        String validateUrl = "/validate-awaiting-information";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");
        // Submit
        mockMvc.perform(
                post(submitUrl)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

        // Validate
        mockMvc.perform(
                post(validateUrl)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldHandleCaseDataWithAdditionalFields() throws Exception {

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("id", 12345678L);
        caseData.put("applicantName", "John Doe");
        caseData.put("respondentName", "Jane Doe");
        caseData.put("caseType", "C100");

        AwaitingInformation awaitingInfo = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(5))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();
        caseData.put(AWAITING_INFORMATION_DETAILS, awaitingInfo);
        caseData.put(CASE_STATUS, "Awaiting information");

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
            .andReturn();
    }

    @Test
    public void shouldHandleValidateWithDifferentReviewDateRanges() throws Exception {

        LocalDate[] dates = {
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(30),
            LocalDate.now().plusDays(365)
        };

        for (LocalDate date : dates) {
            Map<String, Object> caseData = new HashMap<>();
            caseData.put("id", 12345678L);
            AwaitingInformation awaitingInfo = AwaitingInformation.builder()
                .reviewDate(date)
                .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
                .build();
            caseData.put(AWAITING_INFORMATION_DETAILS, awaitingInfo);
            caseData.put(CASE_STATUS, "Awaiting information");

            when(awaitingInformationService.addToCase(any())).thenReturn(caseData);
            String url = "/validate-awaiting-information";
            String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");
            mockMvc.perform(
                    post(url)
                        .accept(APPLICATION_JSON)
                        .contentType(APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();
        }
    }
}

