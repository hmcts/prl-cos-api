package uk.gov.hmcts.reform.prl;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;

/**
 * This will create C100 case in CCD to verify call.
 */
@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CosApiSmokeTests {

    private static final String SEND_AND_REPLY_REQUEST = "requests/send-and-reply-request.json";
    private static final String VALID_INPUT_JSON = "controller/valid-request-casedata-body.json";
    public static final String LOCALHOST_4044 = "http://localhost:4044";
    private static final String MIAM_VALIDATION_REQUEST_ERROR = "requests/call-back-controller-miam-request-error.json";
    private final String userToken = "Bearer testToken";
    private final String s2sToken = "Bearer s2stoken";


    @Autowired
    protected IdamTokenGenerator  idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;


    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            LOCALHOST_4044
        );


    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    @Test
    public void checkSolicitorCanAccessC100MiamExemptionEvent() throws Exception {

        String requestBody = ResourceLoader.loadJson(MIAM_VALIDATION_REQUEST_ERROR);

        request
            .header("Authorization", targetInstance.equalsIgnoreCase(LOCALHOST_4044)
                ? userToken : idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", targetInstance.equalsIgnoreCase(LOCALHOST_4044)
                ? s2sToken : serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/validate-miam-application-or-exemption")
            .then()
            .body("errors",
                  contains(
                      "You cannot make this application unless the applicant has either attended, or is exempt from attending a MIAM")
            )
            .assertThat().statusCode(200);
    }

    @Test
    public void checkSolicitorGetCorrectFeeForC100ApplicationEvent() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        request
            .header("Authorization", targetInstance.equalsIgnoreCase(LOCALHOST_4044)
                ? userToken : idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", targetInstance.equalsIgnoreCase(LOCALHOST_4044)
                ? s2sToken : serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/getSolicitorAndFeeDetails")
            .then()
            .body("errors", contains("Submit and pay is not allowed for this case unless you finish all the mandatory events"))
            .assertThat().statusCode(200);
    }


    @Test
    public void checkSendAndReplyMessageMidEvent() throws Exception {
        String requestBody = ResourceLoader.loadJson(SEND_AND_REPLY_REQUEST);
        request
            .header("Authorization", targetInstance.equalsIgnoreCase(LOCALHOST_4044)
                ? userToken : idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/send-and-reply-to-messages/mid-event")
            .then()
            .body("$", CoreMatchers.not(hasKey("messageReply")))
            .assertThat().statusCode(200);
    }

    @Test
    public void checkSendAndReplyMessageAboutToSubmitEvent() throws Exception {
        String requestBody = ResourceLoader.loadJson(SEND_AND_REPLY_REQUEST);
        request
            .header("Authorization", targetInstance.equalsIgnoreCase(LOCALHOST_4044)
                ? userToken : idamTokenGenerator.generateIdamTokenForSolicitor())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/send-and-reply-to-messages/about-to-submit")
            .then()
            .body("$", CoreMatchers.not(hasKey("openMessages")))
            .assertThat().statusCode(200);

    }
}
