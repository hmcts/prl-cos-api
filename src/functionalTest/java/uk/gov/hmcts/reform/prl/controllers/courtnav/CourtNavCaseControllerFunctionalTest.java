package uk.gov.hmcts.reform.prl.controllers.courtnav;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.courtnav.mappers.FL401ApplicationMapper;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.courtnav.CourtNavCaseService;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.controllers.ManageOrdersControllerFunctionalTest.VALID_CAFCASS_REQUEST_JSON;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CourtNavCaseControllerFunctionalTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private FL401ApplicationMapper fl401ApplicationMapper;

    @MockitoBean
    protected CourtNavCaseService courtNavCaseServic;

    @MockitoBean
    private AuthorisationService authorisationService;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String VALID_REQUEST_BODY = "requests/courtnav-request.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private static CaseDetails caseDetails;

    @Value("${TEST_URL}")
    protected String cosApiUrl;

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }


    @Test
    public void givenRequestBody_whenCase_then400Response() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_CAFCASS_REQUEST_JSON);

        caseDetails =  request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/testing-support/create-ccd-case-data")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(CaseDetails.class);

        assertNotNull(caseDetails);
        assertNotNull(caseDetails.getId());

        RestAssured.given().relaxedHTTPSValidation().baseUri(cosApiUrl)
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body("")
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .post("/case")
            .then()
            .assertThat().statusCode(400);

    }
}
