package uk.gov.hmcts.reform.prl.clients;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.junit.After;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;

import java.io.IOException;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.Assert.assertEquals;

@Ignore
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "Payment-api", port = "")
@PactFolder("pacts")
@SpringBootTest({
    "payments.api.url : http://payment-api-demo.service.core-compute-demo.internal/"
})
public class PaymentServiceRequestConsumerTest {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";

    @Autowired
    private PaymentRequestService paymentRequestService;

    @Autowired
    private PaymentApi paymentApi;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    private static final String SERVICE_AUTH_TOKEN = "TestServiceAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException, IOException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "Payment_ServiceRequest", consumer = "prl_cos")
    RequestResponsePact getFeeDetails(PactDslWithProvider builder) throws JSONException, IOException {

        return builder
            .given("A request to get service request details")
            .uponReceiving("a request to generate service request reference number details")
            .path("/service-request")
            .method(HttpMethod.POST.toString())
            .headers(
                HttpHeaders.AUTHORIZATION, SERVICE_AUTH_TOKEN,
                SERVICE_AUTHORIZATION, SOME_SERVICE_AUTHORIZATION_TOKEN
            )
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(buildPaymentDtoPactDsl())
            .toPact();
    }

    private DslPart buildPaymentDtoPactDsl() {
        return newJsonBody((o) -> {
            o.stringType("call_back_url", "https://manage-case.demo.platform.hmcts.net/cases")
                .minArrayLike("case_payment_request", 0, 1, case_payment_req -> case_payment_req.stringType("action", "payment")
                    .stringType("responsible_party", "test vs test12"))
                .stringType("case_reference", "string")
                .stringType("ccd_case_number", "1637145859478895")
                .minArrayLike("fees", 0, 1, fee -> fee.decimalType("calculated_amount", 232.00)
                    .stringType("code", "FEE0325")
                    .stringType("version", "1")
                    .numberType("volume", 1))
                .stringType("hmcts_org_id", "ABA5");

        }).build();
    }


    @PactTestFor(pactMethod = "getServiceRequest")
    public void verifyPaymentServiceRequestDataPact(MockServer mockServer) throws JSONException, IOException {

        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost request = new HttpPost(mockServer.getUrl() + "/service-request");
        request.addHeader(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN);
        request.addHeader("content-type", "application/vnd.uk.gov.hmcts.pdf-service.v2+json;charset=UTF-8");
        request.setEntity((HttpEntity) buildPaymentDtoPactDsl());

        HttpResponse getUserDetailsResponse = httpClient.execute(request);

        assertEquals(200, getUserDetailsResponse.getStatusLine().getStatusCode());
    }

}

