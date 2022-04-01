package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {FeeAndPayServiceRequestControllerIntegrationTest.class, Application.class})
public class FeeAndPayServiceRequestControllerIntegrationTest extends IntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    @Value("${payments.api.url}")
    protected String paymentUrl;

    private final String feeAndPayServiceRequestControllerEndPoint = "/create-payment-service-request";

    private final String path = "CallBackRequest.json";

    @Test
    public void whenInvalidRequestFormat_Return400() throws IOException {

        HttpPost httpPost = new HttpPost(serviceUrl + feeAndPayServiceRequestControllerEndPoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);

        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void return200ForHealthcheckForPaymentApi() throws Exception {

        Response response = SerenityRest.given()
            .when()
            .get(paymentUrl + "/health")
            .andReturn();
        assertEquals(response.getStatusCode(), HttpStatus.SC_OK);

    }
}
