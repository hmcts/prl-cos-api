package uk.gov.hmcts.reform.prl.controllers.citizen;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.util.IdamTokenGenerator;
import uk.gov.hmcts.reform.prl.util.ServiceAuthenticationGenerator;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.junit.Assert.assertEquals;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(classes = {Application.class, FeesAndPaymentControllerIntegrationTest.class})
public class FeesAndPaymentControllerIntegrationTest extends IntegrationTest {

    @Value("${case.orchestration.service.base.uri}")
    protected String serviceUrl;

    private final String createPaymentEndpoint = "/fees-and-payment-apis/create-payment";

    private final String getPaymentStatusEndpoint = "/fees-and-payment-apis/retrievePaymentStatus/RC-1599-4778-4711-5958/1656350492135029";

    private final String validBody = "requests/create-payment-input.json";

    @Value("${payments.api.url}")
    protected String paymentUrl;

    @Autowired
    IdamTokenGenerator idamTokenGenerator;

    @Autowired
    ServiceAuthenticationGenerator serviceAuthenticationGenerator;

    @Test
    public void testCreatePaymentEndpoint() throws Exception {
        String requestBody = ResourceLoader.loadJson(validBody);
        HttpPost httpPost = new HttpPost(serviceUrl + createPaymentEndpoint);
        httpPost.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpPost.addHeader("ServiceAuthorization", serviceAuthenticationGenerator.generate());
        httpPost.addHeader(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem());
        StringEntity body = new StringEntity(requestBody);
        httpPost.setEntity(body);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpPost);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testRetrievePaymentStatus() throws Exception {
        HttpGet httpGet = new HttpGet(serviceUrl +  getPaymentStatusEndpoint);
        httpGet.addHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        httpGet.addHeader("ServiceAuthorization", serviceAuthenticationGenerator.generate());
        httpGet.addHeader(AUTHORIZATION, idamTokenGenerator.generateIdamTokenForSystem());
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpGet);
        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());

    }
}
