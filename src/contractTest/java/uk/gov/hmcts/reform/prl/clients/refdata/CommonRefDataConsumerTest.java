package uk.gov.hmcts.reform.prl.clients.refdata;


import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "commonDataRefApi", port = "8899")
@ContextConfiguration(
    classes = {CommonRefDataApiConsumerApplication.class, IdamApiConsumerApplication.class}
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
        "rd_professional.api.url=",
        "payments.api.url=",
        "pba.validation.service.api.baseurl=",
        "staffDetails.api.url=",
        "amRoleAssignment.api.url="
    }
)

@PactFolder("pacts")
public class CommonRefDataConsumerTest {

    @Autowired
    CommonDataRefApi commonDataRefApi;

    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private static final String SERVICE_AUTHORIZATION_HEADER = "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";

    private final String validResponseBody = "commonrefdata/CommonRefData.json";



    @Pact(provider = "commonData_Ref_Api", consumer = "prl_cos")
    public V4Pact generatePactFragmentForCategoryId(PactDslWithProvider builder) throws Exception {
        // @formatter:off
        return builder
            .given("Common Data")
            .uponReceiving("A Request for Common Data API")
            .method("GET")
            .headers("ServiceAuthorization", SERVICE_AUTHORIZATION_HEADER)
            .headers("Authorization", BEARER_TOKEN)
            .headers("Content-Type", "application/json")
            .path("/refdata/commondata/lov/categories/hearingType")
            .query("serviceId=ABA5&isChildRequired=N")
            .willRespondWith()
            .status(200)
            .body(ResourceLoader.loadJson(validResponseBody), "application/json")
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForCategoryId")
    public void verifyCommonDataDetails() {
        CommonDataResponse allCategoryValuesByCategoryId = commonDataRefApi.getAllCategoryValuesByCategoryId(BEARER_TOKEN,
                                                                                                             SERVICE_AUTHORIZATION_HEADER,
                                                                                                             "hearingType",
                                                                                                             "ABA5",
                                                                                                             "N"
        );

        assertNotNull(allCategoryValuesByCategoryId);
    }

}
