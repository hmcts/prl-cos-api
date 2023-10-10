package uk.gov.hmcts.reform.prl.clients.sendletter;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.prl.clients.PaymentApi;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.hmcts.reform.sendletter.api.proxy.SendLetterApiProxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.ccd.client"})
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "send-letter-api", port = "5001")
@PactFolder("pacts")
@SpringBootTest
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class SendLetterApiConsumerTest {

    private static final String SERVICE_AUTHORIZATION_HEADER =
        "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhcGlfZ3ciLCJleHAiOjE2OTY5NjIwMDh9.v5OUEVppQT"
            + "qhWPTgoDsAV-34xUY67ksfyJQUz8NJuefy7RSTNxEYwRNm-ivzhIXVUT7wEY6F8SrD1qPQ1LrURQ";

    @Autowired
    SendLetterApiProxy sendLetterApiProxy;

    @Autowired
    IdamApi idamApi;

    private LetterWithPdfsRequest letterWithPdfsRequest;

    public static final String isAsync = "true";

    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String RECIPIENTS = "recipients";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";

    @Autowired
    PaymentApi paymentApi;

    @BeforeEach
    public void beforeEach() throws Exception {
    }

    @AfterEach
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "send-letter_api", consumer = "prl_cos")
    private RequestResponsePact sendLetter(PactDslWithProvider builder) throws JsonProcessingException {
        String letterType = "typeA";
        String caseId = "1658508917240231";

        final Map<String, Object> additionalDataOld = new HashMap<>();
        additionalDataOld.put(LETTER_TYPE_KEY, letterType);
        additionalDataOld.put(CASE_IDENTIFIER_KEY, caseId);
        additionalDataOld.put(CASE_REFERENCE_NUMBER_KEY, caseId);
        additionalDataOld.put(RECIPIENTS, Arrays.asList("recipientName"));
        Map<String,Object> mainMap = new HashMap<>();
        mainMap.put("template","abc");
        Map<String,String> valuesMap = new HashMap<>();
        valuesMap.put("a","b");
        mainMap.put("values",valuesMap);
        List<String> documents = new ArrayList<>();
        documents.add(mainMap.toString());
        Map<String,Object> map3 = new HashMap<>();
        map3.put("recipients",List.of("Joe Bloggins"));


        letterWithPdfsRequest = new LetterWithPdfsRequest(null,letterType,map3);
        return builder
                .given("SendLetter")
                .uponReceiving("A request for send letter")
                .path("/letters")
                .method("POST")
                .headers("ServiceAuthorization", SERVICE_AUTHORIZATION_HEADER)
                .headers("Content-Type", "application/json")
                .matchQuery("isAsync", "true", "true")
                .willRespondWith()
                .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new ObjectMapper().writeValueAsString(letterWithPdfsRequest), "application/json")
                .status(HttpStatus.SC_OK)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "sendLetter")
    public void verifySendLetter() {
        SendLetterResponse sendLetterResponse = sendLetterApiProxy.sendLetter(SERVICE_AUTHORIZATION_HEADER, isAsync, letterWithPdfsRequest);
    }

}
