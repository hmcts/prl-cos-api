package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

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

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String CASE_DATA_INPUT = "requests/create-case-valid-casedata-input.json";

    private final String targetInstance =
            StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:4044"
            );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

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
}
