package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;

import java.util.Map;

import static org.apache.commons.lang3.RandomUtils.nextLong;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.util.TestConstants.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.util.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.prl.util.TestConstants.TEST_SERVICE_AUTH_TOKEN;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class C100RespondentSolicitiorControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    C100RespondentSolicitorService respondentSolicitorService;

    @MockBean
    AuthorisationService authorisationService;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EventService eventPublisher;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    @Test
    public void testGetC100RespondentSolicitor() throws Exception {
        String url = "/respondent-solicitor/about-to-start";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

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
    public void testHandleAboutToSubmit() throws Exception {
        String url = "/respondent-solicitor/about-to-submit";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

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
    public void testGenerateConfidentialityDynamicSelectionDisplay() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(nextLong())
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        Mockito.when(respondentSolicitorService.generateConfidentialityDynamicSelectionDisplay(any())).thenReturn(Map.of("childName", "123"));

        String url = "/respondent-solicitor/keep-details-private-list";
        String jsonRequest = ResourceLoader.loadJson("requests/C100-case-data.json");


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
    public void testGenerateC7ResponseDraftDocument() throws Exception {
        String url = "/respondent-solicitor/generate-c7response-document";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

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
    public void testValidateActiveRespondentResponseBeforeStart() throws Exception {
        String url = "/respondent-solicitor/about-to-start-response-validation";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

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
    public void testUpdateC7ResponseSubmit() throws Exception {
        String url = "/respondent-solicitor/submit-c7-response";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

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
    public void testSubmittedC7Response() throws Exception {
        String url = "/respondent-solicitor/submitted";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");
        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        Mockito.doNothing().when(eventPublisher).publishEvent(any());

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



