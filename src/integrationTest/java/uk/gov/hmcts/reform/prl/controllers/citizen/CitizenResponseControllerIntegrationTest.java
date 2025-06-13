package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.citizen.CitizenResponseService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class CitizenResponseControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    AuthorisationService authorisationService;

    @MockitoBean
    CitizenResponseService citizenResponseService;

    @MockitoBean
    CaseService caseService;

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
    }

    @Test
    public void testGenerateC7Document() throws Exception {
        String url = "/citizen/12345/67890/generate-c7document";
        String jsonRequest = "{\"isWelsh\":true}";

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(citizenResponseService.generateAndReturnDraftC7(anyString(), anyString(), anyString(), anyBoolean()))
            .thenReturn(Document.builder().build());

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header("serviceAuthorization", "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testGenerateC1ADocument() throws Exception {
        String url = "/citizen/12345/67890/generate-c1ADraftDocument";
        String jsonRequest = "{\"isWelsh\":true}";

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(citizenResponseService.generateAndReturnDraftC1A(anyString(), anyString(), anyString(), anyBoolean()))
            .thenReturn(Document.builder().build());

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header("serviceAuthorization", "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testSubmitAndGenerateC7() throws Exception {
        String url = "/citizen/12345/submit-citizen-response";
        String jsonRequest = ResourceLoader.loadJson("requests/citizen-update-case.json");

        when(authorisationService.isAuthorized(anyString(), anyString())).thenReturn(true);
        when(citizenResponseService.generateAndSubmitCitizenResponse(anyString(), anyString(), any()))
            .thenReturn(CaseDetails.builder().build());
        when(caseService.getCaseDataWithHearingResponse(anyString(), anyString(), any()))
            .thenReturn(CaseDataWithHearingResponse.builder().build());

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header("serviceAuthorization", "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();
    }
}
