package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.clients.OrganisationApi;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.cafcass.CaseDataService;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class CafCassControllerFunctionalTest {

    private final String userToken = "Bearer testToken";
    private final String CONTENT_TYPE_JSON = "application/json";

    private final String targetInstance =
            StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:4044"
            );

    private final RequestSpecification request = given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenDatetimeWindow_whenGetRequestToSearchCasesByCafCassController_then200Response() throws Exception {
        request
                .header("Authorization", userToken)
                .contentType(CONTENT_TYPE_JSON)
                .given()
                .pathParams("start_date",
                        "22-02-2022 14:00",
                        "end_date",
                        "22-02-2022 14:15")
                .when()
                .get("/searchCases")
                .then().assertThat().statusCode(200);

    }

    @Test
    public void givenDatetimeWindowSearchCasesByCafCassController_thenCheckMandatoryFields() throws Exception {
        Response response = request
                .header("Authorization", userToken)
                .contentType(CONTENT_TYPE_JSON)
                .given()
                .pathParams("start_date",
                        "22-02-2022 14:00",
                        "end_date",
                        "22-02-2022 14:15")
                .when()
                .get("/searchCases");

        CafCassResponse cafcassResponse = (CafCassResponse) response.getBody();
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        String jsonResponseStr = objectMapper.writeValueAsString(cafcassResponse);
//        assertFalse (cafcassResponse.getCases().isEmpty());
//        assertEquals ("PRLAPPS", cafcassResponse.getCases().get(0).getCaseTypeId());
//        assertNotNull (cafcassResponse.getCases().get(0).getState());
//        assertFalse (cafcassResponse.getCases().get(0).getState().isEmpty());
//        assertNotNull (cafcassResponse.getCases().get(0).getCaseTypeOfApplication());
//        assertFalse (cafcassResponse.getCases().get(0).getCaseData().getChildren().isEmpty());
//        assertFalse (cafcassResponse.getCases().get(0).getCaseData().getApplicants().isEmpty());

        JSONObject json = new JSONObject(cafcassResponse);
        assertNotNull (json.getString("cases.state").toString());
        assertNotNull (json.getString("cases.case_type_id").toString());
        assertNotNull (json.getString("cases.caseTypeofApplication").toString());

    }

    @Test
    public void givenNullDateWindow_whenGetRequestToSearchCasesByCafCassController_then400Response() throws Exception {
        request
                .header("Authorization", userToken)
                .contentType(CONTENT_TYPE_JSON)
                .given()
                .pathParams("start_date",
                        null,
                        "end_date",
                        "22-02-2022 14:15")
                .when()
                .get("/searchCases")
                .then().assertThat().statusCode(400);

    }
}
