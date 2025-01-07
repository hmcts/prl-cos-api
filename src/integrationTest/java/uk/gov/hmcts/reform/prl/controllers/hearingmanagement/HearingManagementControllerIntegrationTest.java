package uk.gov.hmcts.reform.prl.controllers.hearingmanagement;

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
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.hearingmanagement.HearingManagementService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class HearingManagementControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AuthorisationService authorisationService;

    @MockBean
    HearingManagementService hearingManagementService;

    @MockBean
    AllTabServiceImpl allTabService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    @Test
    public void testHearingManagementStateUpdate() throws Exception {
        String url = "/hearing-management-state-update/CASE_ISSUED";
        String jsonRequest = ResourceLoader.loadJson("requests/hearing-management-controller.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        Mockito.when(authorisationService.authoriseService(any())).thenReturn(true);
        Mockito.doNothing().when(hearingManagementService).caseStateChangeForHearingManagement(any(), any());

        mockMvc.perform(
                put(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testNextHearingDateUpdate() throws Exception {
        String url = "/hearing-management-next-hearing-date-update";
        String jsonRequest = ResourceLoader.loadJson("requests/hearing-mgmnt-controller-next-hearing-details.json");

        Mockito.when(authorisationService.authoriseUser(any())).thenReturn(true);
        Mockito.when(authorisationService.authoriseService(any())).thenReturn(true);
        Mockito.doNothing().when(hearingManagementService).caseNextHearingDateChangeForHearingManagement(any());

        mockMvc.perform(
                put(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .header("ServiceAuthorization", "testServiceAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testUpdateNextHearingDetailsCallback() throws Exception {
        String url = "/update-next-hearing-details-callback/about-to-submit";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(hearingManagementService.getNextHearingDate(any())).thenReturn(NextHearingDetails.builder().build());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testUpdateAllTabsAfterHmcCaseState() throws Exception {
        String url = "/update-allTabs-after-hmc-case-state/submitted";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Map<String, Object> caseDatMap = new HashMap<>();
        caseDatMap.put("caseId", 123L);

        Mockito.when(allTabService.getStartAllTabsUpdate(any())).thenReturn(new StartAllTabsUpdateDataContent(
            "testAuthToken",
            null,
            null,
            caseDatMap,
            null,
            null
        ));

        mockMvc.perform(
                post(url)
                    .header("Authorization", "Bearer testAuthToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}
