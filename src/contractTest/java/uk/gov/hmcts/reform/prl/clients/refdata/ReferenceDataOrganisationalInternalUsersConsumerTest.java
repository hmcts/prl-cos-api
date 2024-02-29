package uk.gov.hmcts.reform.prl.clients.refdata;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.prl.models.Organisations;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@PactTestFor(providerName = "referenceData_organisationalInternal", port = "8894")
@TestPropertySource(
    properties = {"bundle.api.url=","idam.api.url=localhost:5000","commonData.api.url=http://localhost:8899",
        "fis_hearing.api.url=localhost:5000",
        "refdata.api.url=",
        "courtfinder.api.url=",
        "prl-dgs-api.url=",
        "fees-register.api.url=",
        "fis_hearing.api.url=",
        "judicialUsers.api.url=",
        "locationfinder.api.url=",
        "rd_professional.api.url=localhost:8894",
        "payments.api.url=",
        "pba.validation.service.api.baseurl=",
        "staffDetails.api.url=",
        "amRoleAssignment.api.url="
    }
)
public class ReferenceDataOrganisationalInternalUsersConsumerTest extends ReferenceDataConsumerTestBase {

    @Pact(provider = "referenceData_organisationalInternal", consumer = "prl_cos")
    public RequestResponsePact generatePactFragmentForGetOrganisationById(PactDslWithProvider builder) {
        // @formatter:off
        return builder
            .given("Organisation exists for given Id")
            .uponReceiving("A Request to get organisation by Id")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/refdata/internal/v1/organisations")
            .query("id=orgId")
            .willRespondWith()
            .body(buildOrganisationResponseDsl())
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetOrganisationById")
    public void verifyGetOrganisationById() {

        Organisations orgainisation = organisationApi.findOrganisation(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, "orgId");
        assertThat(orgainisation, is(notNullValue()));
        assertThat(orgainisation.getOrganisationIdentifier(), is("someOrganisationIdentifier"));
    }

}
