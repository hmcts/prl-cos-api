package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.hasKey;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class SendAndReplyControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    private static final String SEND_AND_REPLY_REQUEST_FOR_SEND = "requests/send-and-reply-request.json";

    private static final String SEND_AND_REPLY_REQUEST_FOR_REPLY = "requests/send-and-reply-request-for-reply.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenValidUserDetails_whenAboutToSubmitEndPoint_thenBodyContainsUserEmail() throws Exception {
        String requestBody = ResourceLoader.loadJson(SEND_AND_REPLY_REQUEST_FOR_SEND);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
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
        String requestBody = ResourceLoader.loadJson(SEND_AND_REPLY_REQUEST_FOR_SEND);
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
        String requestBody = ResourceLoader.loadJson(SEND_AND_REPLY_REQUEST_FOR_SEND);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/send-and-reply-to-messages/about-to-submit")
            .then()
            .assertThat().statusCode(200);

    }

    @Ignore
    @Test
    public void givenInValidRequest_whenSubmitted_then500Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(SEND_AND_REPLY_REQUEST_FOR_SEND);
        request
            .header("Authorization", "Bearer 1234")
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/send-and-reply-to-messages/submitted")
            .then()
            .assertThat().statusCode(500);

    }

    @Test
    public void givenBodyWithSendData_whenAboutToStartCallback_thenPopulateDynamicList() throws Exception {
        String requestBody = ResourceLoader.loadJson(SEND_AND_REPLY_REQUEST_FOR_SEND);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/send-and-reply-to-messages/send-or-reply-to-messages/about-to-start")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void givenBodyWithSendData_whenMidEventCallback_thenPopulateDynamicList() throws Exception {
        String requestBody = ResourceLoader.loadJson(SEND_AND_REPLY_REQUEST_FOR_SEND);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/send-and-reply-to-messages/send-or-reply-to-messages/mid-event")
            .then()
            .assertThat().statusCode(200);
    }

    @Test
    public void givenBodyWithNoMessages_whenAboutToSubmitForSendOrReply() throws Exception {
        String requestBody = ResourceLoader.loadJson(SEND_AND_REPLY_REQUEST_FOR_REPLY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/send-and-reply-to-messages/send-or-reply-to-messages/about-to-submit")
            .then()
            .assertThat().statusCode(200);

    }

    @Test
    public void givenBodyWithNoMessages_whenSubmittedForSendOrReply() throws Exception {
        String requestBody = ResourceLoader.loadJson(SEND_AND_REPLY_REQUEST_FOR_REPLY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/send-and-reply-to-messages/send-or-reply-to-messages/submitted")
            .then()
            .assertThat().statusCode(200);

    }

    @Test
    public void givenBodyMessagesWithUnclearedFields_whenClearDynamicListsForSendOrReply() throws Exception {
        String requestBody = ResourceLoader.loadJson(SEND_AND_REPLY_REQUEST_FOR_REPLY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/send-and-reply-to-messages/send-or-reply-to-messages/clear-dynamic-lists")
            .then()
            .assertThat().statusCode(200);

    }

}
