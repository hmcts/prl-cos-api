package uk.gov.hmcts.reform.prl.clients.listwithoutnotice;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.clients.idam.IdamApiConsumerApplication;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedRequest;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "hearingApiClient", port = "8899")
@ContextConfiguration(
    classes = {HearingApiClientConsumerApplication.class, IdamApiConsumerApplication.class}
)
@TestPropertySource(
    properties = {"bundle.api.url=","idam.api.url=localhost:5000","commonData.api.url=localhost:5000",
        "fis_hearing.api.url=http://localhost:8899",
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
        "amRoleAssignment.api.url="
    }
)

@PactFolder("pacts")
public class CaseLinedApiConsumerTest {

    @Autowired
    HearingApiClient hearingApiClient;

    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private static final String SERVICE_AUTHORIZATION_HEADER = "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";

    private final String caseLinkedResponseBodyResponseBody = "listwithoutnotice/CaseLinkedResponseBody.json";
    private CaseLinkedRequest caseLinkedRequest;



    @Pact(provider = "hearingApiClient", consumer = "prl_cos")
    public V4Pact generatePactFragmentForCaseLinked(PactDslWithProvider builder) throws Exception {

        caseLinkedRequest = CaseLinkedRequest.caseLinkedRequestWith().caseReference("1677767515750127").build();
        // @formatter:off
        return builder
            .given("Case Linked")
            .uponReceiving("A Request for Case Linked")
            .method("POST")
            .headers("ServiceAuthorization", SERVICE_AUTHORIZATION_HEADER)
            .headers("Authorization", BEARER_TOKEN)
            .headers("Content-Type", "application/json")
            .path("/serviceLinkedCases")
            .body(new ObjectMapper().writeValueAsString(caseLinkedRequest), "application/json")
            .willRespondWith()
            .status(200)
            .body(ResourceLoader.loadJson(caseLinkedResponseBodyResponseBody),"application/json")
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForCaseLinked")
    public void verifyCaseLinkedDetails() {
        caseLinkedRequest = CaseLinkedRequest.caseLinkedRequestWith().caseReference("1677767515750127").build();
        List<CaseLinkedData>
            caseLinkedData = hearingApiClient.getCaseLinkedData(BEARER_TOKEN,SERVICE_AUTHORIZATION_HEADER,caseLinkedRequest);
        assertNotNull(caseLinkedData);
        assertEquals(caseLinkedData.get(0).getCaseReference(),"1670601355422736");
        assertEquals(caseLinkedData.get(0).getCaseName(),"Case_Flag_9_Dec_6");
    }
}
