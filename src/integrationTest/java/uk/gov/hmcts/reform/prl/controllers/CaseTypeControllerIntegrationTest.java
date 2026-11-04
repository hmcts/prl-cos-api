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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.GetCaseCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.util.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.prl.util.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@Slf4j
@SpringBootTest(properties = {
    "feature.toggle.caseNameWithPrefixReleaseDate=2026-10-23"
})
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseTypeControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AuthorisationService authorisationService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        objectMapper.registerModule(new ParameterNamesModule());
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
    }

    @Test
    public void testInjectCaseMetadata() throws Exception {
        String url = "/case-type/inject-case-metadata";
        String jsonRequest = ResourceLoader.loadJson("requests/GetCaseCallbackRequest.json");

        MvcResult mvcResult = mockMvc.perform(
                post(url)
                    .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                    .header(SERVICE_AUTHORIZATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON)
                    .content(jsonRequest))
            .andExpect(status().isOk())
            .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        GetCaseCallbackResponse getCaseCallbackResponse = objectMapper.readValue(contentAsString, GetCaseCallbackResponse.class);

        assertEquals(getCaseCallbackResponse.getMetadataFields().size(), 1);
        assertEquals(getCaseCallbackResponse.getMetadataFields().getFirst().getFormattedValue(), "## John Doe Vs Mary Richards v2\n"
            + " ## Casenumber: 1709204361187289");

    }

}
