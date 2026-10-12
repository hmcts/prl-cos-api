package uk.gov.hmcts.reform.prl.clients.hmc;

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
import uk.gov.hmcts.reform.prl.clients.HmcHearingApiClient;
import uk.gov.hmcts.reform.prl.clients.idam.IdamApiConsumerApplication;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Consumer Pact test for the HMC hearing component, exercised directly via {@link HmcHearingApiClient}
 * (previously routed through fis-hmc-api's HearingApiClient). Verifies the GET /hearings/{caseReference}
 * interaction used by HearingService#getHearings.
 */
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "hmcHearingApi", port = "8900")
@ContextConfiguration(
    classes = {HmcHearingApiClientConsumerApplication.class, IdamApiConsumerApplication.class}
)
@TestPropertySource(
    properties = {
        // Point the HMC Feign client at the Pact mock server (overrides application.yaml default)
        "hearing_component.api.feign-url=http://localhost:8900",
        // Required by TempHmcHearingFeignConfig S2S interceptor (secret must be valid base32 for TOTP)
        "fis.s2s.microservice=fis_hmc_api",
        "fis.s2s.secret=AABBCCDDEEFFGGHH",
        // Placeholder URLs required by other Feign clients registered via the full clients package scan
        "bundle.api.url=",
        "idam.api.url=localhost:5000",
        "commonData.api.url=localhost:5000",
        "fis_hearing.api.url=",
        "refdata.api.url=",
        "courtfinder.api.url=",
        "fees-register.api.url=",
        "judicialUsers.api.url=",
        "locationfinder.api.url=",
        "rd_professional.api.url=",
        "payments.api.url=",
        "pba.validation.service.api.baseurl=",
        "staffDetails.api.url=",
        "amRoleAssignment.api.url=",
        "core_case_data.api.url=",
        "postcodelookup.api.url=",
        "bankHolidays.api.url="
    }
)
@PactFolder("pacts")
public class HmcHearingApiConsumerTest {

    @Autowired
    HmcHearingApiClient hmcHearingApiClient;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    private static final String CASE_REFERENCE = "1675335865166401";

    private final String getHearingDetailsResponseBody = "listwithoutnotice/GetHearingDetailsResponseBody.json";

    @Pact(provider = "hmcHearingApi", consumer = "prl_cos_api")
    public RequestResponsePact generatePactFragmentForGetHearingDetails(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("Hearings exist for a case reference")
            .uponReceiving("A Request to get hearing details for a case reference")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .headers(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/hearings/" + CASE_REFERENCE)
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(ResourceLoader.loadJson(getHearingDetailsResponseBody), "application/json")
            .toPact();
        // @formatter:on
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetHearingDetails")
    public void verifyGetHearingDetails() {
        Hearings hearings = hmcHearingApiClient.getHearingDetails(
            AUTHORIZATION_TOKEN,
            SERVICE_AUTH_TOKEN,
            null,
            null,
            null,
            CASE_REFERENCE
        );

        assertNotNull(hearings);
        assertEquals("ABA5", hearings.getHmctsServiceCode());
        assertEquals(CASE_REFERENCE, hearings.getCaseRef());
        assertNotNull(hearings.getCaseHearings());
    }
}

