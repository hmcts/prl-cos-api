package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.ResourceLoader;

import static org.hamcrest.Matchers.equalTo;

@Slf4j
public class PrePopulateFeeAndSolicitorNameControllerFunctionalTest {

    private static final String VALID_INPUT_JSON = "controller/valid-request-casedata-body.json";

    @BeforeClass
    public static void setup() {
        RestAssured.port = 4044;
        RestAssured.baseURI = "http://localhost";
    }

    @Test
    public void givenValidAuthDetailsAndC100Case_whenEndPointCalled_ResponseContainsFeeInfo() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        RequestSpecification request = RestAssured.given();

        ValidatableResponse response =
            request
                .header("Authorization", "Bearer 1234") //TODO: need real auth token
                .body(requestBody)
                .when()
                .contentType("application/json")
                .post("/getSolicitorAndFeeDetails")
                .then()
                .body("data.feeAmount", equalTo("£232.00"))
                .assertThat().statusCode(200);
    }




}
