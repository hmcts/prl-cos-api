package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.exception.CaseActivationNotAllowedException;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.citizen.CaseActivationResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class LinkCitizenCaseControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LinkCitizenCaseService linkCitizenCaseService;

    @MockBean
    AuthorisationService authorisationService;

    @MockBean
    HearingService hearingService;

    @MockBean
    CaseService caseService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    @Test
    public void testLinkCaseToAccount() throws Exception {
        String url = "/citizen/link-case-to-account";
        String jsonRequest = "{ \"caseId\": \"12345\", \"accessCode\": \"testAccessCode\" }";

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(linkCitizenCaseService.linkCitizenToCase(anyString(), anyString(), anyString()))
            .thenReturn(Optional.of(CaseDetails.builder()
                                        .id(223L)
                                        .data(Map.of("caseId", 12345L))
                                        .build()));

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testLinkCaseToAccountWithHearing() throws Exception {
        String url = "/citizen/link-case-to-account-with-hearing";
        String jsonRequest = "{ \"caseId\": \"12345\", \"accessCode\": \"testAccessCode\", \"hearingNeeded\": \"Yes\" }";

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(linkCitizenCaseService.linkCitizenToCase(anyString(), anyString(), anyString()))
            .thenReturn(Optional.of(CaseDetails.builder()
                                        .id(223L)
                                        .data(Map.of("caseId", 12345L))
                                        .build()));
        when(hearingService.getHearings(anyString(), anyString())).thenReturn(Hearings.hearingsWith().build());

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testValidateAccessCode() throws Exception {
        String url = "/citizen/validate-access-code";
        String jsonRequest = "{ \"caseId\": \"12345\", \"accessCode\": \"testAccessCode\" }";

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(linkCitizenCaseService.validateAccessCode(anyString(), anyString(), anyString())).thenReturn("Valid");

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void checkCaseActivationReturnsOnlyTheOutcome() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        for (String outcome : new String[]{"Valid", "NotReady", "Invalid", "Linked", "Duplicate"}) {
            when(linkCitizenCaseService.checkCaseActivation("1234567890123456", "ABCD1234", "testAuthToken"))
                .thenReturn(new CaseActivationResponse(outcome));
            mockMvc.perform(post("/citizen/check-case-activation")
                                .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                                .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                                .contentType(APPLICATION_JSON)
                                .content("{\"caseId\":\"1234567890123456\",\"accessCode\":\"ABCD1234\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"" + outcome + "\"}", true));
        }
    }

    @Test
    public void checkCaseActivationRejectsInvalidCredentials() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(false);
        mockMvc.perform(post("/citizen/check-case-activation")
                            .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                            .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                            .contentType(APPLICATION_JSON)
                            .content("{\"caseId\":\"1234567890123456\",\"accessCode\":\"ABCD1234\"}"))
            .andExpect(status().isForbidden());
        verifyNoInteractions(linkCitizenCaseService);
    }

    @Test
    public void checkCaseActivationRejectsMalformedRequests() throws Exception {
        for (String body : new String[]{"{}", "{\"caseId\":\"bad\",\"accessCode\":\"ABCD1234\"}",
            "{\"caseId\":\"1234567890123456\",\"accessCode\":\"\"}"}) {
            mockMvc.perform(post("/citizen/check-case-activation")
                                .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                                .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                                .contentType(APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(linkCitizenCaseService);
    }

    @Test
    public void checkCaseActivationRequiresAuthenticationHeaders() throws Exception {
        mockMvc.perform(post("/citizen/check-case-activation").contentType(APPLICATION_JSON)
                            .content("{\"caseId\":\"1234567890123456\",\"accessCode\":\"ABCD1234\"}"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(linkCitizenCaseService);
    }

    @Test
    public void bothLinkEndpointsReturnConflictWhenActivationIsNotAllowed() throws Exception {
        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(linkCitizenCaseService.linkCitizenToCase(anyString(), anyString(), anyString()))
            .thenThrow(new CaseActivationNotAllowedException());
        for (String path : new String[]{"/citizen/link-case-to-account", "/citizen/link-case-to-account-with-hearing"}) {
            mockMvc.perform(post(path)
                                .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                                .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                                .contentType(APPLICATION_JSON)
                                .content("{\"caseId\":\"1234567890123456\",\"accessCode\":\"ABCD1234\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().json("{\"status\":\"NotReady\"}", true));
        }
        verifyNoInteractions(hearingService, caseService);
    }
}
