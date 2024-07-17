package uk.gov.hmcts.reform.prl.controllers.caseaccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class RestrictedCaseAccessControllerFunctionalTest {


    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String VALID_REQUEST_BODY = "requests/restricted-case-access.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenRequestBody_whenAboutToStart_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCourtAdmin())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/restricted-case-access/about-to-start")
            .then()
            .assertThat().statusCode(200)
            .body("errors[0]", equalTo("No one have access to this case right now, "
                                           + "Please provide access to the people with right permissions"));

    }

    @Test
    public void givenRequestBody_whenAboutToSubmit_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCourtAdmin())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/restricted-case-access/about-to-submit")
            .then()
            .assertThat().statusCode(200)
            .body("data.caseSecurityClassification", equalTo("restricted"));

    }

    @Test
    public void givenRequestBody_whenChangeCaseAccess_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCourtAdmin())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/restricted-case-access/change-case-access")
            .then()
            .assertThat().statusCode(500);
    }
}
