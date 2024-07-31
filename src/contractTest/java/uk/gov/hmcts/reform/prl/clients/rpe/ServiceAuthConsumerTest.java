package uk.gov.hmcts.reform.prl.clients.rpe;


import au.com.dius.pact.consumer.dsl.PactDslRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(
    properties = {"bundle.api.url=","idam.api.url=","commonData.api.url=",
        "fis_hearing.api.url=",
        "refdata.api.url=",
        "courtfinder.api.url=",
        "prl-dgs-api.url=",
        "fees-register.api.url=",
        "judicialUsers.api.url=",
        "locationfinder.api.url=",
        "rd_professional.api.url=",
        "payments.api.url=",
        "pba.validation.service.api.baseurl=",
        "staffDetails.api.url=",
        "idam.s2s-auth.url=localhost:5000"
    }
)
@EnableAutoConfiguration
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "s2s_auth", port = "5000")
@SpringBootTest
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.authorisation"})
public class ServiceAuthConsumerTest {

    private static final String AUTHORISATION_TOKEN = "Bearer someAuthorisationToken";
    public static final String SOME_MICRO_SERVICE_NAME = "someMicroServiceName";
    public static final String SOME_MICRO_SERVICE_TOKEN = "someMicroServiceToken";

    @Autowired
    private ServiceAuthorisationApi serviceAuthorisationApi;

    @Autowired
    ObjectMapper objectMapper;

    Map<String, String> jsonPayload = new HashMap<>();

    @BeforeEach
    public void setUpTest() {
        jsonPayload.put("microservice", "prl_cos_api");
        // jsonPayload.put("oneTimePassword", "784467");
    }

    @Pact(consumer = "prl_cos",provider = "s2s_auth")
    public V4Pact executeLease(PactDslWithProvider builder) throws JsonProcessingException {

        return builder.given("microservice with valid credentials")
            .uponReceiving("a request for a token")
            .path("/lease")
            .method(HttpMethod.POST.toString())
            .body(buildJsonPayload())
            .willRespondWith()
            .headers(Map.of(HttpHeaders.CONTENT_TYPE, "text/plain"))
            .status(HttpStatus.OK.value())
            .body(PactDslRootValue.stringType(SOME_MICRO_SERVICE_TOKEN))
            .toPact(V4Pact.class);
    }


    @Test
    @PactTestFor(pactMethod = "executeLease")
    void verifyLease() {

        String token = serviceAuthorisationApi.serviceToken(jsonPayload);
        assertThat(token)
            .isEqualTo("someMicroServiceToken");

    }

    private String buildJsonPayload() throws JsonProcessingException {

        return objectMapper.writeValueAsString(jsonPayload);
    }
}
