package uk.gov.hmcts.reform.prl.controllers.reopenclosedcases;

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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;


@Slf4j
@SpringBootTest
@ContextConfiguration
public class ReopenClosedCasesControllerFunctionalTest {

    private static final String VALID_REQUEST_BODY_ABOUT_TO_SUBMIT = "requests/reopenclosedcases/about-to-submit.json";


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
    public void givenRequestBody_aboutToSubmit_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY_ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/reopen-case/about-to-submit")
            .then()
            .assertThat().statusCode(200)
            .body("data.newChildDetails[0].value.finalDecisionResolutionReason", nullValue(),
                  "data.finalCaseClosedDate",
                  nullValue(),
                  "data.caseStatus.state", equalTo("Case Issued")
            )
            .extract()
            .as(AboutToStartOrSubmitCallbackResponse.class);
    }
}
