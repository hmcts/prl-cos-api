package uk.gov.hmcts.reform.prl.clients.refdatacategory;

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
import uk.gov.hmcts.reform.prl.clients.cafcass.ReferenceDataApi;
import uk.gov.hmcts.reform.prl.clients.idam.IdamApiConsumerApplication;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.refdata.Categories;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "referenceDataCategoryApi", port = "8894")
@TestPropertySource(
    properties = {"bundle.api.url=","idam.api.url=localhost:5000","commonData.api.url=http://localhost:8899",
        "fis_hearing.api.url=localhost:5000",
        "refdata.api.url=localhost:8894",
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
        "amRoleAssignment.api.url=",
        "core_case_data.api.url="
    }
)
@ContextConfiguration(
        classes = {RefDataCategoryApiConsumerApplication.class, IdamApiConsumerApplication.class})
public class RefDataCategoryApiConsumerTest {

    private final String response = "response/RefDataCategoryResponse.json";

    @Autowired private ReferenceDataApi hearingRefDataApi;


    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    @Pact(provider = "referenceDataCategoryApi", consumer = "prl_cos_api")
    public RequestResponsePact generateCategoryApiConsumerTest(PactDslWithProvider builder)
            throws Exception {

        return builder.given("case hearing/hearings exist for a case ")
                .uponReceiving(
                        "A Request to get list of values for a given service id and category id")
                .method("GET")
                .headers(
                        SERVICE_AUTHORIZATION_HEADER,
                        SERVICE_AUTH_TOKEN,
                        AUTHORIZATION_HEADER,
                        AUTHORIZATION_TOKEN)
                .headers("Content-Type", "application/json")
                .path("/refdata/commondata/lov/categories/HearingType")
                .query("serviceId=ABA5")
                .willRespondWith()
                .body(ResourceLoader.loadJson(response), "application/json")
                .status(HttpStatus.SC_OK)
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generateCategoryApiConsumerTest")
    public void verifyCateogryListOfValues() {

        final Categories categories =
            hearingRefDataApi.retrieveListOfValuesByCategoryId(
                        AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, "HearingType", "ABA5");

        assertNotNull(categories);
    }
}
