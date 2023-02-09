package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@TestPropertySource(
    properties = {
        "idam.client.secret=${CITIZEN_IDAM_CLIENT_SECRET}",
        "idam.client.id=prl-citizen-frontend",
        "idam.s2s-auth.microservice=prl_citizen_frontend"
    }
)
public class CaseControllerFunctionalTest {

    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String CASE_DATA_INPUT = "requests/create-case-valid-casedata-input.json";

    private static final String LINK_CITIZEN_REQUEST_BODY = "requests/link-citizen-case.json";

    private final String targetInstance =
            StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:4044"
            );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @MockBean
    private AuthorisationService authorisationService;

    @MockBean
    private CaseService caseService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
        public void createCaseInCcd() throws Exception {
        String requestBody = ResourceLoader.loadJson(CASE_DATA_INPUT);
        request
                .header("Authorization", idamTokenGenerator.generateIdamTokenForCitizen())
                .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
                .body(requestBody)
                .when()
                .contentType("application/json")
                .post("/case/create")
                .then()
                .assertThat().statusCode(200);
    }


    @Test
    public void testRetrieveCitizenCases() {
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCitizen())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
            .when()
            .contentType("application/json")
            .get("/cases")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void testLinkCitizenToCase() throws Exception {
        String requestBody = ResourceLoader.loadJson(LINK_CITIZEN_REQUEST_BODY);
        when(authorisationService.authoriseService(anyString())).thenReturn(Boolean.TRUE);

        mockMvc.perform(post("/citizen/link")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .header("serviceAuthorization", "auth")
                            .header("accessCode", "auth")
                            .header("caseId", "12345678"))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testLinkCitizenToCaseWith401() throws Exception {
        String requestBody = ResourceLoader.loadJson(LINK_CITIZEN_REQUEST_BODY);
        when(authorisationService.authoriseService(anyString())).thenReturn(Boolean.TRUE);

        mockMvc.perform(post("/citizen/link")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("serviceAuthorization", "auth")
                            .header("accessCode", "auth")
                            .header("caseId", "12345678"))
            .andExpect(status().is4xxClientError())
            .andReturn();
    }

}
