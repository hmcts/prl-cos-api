package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static org.hamcrest.Matchers.equalTo;

public class CallbackControllerFunctionalTest {

    private final String jurisdiction = "PRIVATELAW";
    private final String caseType = "PRLAPPS";
    private final Long caseId = 1234567887654321L;
    private final String eventName = "system-update";
    private final String userToken = "Bearer testToken";
    private final String serviceAuthToken = "Bearer testServiceAuth";
    private final String systemUserId = "systemUserID";
    private final String eventToken = "eventToken";
    private static final String VALID_INPUT_JSON = "CallBackRequest.json";

    @BeforeClass
    public static void setup() throws Exception {
        RestAssured.port = 4044;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    public void givenValidRequest_whenPostReqeuestToMiamValidatation_then200Response() throws Exception {
        RequestSpecification request = RestAssured.given();
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        request
            .header("Authorization", userToken)
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/validate-miam-application-or-exemption")
        .then().assertThat().statusCode(200);
    }


    @Test
    public void givenNoAuthorization_whenPostRequestToDraftDocumentGeneration_then400Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        RequestSpecification request = RestAssured.given();
        request
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/generate-save-draft-document")
            .then().assertThat().statusCode(400);
    }


    @Test
    public void givenRequestWithApplicantOrRespondentCaseName_whenEndPointCalled_ResponseContainsApplicantCaseName() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        RequestSpecification request = RestAssured.given();
        ValidatableResponse response =
            request
                .header("Authorization", userToken)
                .body(requestBody)
                .when()
                .contentType("application/json")
                .post("/copy-FL401-case-name-to-C100")
                .then()
                .body("data.applicantCaseName", equalTo("testString"))
                .assertThat().statusCode(200);
    }

}
