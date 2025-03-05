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
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;

import static org.mockito.ArgumentMatchers.any;
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
public class ResetAccessCodeControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    CaseInviteManager caseInviteManager;

    @MockBean
    AuthorisationService authorisationService;

    @Autowired
    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }


    @Test
    public void testResetAccessCode() throws Exception {
        String url = "/regenerate-access-code";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(caseInviteManager.reGeneratePinAndSendNotificationEmail(any())).thenReturn(CaseData.builder().build());

        mockMvc.perform(
                post(url)
                    .header("Authorization", "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

}
