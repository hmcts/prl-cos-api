package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Slf4j
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class CaseWithdrawnRequestControllerFunctionalTest {


    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;


    private static final String VALID_REQUEST_BODY = "requests/call-back-controller.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );
    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenRequestBody_whenIssue_and_send_to_local_court_then200Response() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCafcass())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body(requestBody)
            .when()
            .contentType("application/json")
            .post("/case-withdrawn-email-notification")
            .then()
            .assertThat().statusCode(200)
            .body("confirmation_header", equalTo("# Cais wedi’i dynnu’n ôl <br/> Application withdrawn"),
                  "confirmation_body", notNullValue()
            );

    }

}
