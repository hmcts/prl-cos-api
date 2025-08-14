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
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        when(linkCitizenCaseService.validateAccessCode(anyString(), anyString())).thenReturn("Valid");

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
}
