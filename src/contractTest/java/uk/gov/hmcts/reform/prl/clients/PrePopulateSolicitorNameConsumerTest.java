package uk.gov.hmcts.reform.prl.clients;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "Idam_api", port = "5000")
@PactFolder("pacts")
@SpringBootTest({
    "auth.idam.client.baseUrl : localhost:5000"
})
public class PrePopulateSolicitorNameConsumerTest {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";

    @Autowired
    private UserService userService;

    @Autowired
    private IdamApi idamApi;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthTokenGenerator serviceTokenGenerator;

    @MockBean
    private AuthorisationService authorisationService;

    private static final String SERVICE_AUTH_TOKEN = "TestServiceAuthToken";
    private static final String INPUT_REQUEST = "<html><body><div>Case Details: {{ caseNo }}</div></body></html>";


    @BeforeEach
    public void setUpEachTest() throws InterruptedException, IOException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "Idam_api", consumer = "prl_cos")
    RequestResponsePact getUserDetails(PactDslWithProvider builder) throws JSONException, IOException {

        return builder
            .given("A request to get solicitor name")
            .uponReceiving("a request to generate solicitor details")
            .path("/details")
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SERVICE_AUTH_TOKEN,
                     HttpHeaders.CONTENT_TYPE, "application/json"
            )

            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "getUserDetails")
    public void verifyGetUserDetailsDataPact(MockServer mockServer) throws JSONException, IOException {

        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpGet request = new HttpGet(mockServer.getUrl() + "/details");
        request.setHeader(HttpHeaders.AUTHORIZATION, SERVICE_AUTH_TOKEN);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        HttpResponse getUserDetailsResponse = httpClient.execute(request);

        assertEquals(200, getUserDetailsResponse.getStatusLine().getStatusCode());
    }

}
