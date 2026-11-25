package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeResponseForCitizen;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
public class FeesAndPaymentControllerFunctionalTest {

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4044"
        );

    private static final String CREATE_PAYMENT_INPUT = "requests/create-payment-input.json";

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    /*
    These test cases will be enabled once we have merged and integrated with Fee and Pay on Demo environment.
     */
    @Test
    public void givenRequestBody_whenGetC100ApplicationFees_then200Response() throws Exception {
        FeeResponseForCitizen response1 = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance)
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("Authorization", idamTokenGenerator.generateIdamTokenForSystem())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generateTokenForCcd())
            .body("")
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .get("/fees-and-payment-apis/getC100ApplicationFees")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(FeeResponseForCitizen.class);

        Assertions.assertNotNull(response1.getAmount());

    }

    @Test
    public void testFetchFee() {
        FeeResponseForCitizen response = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance)
            .header("Content-Type", APPLICATION_JSON_VALUE)
            .header("Accepts", APPLICATION_JSON_VALUE)
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate("prl_citizen_frontend"))
            .body("")
            .when()
            .contentType(APPLICATION_JSON_VALUE)
            .get("/fees-and-payment-apis/getFee/C100_SUBMISSION_FEE")
            .then()
            .assertThat().statusCode(200)
            .extract()
            .as(FeeResponseForCitizen.class);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getAmount());
        Assertions.assertEquals("270.00", response.getAmount());
    }
}
