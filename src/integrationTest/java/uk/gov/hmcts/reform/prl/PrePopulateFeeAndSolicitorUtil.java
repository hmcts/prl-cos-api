package uk.gov.hmcts.reform.prl;

import io.restassured.response.Response;

import static net.serenitybdd.rest.SerenityRest.given;

public class PrePopulateFeeAndSolicitorUtil {
    static Response prePopulateFeeAndSolicitorName(final String requestBody, final String prePopulateUri, final String userToken) {
        return given()
            .contentType("application/json")
            .header("Authorization", userToken)
            .body(requestBody)
            .when()
            .post(prePopulateUri)
            .andReturn();
    }
}
