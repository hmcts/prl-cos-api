package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Mock
    private ObjectMapper objectMapper;

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

    private static CaseData caseData;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Ignore
    @Test
    public void createCaseInCcd() throws Exception {
        String requestBody = ResourceLoader.loadJson(CASE_DATA_INPUT);
        request
            .header("Authorization", "authToken")
            .header("ServiceAuthorization", "s2sAuthToken")
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/case/create")
            .then()
            .assertThat().statusCode(200);
    }

    @Ignore
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

    @Ignore
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




    @Ignore("as there is no end point existing with this link")
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

    @Test
    public void retrieveCitizenFlagsSuccessResponse() throws Exception {
        mockMvc.perform(get("/1234567/retrieve-ra-flags/party-1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", idamTokenGenerator.generateIdamTokenForCitizen())
                            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void updateCitizenFlagsSuccessResponse() throws Exception {
        String requestBody = ResourceLoader.loadJson("requests/ra-update-request.json");
        when(authorisationService.authoriseService(anyString())).thenReturn(Boolean.TRUE);

        mockMvc.perform(post("1234567/c100RequestSupport/party-update-ra")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .header("serviceAuthorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testCreateDummyCase() {
        caseData = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCitizen())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .when()
            .contentType("application/json")
            .post("/testing-support/create-dummy-citizen-case")
            .then()
            .extract()
            .as(CaseData.class);
        Assert.assertNotNull(caseData);
        Assert.assertNotNull(caseData.getId());
    }

    @Test
    public void testUpdateCaseWithOtherPersonDetails() {

        CaseData responseData = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCitizen())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .header("accessCode", " ")
            .body(caseData)
            .when()
            .contentType("application/json")
            .post(caseData.getId() + "/citizen-case-submit/update-case")
            .then()
            .extract()
            .as(CaseData.class);

        Assert.assertNotNull(responseData);
        Assert.assertNotNull(responseData.getOtherPartyInTheCaseRevised());
        Assert.assertNotNull(responseData.getOtherPartyInTheCaseRevised().get(0));
        Assert.assertEquals("Andrew",responseData.getOtherPartyInTheCaseRevised().get(0).getValue().getFirstName());
        Assert.assertEquals("Smith",responseData.getOtherPartyInTheCaseRevised().get(0).getValue().getLastName());
    }
}
