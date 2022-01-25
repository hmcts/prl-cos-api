package uk.gov.hmcts.reform.prl;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class DocumentGenerateUtil {

    static Response documentGenerate(final String requestBody, final String generateDocUri, final String userToken) {
        return given()
            .contentType("application/json")
            .header("Authorization", userToken)
            .body(requestBody)
            .when()
            .post(generateDocUri)
            .andReturn();
    }
}
