package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.reform.prl.ResourceLoader;



public class ServiceOfApplicationControllerFT {

    private static final String VALID_REQUEST_BODY = "requests/manage-order-fetch-children-request.json";


    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenRequestWithCaseData_ResponseContainsHeaderAndCollapsable() throws Exception {

        final String userToken = "Bearer testToken";

        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", userToken)
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/service-of-application/about-to-start")
            .then()
            .assertThat().statusCode(200);
    }
}
