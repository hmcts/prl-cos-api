package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static io.restassured.RestAssured.given;

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
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
    }



    @Test
    public  void testHealthForDgsApi() {

        given().when()
            .contentType("application/json")
            .post("/validate-application-consideration-timetable").then().assertThat().statusCode(200);
    }
}
