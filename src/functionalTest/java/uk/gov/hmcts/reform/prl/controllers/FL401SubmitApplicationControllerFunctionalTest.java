package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class FL401SubmitApplicationControllerFunctionalTest {

    private final String userToken = "Bearer testToken";

    private static final String VALID_REQUEST_BODY = "controller/valid-request-body.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    @Test
    public void givenNoRequestBodyReturn400FromSubmitApplication() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", userToken)
            .when()
            .contentType("application/json")
            .post("/fl401-submit-application-validation")
            .then()
            .assertThat().statusCode(400);
    }

    @Test
    public void givenNoRequestBodyReturn400FromGenerateDocument() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", userToken)
            .when()
            .contentType("application/json")
            .post("/fl401-generate-document-submit-application")
            .then()
            .assertThat().statusCode(400);
    }

    @Test
    public void givenNoRequestBodyReturn400FromSubmitAndSendNotification() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", userToken)
            .when()
            .contentType("application/json")
            .post("/fl401-submit-application-send-notification")
            .then()
            .assertThat().statusCode(400);
    }

}
