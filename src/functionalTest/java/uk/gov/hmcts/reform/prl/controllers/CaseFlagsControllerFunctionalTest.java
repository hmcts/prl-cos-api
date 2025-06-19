package uk.gov.hmcts.reform.prl.controllers;

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

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CaseFlagsControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    private static final String CASE_FLAG_CONTROLLER_REQUEST_JSON = "requests/case-flag-controller-request.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenBodyWithCaseFlags_whenAboutToStartCallback() throws Exception {
        String requestBody = ResourceLoader.loadJson(CASE_FLAG_CONTROLLER_REQUEST_JSON);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/caseflags/about-to-start")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void givenBodyWithCaseFlags_whenAboutToSubmit() throws Exception {
        String requestBody = ResourceLoader.loadJson(CASE_FLAG_CONTROLLER_REQUEST_JSON);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/caseflags/about-to-submit")
            .then()
            .assertThat().statusCode(200);

    }

    @Test
    public void givenBodyWithCaseFlags_whenSubmitted() throws Exception {
        String requestBody = ResourceLoader.loadJson(CASE_FLAG_CONTROLLER_REQUEST_JSON);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/caseflags/submitted")
            .then()
            .assertThat().statusCode(200);

    }

}
