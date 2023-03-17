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
import uk.gov.hmcts.reform.prl.clients.CommonDataRefApi;
import uk.gov.hmcts.reform.prl.clients.idam.IdamApiConsumerApplication;
import uk.gov.hmcts.reform.prl.models.dto.hearingdetails.CommonDataResponse;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "commonDataRefApi", port = "8890")
@ContextConfiguration(
    classes = {CommonRefDataApiConsumerApplication.class, IdamApiConsumerApplication.class}
)
@TestPropertySource(
    properties = {"commonData.api.url=http://localhost:8890", "idam.api.url=localhost:5000"}
)
@PactFolder("pacts")
public class CommonRefDataConsumerTest {

    @Autowired
    CommonDataRefApi commonDataRefApi;

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";

    private final String validResponseBody = "commonrefdata/CommonRefData.json";

    @Pact(provider = "commonDataRefApi", consumer = "prl_cos")
    public RequestResponsePact generateCommonRefData(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("common Ref data for given servicename")
            .uponReceiving("A Request to get common Ref data")
            .method("GET")
            .headers(
                SERVICE_AUTHORIZATION_HEADER,
                SERVICE_AUTH_TOKEN,
                AUTHORIZATION_HEADER,
                AUTHORIZATION_TOKEN)
            .path("refdata/commondata/lov/categories/HearingType")
            .query("serviceId=RTF&isChildRequired=Yes")
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(ResourceLoader.loadJson(validResponseBody),"application/json")
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generateCommonRefData")
    public void verifyCommonRefData() {

        CommonDataResponse commonDataResponse = commonDataRefApi.getAllCategoryValuesByCategoryId(
            AUTHORIZATION_TOKEN,
            SERVICE_AUTH_TOKEN,
            "HearingType","ABA5","Yes"
        );
        assertNotNull(commonDataResponse);
        assertEquals("HearingType",commonDataResponse.getCategoryValues().get(0).getCategoryKey());
    }

}
