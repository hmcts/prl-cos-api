package uk.gov.hmcts.reform.prl.clients;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.methods.HttpGet;
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
import uk.gov.hmcts.reform.prl.services.FeeService;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@Ignore
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "FeesRegister-api")
@PactFolder("pacts")
@SpringBootTest({
    "fees-register.api.url : http://fees-register-api-aat.service.core-compute-aat.internal"
})
public class PrePopulateFeeDetailsConsumerTest {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";

    @Autowired
    private FeeService feeService;

    @Autowired
    private FeesRegisterApi feesRegisterApi;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    private static final String SERVICE_AUTH_TOKEN = "TestServiceAuthToken";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException, IOException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "feeRegister_lookUp", consumer = "prl_cos")
    RequestResponsePact getFeeDetails(PactDslWithProvider builder) throws JSONException, IOException {

        Map<String, String> responseheaders = ImmutableMap.<String, String>builder()
            .put("Content-Type", "application/json")
            .build();

        return builder
            .given("A request to get fee amount details")
            .uponReceiving("a request to generate feeamount details")
            .path("/fees-register/fees/lookup")
            .method(HttpMethod.GET.toString())
            .body("channel=default&event=miscellaneous"
                      + "&jurisdiction1=family&jurisdiction2="
                      + "family court&keyword=ChildArrangement"
                      + "&service=private law\n",
                  "application/json")
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .headers(responseheaders)
            .toPact();
    }

    @PactTestFor(pactMethod = "getFeeDetails")
    public void verifyGetUserDetailsDataPact(MockServer mockServer) throws JSONException, IOException {

        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet request = new HttpGet(mockServer.getUrl() + "/fees-register/fees/lookup");
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse getUserDetailsResponse = httpClient.execute(request);

        assertEquals(200, getUserDetailsResponse.getStatusLine().getStatusCode());
    }

}

