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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    HearingService hearingService;

    @MockBean
    CaseService caseService;

    @MockBean
    AuthorisationService authorisationService;

    @MockBean
    ConfidentialDetailsMapper confidentialDetailsMapper;

    @MockBean
    AuthTokenGenerator authTokenGenerator;

    @Autowired
    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    @Test
    public void testRetrieveCaseById() throws Exception {
        String url = "/{caseId}";
        String caseId = "12345";

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(caseService.getCase(anyString(), anyString())).thenReturn(CaseDetails.builder()
                                                                           .id(2344L)
                                                                           .state("state")
                                                                           .createdDate(LocalDateTime.now())
                                                                           .lastModified(LocalDateTime.now())
                                                                           .data(new HashMap<>())
                                                                           .build());

        mockMvc.perform(
                get(url, caseId)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testRetrieveCaseAndHearing() throws Exception {
        String url = "/retrieve-case-and-hearing/{caseId}/{hearingNeeded}";
        String caseId = "12345";
        boolean hearingNeeded = true;

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(
                get(url, caseId, hearingNeeded)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testUpdateCase() throws Exception {
        String url = "/{caseId}/{eventId}/update-case";
        String caseId = "12345";
        String eventId = "event123";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(caseService.updateCase(any(), anyString(), anyString(), anyString())).thenReturn(CaseDetails.builder()
                                                                                                  .id(2344L)
                                                                                                  .state("state")
                                                                                                  .createdDate(
                                                                                                      LocalDateTime.now())
                                                                                                  .lastModified(
                                                                                                      LocalDateTime.now())
                                                                                                  .data(new HashMap<>())
                                                                                                  .build());
        when(confidentialDetailsMapper.mapConfidentialData(any(), anyBoolean())).thenReturn(CaseData.builder().build());

        mockMvc.perform(
                post(url, caseId, eventId)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .header("accessCode", "testAccessCode")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testRetrieveCasesForCitizen() throws Exception {
        String url = "/citizen/{role}/retrieve-cases/{userId}";
        String role = "citizen";
        String userId = "user123";

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(caseService.retrieveCases(anyString(), anyString())).thenReturn(new ArrayList<>());

        mockMvc.perform(
                get(url, role, userId)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testRetrieveCitizenCases() throws Exception {
        String url = "/cases";

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(caseService.retrieveCases(anyString(), anyString())).thenReturn(new ArrayList<>());

        mockMvc.perform(
                get(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testCreateCase() throws Exception {
        String url = "/case/create";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(caseService.createCase(any(), anyString())).thenReturn(CaseDetails.builder()
                                                                        .id(2344L)
                                                                        .state("state")
                                                                        .createdDate(LocalDateTime.now())
                                                                        .lastModified(LocalDateTime.now())
                                                                        .data(new HashMap<>())
                                                                        .build());

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testGetAllHearingsForCitizenCase() throws Exception {
        String url = "/hearing/{caseId}";
        String caseId = "12345";

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(
                post(url, caseId)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testFetchIdamAmRoles() throws Exception {
        String url = "/fetchIdam-Am-roles/{emailId}";
        String emailId = "test@example.com";

        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(caseService.fetchIdamAmRoles(anyString(), anyString())).thenReturn(new HashMap<>());

        mockMvc.perform(
                get(url, emailId)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }
}
