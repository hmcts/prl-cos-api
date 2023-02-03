package uk.gov.hmcts.reform.prl.clients.refdata;
/*
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.prl.clients.RefDataUserApi;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@PactTestFor(providerName = "referenceData_StaffUsersInternal", port = "8894")
@TestPropertySource(
    properties = {"staffDetails.api.url=localhost:8894", "idam.api.url=localhost:5000"}
)
public class ReferenceDataStaffUserConsumerTest {

    @Autowired
    RefDataUserApi staffResponseDetailsApi;

    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";

    @Pact(provider = "referenceData_organisationalInternal", consumer = "fpl_ccdConfiguration")
    public RequestResponsePact generateStaffUsers(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("Organisation exists for given Id")
            .uponReceiving("A Request to get organisation by Id")
            .method("GET")
            .headers("ServiceAuthorization", SERVICE_AUTHORIZATION_HEADER)
            .headers("Authorization", AUTHORIZATION_TOKEN)
            .headers("Content-Type", "application/json")
            .path("/refdata/internal/staff/PRIVATELAW")
            .query("ccd_service_names =PRIVATELAW")
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generateStaffUsers")
    public void verifyGetStaffUsers() {

        List<StaffResponse> staffResponse = staffResponseDetailsApi.getAllStaffResponseDetails(
            AUTHORIZATION_TOKEN,
            SERVICE_AUTHORIZATION_HEADER,
            "PRIVATELAW"
        );
        assertThat(staffResponse, is(notNullValue()));
    }

}*/
