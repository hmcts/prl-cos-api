package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.CoreMatchers;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasKey;

@Slf4j
public class SendAndReplyControllerFunctionalTest {

    private static final String VALID_INPUT_JSON = "CallBackRequest.json";

    @BeforeClass
    public static void setup() {
        RestAssured.port = 4044;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    public void givenValidUserDetails_whenAboutToSubmitEndPoint_thenBodyContainsUserEmail() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        RequestSpecification request = RestAssured.given();
        ValidatableResponse response =
            request
            .header("Authorization", "Bearer 1234") //TODO: need real auth token
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/send-and-reply-to-messages/about-to-start")
                .then()
                .body("data.messageObject.senderEmail", containsString("fprl_caseworker_solicitor@mailinator.com"))
                .assertThat().statusCode(200);
    }

    @Test
    public void givenBodyWithSendData_whenMidEventCallback_thenMessageReplyNotPopulated() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        RequestSpecification request = RestAssured.given();
        ValidatableResponse response =
            request
                .header("Authorization", "Bearer 1234")
                .body(requestBody)
                .when()
                .contentType("application/json")
                .post("/send-and-reply-to-messages/mid-event")
                .then()
                .body("$", CoreMatchers.not(hasKey("messageReply")))
                .assertThat().statusCode(200);

    }

    @Test
    public void givenBodyWithNoMessages_whenAboutToSubmit_thenResponseContainsNoMessageData() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        RequestSpecification request = RestAssured.given();
        ValidatableResponse response =
            request
                .header("Authorization", "Bearer 1234")
                .body(requestBody)
                .when()
                .contentType("application/json")
                .post("/send-and-reply-to-messages/about-to-submit")
                .then()
                .body("$", CoreMatchers.not(hasKey("openMessages")))
                .assertThat().statusCode(200);

    }

    @Test
    public void givenInValidRequest_whenSubmitted_then500Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        RequestSpecification request = RestAssured.given();
        ValidatableResponse response =
            request
                .header("Authorization", "Bearer 1234")
                .body(requestBody)
                .when()
                .contentType("application/json")
                .post("/send-and-reply-to-messages/submitted")
                .then()
                .assertThat().statusCode(500);

    }

}
