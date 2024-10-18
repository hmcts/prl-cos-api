package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
public class FL401SubmitApplicationControllerFT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @MockBean
    private CourtFinderService courtFinderService;

    @MockBean
    private UserService userService;

    @MockBean
    private SolicitorEmailService solicitorEmailService;

    @MockBean
    private CaseWorkerEmailService caseWorkerEmailService;

    private static final String FL401_ABOUT_TO_SUBMIT_CREATION = "requests/fl401-submit-application-controller-validation.json";
    private static final String FL401_GENERATE_DOC_SUBMIT = "requests/fl401-submit-application-controller-generate-doc-submit.json";


    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenMandatoryEventsNotComplete_whenPostRequestMade_then200ResponseAndErrorsPresent() throws Exception {
        String requestBody = ResourceLoader.loadJson(FL401_ABOUT_TO_SUBMIT_CREATION);

        mockMvc.perform(post("/fl401-submit-application-validation")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("errors").isNotEmpty())
            .andReturn();

    }

    @Test
    public void givenFl401Case_whenPostRequestMade_then200ResponseAndNotificationFlagSet() throws Exception {
        String requestBody = ResourceLoader.loadJson(FL401_ABOUT_TO_SUBMIT_CREATION);

        UserDetails userDetails = UserDetails.builder()
            .forename("test")
            .surname("test")
            .build();

        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);

        mockMvc.perform(post("/fl401-submit-application-send-notification")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.isNotificationSent").value("Yes"))
            .andReturn();

    }

}
