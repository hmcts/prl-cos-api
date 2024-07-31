package uk.gov.hmcts.reform.prl.clients.refdata;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
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
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RD_STAFF_FIRST_PAGE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RD_STAFF_PAGE_SIZE;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "staffResponseDetailsApi", port = "8899")
@ContextConfiguration(
    classes = {StaffResponseDetailsApiConsumerApplicaton.class, IdamApiConsumerApplication.class}
)
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
        "staffDetails.api.url=http://localhost:8899",
        "amRoleAssignment.api.url="
    }
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

    @Pact(provider = "staffResponseDetails_Api", consumer = "prl_cos")
    public V4Pact generateStaffUsers(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("Staff User details for given servicename")
            .uponReceiving("A Request to get staff details")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/refdata/internal/staff/usersByServiceName")
            .query("ccd_service_names=PRIVATELAW&sort_column=lastName&sort_direction=ASC&page_size=250&page_number=0")
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(ResourceLoader.loadJson(validResponseBody),"application/json")
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "generateStaffUsers")
    public void verifyGetStaffUsers() {

        List<StaffResponse> staffResponseList = staffResponseDetailsApi.getAllStaffResponseDetails(
            AUTHORIZATION_TOKEN,
            SERVICE_AUTH_TOKEN,
            "PRIVATELAW",
            "lastName",
            "ASC",
            RD_STAFF_PAGE_SIZE,
            RD_STAFF_FIRST_PAGE
        ).getBody();
        assertNotNull(staffResponseList);
        assertEquals("Rama",staffResponseList.get(0).getStaffProfile().getLastName());
        assertEquals("crd_func_test_2.0_rdcc_3831_107@justice.gov.uk",staffResponseList.get(0).getStaffProfile().getEmailId());
    }

}
