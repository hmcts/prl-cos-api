package uk.gov.hmcts.reform.prl.controllers.citizen;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.utils.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.utils.ServiceAuthenticationGenerator;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class FeesAndPaymentControllerFunctionalTest {

    @Autowired
    protected IdamTokenGenerator idamTokenGenerator;

    @Autowired
    protected ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    private static final String CREATE_PAYMENT_INPUT = "requests/create-payment-input.json";


    private final String targetInstance =
            StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:4044"
            );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void createPaymentTest() throws Exception {
        String requestBody = ResourceLoader.loadJson(CREATE_PAYMENT_INPUT);
        request
                .header("Authorization", idamTokenGenerator.generateIdamTokenForCitizen())
                .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
                .body(requestBody)
                .when()
                .contentType("application/json")
                .post("/fees-and-payment-apis/")
                .then()
                .assertThat().statusCode(200);
    }

    @Test
    public void retrievePaymentStatustest() throws Exception {
        request
            .header("Authorization", idamTokenGenerator.generateIdamTokenForCitizen())
            .header("ServiceAuthorization", serviceAuthenticationGenerator.generate())
            .when()
            .contentType("application/json")
            .post("/fees-and-payment-apis/retrievePaymentStatus/RC-1599-4778-4711-5958/1656350492135029")
            .then()
            .assertThat().statusCode(200);
    }
}
