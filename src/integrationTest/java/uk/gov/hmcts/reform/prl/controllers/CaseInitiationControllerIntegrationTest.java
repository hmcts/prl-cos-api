package uk.gov.hmcts.reform.prl.controllers;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.caseinitiation.CaseInitiationService;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.util.TestConstants.TEST_AUTH_TOKEN;

@Slf4j
@SpringBootTest
@ExtendWith(SpringRunner.class)
@ContextConfiguration
public class CaseInitiationControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    CaseInitiationService caseInitiationService;

    @MockBean
    AuthorisationService authorisationService;

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testSubmitted() throws Exception {
        String url = "/case-initiation/submitted";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);


        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testPopulateCourtList() throws Exception {
        String url = "/case-initiation/populate-court-list";
        String jsonRequest = ResourceLoader.loadJson("CallbackRequest.json");

        Mockito.when(authorisationService.isAuthorized(any(), any())).thenReturn(true);

        mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}
