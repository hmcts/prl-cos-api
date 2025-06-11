package uk.gov.hmcts.reform.prl.controllers.caseflags;

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

import java.util.Base64;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseFlagsControllerFunctionalTest {
    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String VALID_REQUEST_BODY_INITIAL = "requests/caseFlags/review-lang-sm-initial.json";
    private static final String CLIENT_CONTEXT_CASE_NOTE = "requests/caseFlags/client_context.json";
    private static final String VALID_REQUEST_BODY_WITH_FLAGS = "requests/caseFlags/review-lang-sm-with-flags.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenRequestBody_whenAboutToStart_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_INITIAL);
        String client = ResourceLoader.loadJson(CLIENT_CONTEXT_CASE_NOTE);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .header("client-context", Base64.getEncoder().encodeToString(client.getBytes()))
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/review-additional-application/about-to-start")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void givenRequestBody_whenAboutToSubmit_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WITH_FLAGS);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCourtAdmin())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/caseflags/review-lang-sm/about-to-submit")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void givenRequestBody_whenSetUpWaTest_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_WITH_FLAGS);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCourtAdmin())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/caseflags/setup-wa-task")
            .then()
            .assertThat().statusCode(200);
    }
}
