package uk.gov.hmcts.reform.prl.clients.listwithoutnotice;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpStatus;
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
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
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
    properties = {"fis_hearing.api.url=http://localhost:8899", "idam.api.url=localhost:5000"}
)
@PactFolder("pacts")
public class HearingApiConsumerTest {

    @Autowired
    HearingApiClient hearingApiClient;

    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private static final String SERVICE_AUTHORIZATION_HEADER = "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";

    private final String validResponseBody = "listwithoutnotice/CaseLinkedResponseBody.json";

    static final String AUTHORIZATION_HEADER = "Authorization";
    static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";
    static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    private final String validResponseBody1 = "listwithoutnotice/GetHearingDetails.json";
    private CaseLinkedRequest caseLinkedRequest;


    @Pact(provider = "hearingApiClient", consumer = "prl_cos")
    public RequestResponsePact generatePactFragmentForCaseLinked(PactDslWithProvider builder) throws Exception {

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
            .body(ResourceLoader.loadJson(validResponseBody),"application/json")
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForCaseLinked")
    public void verifyCaseLinkedDetails() {
        caseLinkedRequest = CaseLinkedRequest.caseLinkedRequestWith().caseReference("1677767515750127").build();
        List<CaseLinkedData>
            caseLinkedData = hearingApiClient.getCaseLinkedData(BEARER_TOKEN,SERVICE_AUTHORIZATION_HEADER,caseLinkedRequest);
        assertNotNull(caseLinkedData);
        assertEquals(caseLinkedData.get(0).getCaseReference(),"1677767515750127");
        assertEquals(caseLinkedData.get(0).getCaseName(),"Test");
    }


    @Pact(provider = "hearingApiClient", consumer = "prl_cos")
    public RequestResponsePact generatePactFragmentForGetHearingDetails(PactDslWithProvider builder) throws Exception {

        // @formatter:off
        return builder
            .given("Get Hearing Details")
            .uponReceiving("A Request for Get Hearing Details")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN, AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN)
            .path("/hearingse")
            .query("caseReference=1677767515750127")
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(ResourceLoader.loadJson(validResponseBody1),"application/json")
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentForGetHearingDetails")
    public void verifyGetHearingDetails() {
        Hearings
            hearingDetails = hearingApiClient.getHearingDetails(BEARER_TOKEN,SERVICE_AUTHORIZATION_HEADER,"1677767515750127");
        assertNotNull(hearingDetails);
        assertEquals(hearingDetails.getCaseHearings().get(0).getHearingType(),"Test");
        assertEquals(hearingDetails.getCaseHearings().get(0).getHmcStatus(),"LISTED");
    }

}
