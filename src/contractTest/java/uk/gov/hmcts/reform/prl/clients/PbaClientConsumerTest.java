package uk.gov.hmcts.reform.prl.clients;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.json.JSONException;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.models.dto.payment.PbaOrganisationResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "referenceData_organisationalExternalPbas", port = "8892")
@PactFolder("pacts")
@SpringBootTest({
    "pba.validation.service.api.baseurl : localhost:8892"
})
public class PbaClientConsumerTest {

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    public static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";
    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    @Autowired
    private PbaValidationClient pbaValidationClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorisationService authorisationService;

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "referenceData_organisationalExternalPbas", consumer = "prl_cos")
    RequestResponsePact getOrganisationalPbasReferenceData(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("Pbas organisational data exists for identifier " + ORGANISATION_EMAIL)
            .uponReceiving("a request for information for that organisation's pbas")
            .path("/refdata/external/v1/organisations/pbas")
            .method("GET")
            .headers(
                HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN,
                SERVICE_AUTHORIZATION, SOME_SERVICE_AUTHORIZATION_TOKEN,
                "UserEmail", ORGANISATION_EMAIL
            ).willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(buildOrganisationalResponsePactDsl())
            .toPact();
    }

    private DslPart buildOrganisationalResponsePactDsl() {
        return newJsonBody((o) -> {
            o.object("organisationEntityResponse", ob -> ob
                .stringType(
                    "organisationIdentifier",
                    ORGANISATION_EMAIL
                )
                .stringMatcher("status",
                               "PENDING|ACTIVE|BLOCKED|DELETED", "ACTIVE"
                )
                .stringType("sraId", "sraId")
                .booleanType("sraRegulated", true)
                .stringType("companyNumber", "123456")
                .stringType("companyUrl", "somecompany@org.com")
                .array("paymentAccount", pa ->
                    pa.stringType("paymentAccountA1"))
                .object("superUser", su -> su
                    .stringType("firstName", "firstName")
                    .stringType("lastName", "lastName")
                    .stringType("email", "emailAddress"))
            );
        }).build();
    }

    @Test
    @PactTestFor(pactMethod = "getOrganisationalPbasReferenceData")
    public void verifyGetOrganisationalPbasReferenceDataPact() throws JSONException {

        ResponseEntity<PbaOrganisationResponse> response = pbaValidationClient
            .retrievePbaNumbers(
                SOME_AUTHORIZATION_TOKEN,
                SOME_SERVICE_AUTHORIZATION_TOKEN,
                ORGANISATION_EMAIL
            );

        assertThat(
            response.getBody().getOrganisationEntityResponse().getOrganisationIdentifier(),
            equalTo(ORGANISATION_EMAIL)
        );
    }
}
