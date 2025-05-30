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
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.handlers.CaseEventHandler;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.ReturnApplicationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class ReturnApplicationReturnMessageControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    UserService userService;

    @MockBean
    ReturnApplicationService returnApplicationService;

    @MockBean
    AllTabServiceImpl allTabsService;

    @MockBean
    AuthorisationService authorisationService;

    @MockBean
    CaseEventHandler caseEventHandler;

    @MockBean
    EventService eventPublisher;

    @Autowired
    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    @Test
    public void testReturnApplicationReturnMessage() throws Exception {
        String url = "/return-application-return-message";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder().build());
        when(returnApplicationService.noRejectReasonSelected(any(CaseData.class))).thenReturn(false);
        when(returnApplicationService.getReturnMessage(any(CaseData.class), any(UserDetails.class))).thenReturn(
            "Return message");

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
    public void testReturnApplicationEmailNotification() throws Exception {
        String url = "/return-application-notification";
        String jsonRequest = ResourceLoader.loadJson("requests/C100-case-data.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(returnApplicationService.updateMiamPolicyUpgradeDataForConfidentialDocument(
            any(CaseData.class),
            anyMap()
        )).thenReturn(CaseData.builder().build());
        when(returnApplicationService.getReturnMessageForTaskList(any(CaseData.class))).thenReturn(
            "Return message for task list");
        when(caseEventHandler.getUpdatedTaskList(any(CaseData.class))).thenReturn("Updated task list");
        when(allTabsService.getAllTabsFields(any(CaseData.class))).thenReturn(new HashMap<>());
        doNothing().when(eventPublisher).publishEvent(any());

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
}
