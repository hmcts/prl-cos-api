package uk.gov.hmcts.reform.prl.controllers.closingcase;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;


@Slf4j
@SpringBootTest
@ContextConfiguration
public class ClosingCaseControllerFunctionalTest {

    private static final String VALID_REQUEST_BODY_ABOUT_TO_START = "requests/closingcase/about-to-start.json";

    private static final String VALID_REQUEST_BODY_MID_EVENT = "requests/closingcase/mid-event.json";

    private static final String VALID_REQUEST_BODY_VALIDATE_CHILD = "requests/closingcase/validate-child-details.json";

    private static final String VALID_REQUEST_BODY_ABOUT_TO_SUBMIT = "requests/closingcase/about-to-submit.json";


    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;


    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenRequestBody_aboutToStart_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_ABOUT_TO_START);

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/closing-case/pre-populate-child-data")
            .then()
            .assertThat().statusCode(200)
            .body("data.childOptionsForFinalDecision.list_items", notNullValue(),
                  "data.childOptionsForFinalDecision.list_items", hasSize(5)
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
    }

    @Test
    public void givenRequestBody_midEvent_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_MID_EVENT);

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/closing-case/mid-event")
            .then()
            .assertThat().statusCode(200)
            .body("data.finalOutcomeForChildren", notNullValue(),
                  "data.finalOutcomeForChildren", hasSize(1)
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);

    }

    @Test
    public void givenRequestBody_validateChildDetails_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_VALIDATE_CHILD);

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/closing-case/validate-child-details")
            .then()
            .assertThat().statusCode(200)
            .body("errors", empty())
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
    }

    @Test
    public void givenRequestBody_aboutToSubmit_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/closing-case/about-to-submit")
            .then()
            .assertThat().statusCode(200)
            .body("data.newChildDetails[0].value.finalDecisionResolutionReason", notNullValue(),
                  "data.newChildDetails[0].value.finalDecisionResolutionReason",
                  equalTo("Application refused")
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
    }
}
