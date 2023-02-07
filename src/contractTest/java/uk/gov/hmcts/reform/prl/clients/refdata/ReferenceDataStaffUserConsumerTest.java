package uk.gov.hmcts.reform.prl.clients.refdata;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.clients.StaffResponseDetailsApi;
import uk.gov.hmcts.reform.prl.clients.idam.IdamApiConsumerApplication;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "staffResponseDetailsApi", port = "8899")
@ContextConfiguration(
    classes = {StaffResponseDetailsApiConsumerApplicaton.class, IdamApiConsumerApplication.class}
)
@TestPropertySource(
    properties = {"staffDetails.api.url=http://localhost:8899", "idam.api.url=localhost:5000"}
)
@PactFolder("pacts")
public class ReferenceDataStaffUserConsumerTest {

    @Autowired
    StaffResponseDetailsApi staffResponseDetailsApi;

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    private final String validResponseBody = "gatekeeping/StaffDetailsResponseBody.json";

    @Pact(provider = "staffResponseDetailsApi", consumer = "prl_cos")
    public RequestResponsePact generateStaffUsers(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("Staff User details for given servicename")
            .uponReceiving("A Request to get staff details")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/refdata/internal/staff/usersByServiceName")
            .query("ccd_service_names=PRIVATELAW&sort_column=lastName&sort_direction=ASC")
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(ResourceLoader.loadJson(validResponseBody),"application/json")
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generateStaffUsers")
    public void verifyGetStaffUsers() {

        staffResponseDetailsApi.getAllStaffResponseDetails(
            AUTHORIZATION_TOKEN,
            SERVICE_AUTH_TOKEN,
            "PRIVATELAW","lastName","ASC"
        );
    }

}
