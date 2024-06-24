package uk.gov.hmcts.reform.prl.clients.sendletter;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.clients.idam.IdamApiConsumerApplication;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;
import uk.gov.hmcts.reform.sendletter.api.proxy.SendLetterApiProxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "sendLetter_lookUp", port = "8881")
@ContextConfiguration(
    classes = {SendLetterApiConsumerApplication.class, IdamApiConsumerApplication.class}
)
@TestPropertySource(
    properties = {"bundle.api.url=http://localhost:8899","idam.api.url=localhost:5000","commonData.api.url=localhost:5000",
        "fis_hearing.api.url=localhost:5000",
        "refdata.api.url=",
        "courtfinder.api.url=",
        "prl-dgs-api.url=",
        "fees-register.api.url=",
        "send-letter.url=http://localhost:8881",
        "fis_hearing.api.url=",
        "judicialUsers.api.url=",
        "locationfinder.api.url=",
        "rd_professional.api.url=",
        "payments.api.url=",
        "pba.validation.service.api.baseurl=",
        "staffDetails.api.url=",
        "amRoleAssignment.api.url=",
        "core_case_data.api.url="
    }
)

@PactFolder("pacts")
public class SendLetterApiConsumerTest {

    @Autowired
    SendLetterApiProxy sendLetterApiProxy;

    private LetterWithPdfsRequest letterWithPdfsRequest;

    public static final String isAsync = "true";

    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String RECIPIENTS = "recipients";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";
    private static final String SERVICE_AUTHORIZATION_HEADER = "test";

    private static final String VALID_REQUEST_BODY_DOC = "requests/send-letter-request-body.json";

    private final String validResponseBody = "response/send-letter-response.json";

    @Pact(provider = "sendLetter_lookUp", consumer = "prl_cos")
    private RequestResponsePact generateSendLetterPact(PactDslWithProvider builder) throws Exception {

        String caseId = "1697022507599892";

        final Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(LETTER_TYPE_KEY, "ApplicationPack");
        additionalData.put(CASE_IDENTIFIER_KEY, caseId);
        additionalData.put(CASE_REFERENCE_NUMBER_KEY, caseId);
        additionalData.put(RECIPIENTS, Arrays.asList("Mary Richards"));

        List<String> documents = new ArrayList<>();
        String documentInBytes = ResourceLoader.loadJson(VALID_REQUEST_BODY_DOC);
        documents.add(documentInBytes.trim());

        letterWithPdfsRequest = new LetterWithPdfsRequest(documents,"ApplicationPack",additionalData);

        return builder
            .given("SendLetter")
            .uponReceiving("A request for send letter")
            .path("/letters")
            .method("POST")
            .headers("ServiceAuthorization", SERVICE_AUTHORIZATION_HEADER)
            .headers("Content-Type", "application/vnd.uk.gov.hmcts.letter-service.in.letter.v2+json")
            .matchQuery("isAsync", "true", "true")
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(new ObjectMapper().writeValueAsString(letterWithPdfsRequest), "application/json")
            .status(HttpStatus.SC_OK)
            .body(ResourceLoader.loadJson(validResponseBody),"application/json")
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generateSendLetterPact")
    public void verifySendLetterPact() {
        SendLetterResponse sendLetterResponse = sendLetterApiProxy.sendLetter(SERVICE_AUTHORIZATION_HEADER, isAsync, letterWithPdfsRequest);
        assertNotNull(sendLetterResponse);
        assertEquals("24ab9066-a0d9-409e-862d-164ff6eba545",sendLetterResponse.letterId.toString());
    }
}
