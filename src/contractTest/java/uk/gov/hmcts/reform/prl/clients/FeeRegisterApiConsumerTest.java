package uk.gov.hmcts.reform.prl.clients;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.config.FeesConfig;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.services.FeeService;

import static org.junit.Assert.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "feeRegister_lookUp", port = "8889")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {FeesRegisterApiConsumerApplication.class, FeeService.class, FeesConfig.class})
@TestPropertySource(
    properties = {"fees-register.api.url=localhost:8889", "payments.api.url=localhost:8889",
        "idam.api.url=localhost:5000"}
)
@PactFolder("pacts")
public class FeeRegisterApiConsumerTest {

    @Autowired
    FeesRegisterApi feesRegisterApi;

    @Autowired
    FeeService feeService;

    public static final String CHANNEL = "default";
    public static final String MISC_EVENT = "miscellaneous";
    public static final String JURISDICTION_1 = "family";
    public static final String JURISDICTION_2 = "family court";
    public static final String PRIVATE_LAW_SERVICE = "private law";
    public static final String OTHER_KEYWORD = "ChildArrangement";

    @Pact(provider = "feeRegister_lookUp", consumer = "prl_cos")
    private RequestResponsePact getFeeDetails(PactDslWithProvider builder) throws JSONException {
        return getRequestResponsePact(builder,
                                      CHANNEL,
                                      MISC_EVENT,
                                "FEE0325",
            "Variation or discharge etc of care and supervision orders (section 39)",
                                      232.00,
                                      OTHER_KEYWORD);
    }

    private RequestResponsePact getRequestResponsePact(PactDslWithProvider builder, String keyword, String service,
                                                       String code, String description,
                                                       double feeAmount, String event) {
        return builder
            .given("Fees exist for CCD")
            .uponReceiving(String.format("a request for '%s' CCD fee", keyword))
            .path("/fees-register/fees/lookup")
            .method("GET")
            .matchQuery("service", service, service)
            .matchQuery("jurisdiction1", "family", "family")
            .matchQuery("jurisdiction2", "family court", "family court")
            .matchQuery("channel", "default", "default")
            .matchQuery("event", event, event)
            .matchQuery("keyword", keyword, keyword)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(buildFeesResponseBodyDsl(code, description, feeAmount))
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    private PactDslJsonBody buildFeesResponseBodyDsl(String code, String description, double feeAmount) {
        return new PactDslJsonBody()
            .stringType("code",code)
            .stringType("description", description)
            .numberType("version", 1)
            .decimalType("fee_amount", feeAmount);
    }

    @Test
    @PactTestFor(pactMethod = "getFeeDetails")
    public void verifyOtherFeesServicePact() {
        FeeResponse feeResponse = feesRegisterApi.findFee(
            CHANNEL, MISC_EVENT, JURISDICTION_1, JURISDICTION_2, OTHER_KEYWORD, PRIVATE_LAW_SERVICE
        );
        assertEquals("FEE0325", feeResponse.getCode());
    }
}
