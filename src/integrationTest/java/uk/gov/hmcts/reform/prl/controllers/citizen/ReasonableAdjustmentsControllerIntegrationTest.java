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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenCaseUpdateService;

import static org.mockito.ArgumentMatchers.any;
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
public class ReasonableAdjustmentsControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    CaseService caseService;

    @MockBean
    AuthorisationService authorisationService;

    @MockBean
    CitizenCaseUpdateService citizenCaseUpdateService;

    @Autowired
    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    @Test
    public void testPartyUpdateRA() throws Exception {
        String url = "/12345/67890/party-update-ra";
        String jsonRequest = ResourceLoader.loadJson("requests/ra-update-request.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(caseService.updateCitizenRAflags(
            anyString(),
            anyString(),
            anyString(),
            any()
        )).thenReturn(ResponseEntity.ok().build());

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
    public void testRetrieveReasonableAdjustmentFlags() throws Exception {
        String url = "/12345/retrieve-ra-flags/67890";

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(caseService.getPartyCaseFlags(anyString(), anyString(), anyString())).thenReturn(new Flags());

        mockMvc.perform(
                get(url)
                    .header(AUTHORISATION_HEADER, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testLanguageSupportNotes() throws Exception {
        String url = "/12345/language-support-notes";
        String jsonRequest = "{\"languageSupportNotes\":\"test\",\"partyIdamId\":\"test\"}";


        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(citizenCaseUpdateService.addLanguageSupportCaseNotes(anyString(), anyString(), any())).thenReturn(
            ResponseEntity.ok().build());

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
