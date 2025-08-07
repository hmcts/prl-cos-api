package uk.gov.hmcts.reform.prl.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.clients.ccd.CaseAssignmentService;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100APPLICANTBARRISTER1;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseAssignmentControllerIntegrationTest {
    private MockMvc mockMvc;

    @MockBean
    private AuthorisationService authorisationService;

    @MockBean
    private OrganisationService organisationService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @SpyBean
    private CaseAssignmentService caseAssignmentService;

    @Autowired
    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.findAndRegisterModules();
    }

    @Test
    public void testAddBarrister() throws Exception {
        String url = "/case-assignment/barrister/add/about-to-submit";
        String jsonRequest = ResourceLoader.loadJson("requests/barrister.json");
        when(organisationService.findUserByEmail(anyString()))
            .thenReturn(Optional.of(UUID.randomUUID().toString()));
        when(authorisationService.isAuthorized(anyString(), anyString()))
            .thenReturn(true);

        doNothing()
            .when(caseAssignmentService).validateAddRequest(any(), any(), any(), any(), any());

        doNothing()
            .when(caseAssignmentService).addBarrister(any(), any(), any(), any());

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").isEmpty())
            .andReturn();
        verify(caseAssignmentService).validateAddRequest(any(), any(), any(), any(), any());
        verify(caseAssignmentService).addBarrister(any(),
                                                   any(),
                                                   eq(C100APPLICANTBARRISTER1.getCaseRoleLabel()),
                                                   any());
    }

    @Test
    public void testRemoveBarrister() throws Exception {
        String url = "/case-assignment/barrister/remove/about-to-submit";
        String jsonRequest = ResourceLoader.loadJson("requests/barrister.json");
        when(authorisationService.isAuthorized(anyString(), anyString()))
            .thenReturn(true);

        doNothing()
            .when(caseAssignmentService).validateRemoveRequest(any(), any(), any());

        doNothing()
            .when(caseAssignmentService).removeBarrister(any(), any());

        mockMvc.perform(
                delete(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.errors").isEmpty())
            .andReturn();
        verify(caseAssignmentService).validateRemoveRequest(any(), any(), any());
        verify(caseAssignmentService).removeBarrister(any(), any());
    }
}
