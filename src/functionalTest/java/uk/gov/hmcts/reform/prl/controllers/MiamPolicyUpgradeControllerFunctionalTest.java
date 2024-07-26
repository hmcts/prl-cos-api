package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
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
public class MiamPolicyUpgradeControllerFunctionalTest {

    private MockMvc mockMvc;

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private AuthorisationService authorisationService;

    private static final String USERTOKEN = "Bearer testToken";

    private static final String VALID_MIAM_REQUEST_JSON
        = "requests/miam-policy-upgrade.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenNoBodyWhenAboutToSubmitForMiamPolicyUpgradeReturns400() {
        request
            .header("Authorization", USERTOKEN)
            .header("ServiceAuthorization", "s2sToken")
            .when()
            .contentType("application/json")
            .post("/submit-miam-policy-upgrade")
            .then()
            .assertThat().statusCode(400);
    }

    @Test
    public void givenBodyWhenAboutToSubmitForMiamPolicyUpgradeReturns200() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_MIAM_REQUEST_JSON);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        mockMvc.perform(post("/submit-miam-policy-upgrade")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
                .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
                .content(requestBody)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.mpuClaimingExemptionMiam").value("Yes"))
            .andExpect(jsonPath("data.mpuApplicantAttendedMiam").value("No"))
            .andReturn();
    }
}
