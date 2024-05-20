package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class DraftOrdersControllerFunctionalTest {


    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String VALID_REQUEST_BODY = "requests/call-back-controller.json";
    private static final String VALID_DRAFT_ORDER_REQUEST_BODY = "requests/draft-order-sdo-with-options-request.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenRequestBody_whenReset_fields_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/reset-fields")
            .then()
            .assertThat().statusCode(200)
            .body("data.caseTypeOfApplication", equalTo("C100"));

    }

    @Test
    public void givenRequestBody_whenSelected_order_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/selected-order")
            .then()
            .assertThat().statusCode(200)
            .body("errors[0]", equalTo("This order is not available to be drafted"));

    }

    @Test
    public void givenRequestBody_whenPopulate_draft_order_fields_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/populate-draft-order-fields")
            .then()
            .assertThat().statusCode(200)
            .body("data.caseTypeOfApplication", equalTo("FL401"),
                  "data.ordersHearingDetails[0].value.hearingTypes.value", notNullValue(),
                  "data.draftOrderCollection[0].id", notNullValue(),
                  "data.draftOrdersDynamicList", notNullValue()
                  );

    }

    @Test
    public void givenRequestBody_whenPopulate_standard_direction_order_fields() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/populate-standard-direction-order-fields")
            .then()
            .assertThat().statusCode(200)
            .body("data.caseTypeOfApplication", equalTo("FL401"),
                  "data.draftOrderCollection[0].id", notNullValue());

    }

    @Test
    public void givenRequestBody_whenAbout_to_submit() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/about-to-submit")
            .then()
            .assertThat().statusCode(200)
            .body("data.caseTypeOfApplication", equalTo("FL401"),
                  "data.caseStatus.state", equalTo("Draft"));

    }

    @Test
    public void givenRequestBody_whenGenerate_doc() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/generate-doc")
            .then()
            .assertThat().statusCode(200)
            .body("errors", nullValue(),
                  "data.caseTypeOfApplication", equalTo("FL401"));

    }


    /**
     * When selected order id for DA case is 'FL402'.
     * then error should be 'This order is not available to be drafted'.
     */
    @Test
    public void givenRequestBody_whenFl402OrderSelectedForDA_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace("\"createSelectOrderOptions\": \"standardDirectionsOrder\"",
                     "\"createSelectOrderOptions\": \"noticeOfProceedings\"");

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/selected-order")
            .then()
            .body("errors[0]", equalTo("This order is not available to be drafted"))
            .assertThat().statusCode(200);

    }

    /**
     * When selected order id for CA case is 'Fl402'.
     * then error should be 'This order is not available to be drafted'.
     */
    @Test
    public void givenRequestBody_whenFl402OrderSelectedForCA_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace(
                "\"createSelectOrderOptions\": \"standardDirectionsOrder\"",
                "\"createSelectOrderOptions\": \"noticeOfProceedings\""
            )
            .replace(
                "\"caseTypeOfApplication\": \"FL401\"",
                      "\"caseTypeOfApplication\": \"C100\"");

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/selected-order")
            .then()
            .body("errors[0]", equalTo("This order is not available to be drafted"))
            .assertThat().statusCode(200);

    }

    /**
     * When selected order id for CA case is 'NoticeOfProceeding'.
     * then error msg should be 'This order is not available to be drafted'.
     */
    @Test
    public void givenRequestBody_whenNoticeOfProceedingSelectedForCA_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace(
                "\"createSelectOrderOptions\": \"standardDirectionsOrder\"",
                "\"createSelectOrderOptions\": \"noticeOfProceedingsParties\""
            )
            .replace(
                "\"caseTypeOfApplication\": \"FL401\"",
                "\"caseTypeOfApplication\": \"C100\"");

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/selected-order")
            .then()
            .body("errors[0]", equalTo("This order is not available to be drafted"))
            .assertThat().statusCode(200);

    }

    /**
     * When selected order id for CA case is 'noticeOfProceedingsNonParties'.
     * then error msg should be 'This order is not available to be drafted'.
     */
    @Test
    public void givenRequestBody_whenNoticeOfProceedingNonPartiesSelectedForCA_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_DRAFT_ORDER_REQUEST_BODY);

        String requestBodyRevised = requestBody
            .replace(
                "\"createSelectOrderOptions\": \"standardDirectionsOrder\"",
                "\"createSelectOrderOptions\": \"noticeOfProceedingsNonParties\""
            )
            .replace(
                "\"caseTypeOfApplication\": \"FL401\"",
                "\"caseTypeOfApplication\": \"C100\"");

        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSolicitor())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBodyRevised)
            .when()
            .contentType("application/json")
            .post("/selected-order")
            .then()
            .body("errors[0]", equalTo("This order is not available to be drafted"))
            .assertThat().statusCode(200);

    }



}
