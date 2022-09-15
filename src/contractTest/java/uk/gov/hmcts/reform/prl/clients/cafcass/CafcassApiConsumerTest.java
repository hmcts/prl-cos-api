package uk.gov.hmcts.reform.prl.clients.cafcass;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.client.fluent.Executor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.idam.IdamApiConsumerApplication;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseData;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "cafcass")
@PactFolder("pacts")
@ContextConfiguration(
        classes = {CafcassSearchCaseApiConsumerApplication.class, IdamApiConsumerApplication.class}
)
public class CafcassApiConsumerTest {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_TEST_TOKEN = "Bearer some-access-token";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String SERVICE_AUTH_TEST_TOKEN = "someServiceAuthToken";
    private static final String SEARCH_URL = "/searchCases";

    @BeforeEach
    public void setupEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void tearDown() {
        Executor.closeIdleConnections();
    }

    //GET
    @Pact(provider = "cafcass", consumer = "prl_cos")
    public RequestResponsePact executeGetSearchCases(PactDslWithProvider builder) {
        return
                builder
                        .given("Cases exist for Cafcass")
                        .uponReceiving("A request for Cases")
                        .path("/searchCases")
                        .method("GET")
                        .matchQuery("channel", "default", "default")
                        .matchQuery("event", "miscellaneous", "miscellaneous")
                        .matchQuery("jurisdiction1", "family", "family")
                        .matchQuery("jurisdiction2", "family court", "family court")
                        .matchQuery("service", "private law", "private law")
                        .willRespondWith()
                        .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .headers(getResponseHeaders())
                        .body("Body of response?")
                        .status(HttpStatus.OK.value())
                        .toPact();

    }

    private PactDslJsonBody buildSearchCaseResponseDsl(Integer code, String cases) {
        return new PactDslJsonBody()
                .integerType("total", code)
                .stringType("cases", cases);
    }

    @NotNull
    private Map<String, String> getResponseHeaders() {
        Map<String, String> responseHeaders = new HashMap<>();

        responseHeaders.put("Content-Type", "application/json");

        return responseHeaders;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();

        headers.add("authorisation", "Bearer TestServiceAuthorisationToken");
        headers.add("serviceauthorisation", "TestServiceAuthorisationToken");

        return headers;
    }
}
