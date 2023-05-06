package uk.gov.hmcts.reform.prl.controllers.hearingmanagement;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.hearingmanagement.HearingManagementService;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class HearingManagementControllerFunctionalTest {

    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;

    private static final String VALID_HEARING_MANAGEMENT_REQUEST_BODY = "requests/hearing-management-controller.json";

    private static final String VALID_NEXT_HEARING_DETAILS_REQUEST_BODY = "requests/hearing-mgmnt-controller-next-hearing-details.json";

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @MockBean
    private HearingManagementService hearingManagementService;

    @MockBean
    private AuthorisationService authorisationService;

    @Test
    public void givenRequestBody_whenHearing_management_state_update_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_HEARING_MANAGEMENT_REQUEST_BODY);
        when(authorisationService.authoriseService(anyString())).thenReturn(Boolean.TRUE);
        mockMvc.perform(put("/hearing-management-state-update/{caseState}","DECISION_OUTCOME")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("serviceAuthorization", "sauth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void givenRequestBody_whenHearing_management_next_hearing_details_update_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_NEXT_HEARING_DETAILS_REQUEST_BODY);
        when(authorisationService.authoriseUser(anyString())).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(anyString())).thenReturn(Boolean.TRUE);
        mockMvc.perform(put("/hearing-management-next-hearing-date-update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("authorization", "auth")
                            .header("serviceAuthorization", "sauth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

}
