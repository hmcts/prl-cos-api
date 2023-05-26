package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.ResourceLoader;

public class ResetAccessCodeControllerFT {

    private static final String VALID_INPUT_JSON = "controller/valid-request-casedata-body.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenValidRequest_GeneratesAccessCode_Returns200() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        request
            .header("Authorization", "auth")
            .header("ServiceAuthorization", "s2sToken")
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/regenerate-access-code")
            .then()
            .assertThat().statusCode(200);
    }

}
