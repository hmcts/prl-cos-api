package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeResponseForCitizen;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@SpringBootTest
@ContextConfiguration
public class FeesAndPaymentControllerFunctionalTest {

    private static final String CREATE_PAYMENT_INPUT = "requests/create-payment-input.json";

    @Autowired
    private IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Value("${TEST_URL}")
    protected String cosApiUrl;


    /*
    These test cases will be enabled once we have merged and integrated with Fee and Pay on Demo environment.
     */
    @Test
    public void givenRequestBody_whenGetC100ApplicationFees_then200Response() throws Exception {
        FeeResponseForCitizen response1 = RestAssured.given().relaxedHTTPSValidation().baseUri(cosApiUrl)
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

        Assert.assertNotNull(response1.getAmount());

    }
}
