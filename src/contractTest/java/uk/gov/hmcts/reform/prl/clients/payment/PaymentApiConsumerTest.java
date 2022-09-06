package uk.gov.hmcts.reform.prl.clients.payment;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.clients.PaymentApi;
import uk.gov.hmcts.reform.prl.models.dto.payment.OnlineCardPaymentRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentResponse;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.ccd.client"})
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "payments", port = "5002")
@TestPropertySource(properties = {"payments.api.url=http://localhost:5002"})
@PactFolder("pacts")
@SpringBootTest
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class PaymentApiConsumerTest {

    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private static final String SERVICE_AUTHORIZATION_HEADER = "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private final OnlineCardPaymentRequest onlineCardPaymentRequest = buildOnlineCardPaymentRequest();
    private static final String PAYMENT_REFERENCE = "RC-1662-3761-4393-1823";
    private static final String DATE_CREATED = "2022-09-05T11:09:04.308+00:00";
    private static final String EXTERNAL_REFERENCE = "csfopuk3a6r0e405cqtl9ef5br";
    private static final String NEXT_URL = "https://www.payments.service.gov.uk/secure/3790460a-5932-4364-bba1-75390b4ec758";
    private static final String STATUS = "Initiated";

    @Autowired
    PaymentApi paymentApi;

    @Pact(provider = "payments", consumer = "prl_cos")
    private RequestResponsePact createPayment(PactDslWithProvider builder) throws JsonProcessingException {
        return builder
            .given("A request to create a payment in payments api")
            .uponReceiving("a request to create a payment in payments api with valid authorization")
            .method("POST")
            .headers("ServiceAuthorization", SERVICE_AUTHORIZATION_HEADER)
            .headers("Authorization", BEARER_TOKEN)
            .headers("Content-Type", "application/json")
            .path("/service-request/2022-1662375472431/card-payments")
            .body(new ObjectMapper().writeValueAsString(onlineCardPaymentRequest), "application/json")
            .willRespondWith()
            .status(HttpStatus.SC_CREATED)
            .body(paymentResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createPayment")
    public void verifyCreatePayment() {
        PaymentResponse paymentResponse = paymentApi.createPaymentRequest("2022-1662375472431", BEARER_TOKEN,
                SERVICE_AUTHORIZATION_HEADER, onlineCardPaymentRequest
        );
        assertNotNull(paymentResponse);
        assertEquals(PAYMENT_REFERENCE, paymentResponse.getPaymentReference());
        assertEquals(STATUS, paymentResponse.getPaymentStatus());
        assertEquals(DATE_CREATED, paymentResponse.getDateCreated());
        assertEquals(EXTERNAL_REFERENCE, paymentResponse.getExternalReference());
        assertEquals(NEXT_URL, paymentResponse.getNextUrl());
    }

    private PactDslJsonBody paymentResponse() {
        return new PactDslJsonBody()
            .stringType("payment_reference", PAYMENT_REFERENCE)
            .stringType("date_created", DATE_CREATED)
            .stringType("external_reference", EXTERNAL_REFERENCE)
            .stringType("next_url", NEXT_URL)
            .stringType("status", STATUS).asBody();
    }

    private OnlineCardPaymentRequest buildOnlineCardPaymentRequest() {
        return OnlineCardPaymentRequest.builder()
                .language("ENGLISH")
                .currency("GBP")
                .amount(BigDecimal.valueOf(100))
                .returnUrl("https://localhost")
        .build();
    }
}
