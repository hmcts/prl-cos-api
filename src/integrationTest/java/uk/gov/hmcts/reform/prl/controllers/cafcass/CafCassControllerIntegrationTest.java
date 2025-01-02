package uk.gov.hmcts.reform.prl.controllers.cafcass;

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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.cafcass.CaseDataService;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.util.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.util.TestConstants.CAFCASS_END_DATE_PARAM;
import static uk.gov.hmcts.reform.prl.util.TestConstants.CAFCASS_END_DATE_PARAM_VALUE;
import static uk.gov.hmcts.reform.prl.util.TestConstants.CAFCASS_START_DATE_PARAM;
import static uk.gov.hmcts.reform.prl.util.TestConstants.CAFCASS_START_DATE_PARAM_VALUE;
import static uk.gov.hmcts.reform.prl.util.TestConstants.SEARCH_CASE_ENDPOINT;
import static uk.gov.hmcts.reform.prl.util.TestConstants.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.util.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.prl.util.TestConstants.TEST_SERVICE_AUTH_TOKEN;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CafCassControllerIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private AuthorisationService authorisationService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    CaseDataService caseDataService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }


    @Test
    public void givenValidDatetimeRangeSearchCasesByCafCassControllerReturnOkStatus() throws Exception {
        Mockito.when(authorisationService.authoriseService(any())).thenReturn(true);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        Mockito.when(authorisationService.authoriseUser(any())).thenReturn(true);
        Mockito.when(caseDataService.getCaseData(anyString(), anyString(), anyString()))
            .thenReturn(CafCassResponse.builder().cases(new ArrayList<>()).build());

        mockMvc.perform(get(SEARCH_CASE_ENDPOINT)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                                                  .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                                                  .queryParam(CAFCASS_START_DATE_PARAM, CAFCASS_START_DATE_PARAM_VALUE)
                                                  .queryParam(CAFCASS_END_DATE_PARAM, CAFCASS_END_DATE_PARAM_VALUE)
                                                  .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }
}
