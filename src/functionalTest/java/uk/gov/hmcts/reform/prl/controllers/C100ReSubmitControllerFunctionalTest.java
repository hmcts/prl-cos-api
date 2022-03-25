package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;

import static org.hamcrest.CoreMatchers.containsString;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class C100ReSubmitControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    private static final String SEND_AND_REPLY_REQUEST = "requests/c100-resubmit-controller.json";


    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenPreviousSubmittedState_thenReturnCaseDataContainsSubmittedState() throws Exception {

        String requestBody = ResourceLoader.loadJson(SEND_AND_REPLY_REQUEST);
        request
            .when()
            .header("Authorization", "auth")
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/resubmit-application")
            .then()
            .body("state", containsString("SUBMITTED_PAID"))
            .statusCode(HttpStatus.OK.value());

    }



}
