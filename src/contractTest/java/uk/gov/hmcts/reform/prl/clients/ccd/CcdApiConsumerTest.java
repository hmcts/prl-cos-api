package uk.gov.hmcts.reform.prl.clients.ccd;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.ccd.client"})
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "ccd", port = "5001")
@TestPropertySource(properties = {"core_case_data.api.url=http://localhost:5001"})
@PactFolder("pacts")
@SpringBootTest
@ImportAutoConfiguration({FeignAutoConfiguration.class})
@Slf4j
public class CcdApiConsumerTest {

    @Value("${test.bearer-token}")
    private String bearerToken;

    @Value("${test.service-auth-token}")
    private String serviceAuthorizationHeader;

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Pact(provider = "ccd", consumer = "prl_cos")
    private RequestResponsePact createCaseInCcd(PactDslWithProvider builder) throws JsonProcessingException {
        log.info("bearer token is: {}", bearerToken);
        log.info("serviceAuthorizationHeader is: {}", serviceAuthorizationHeader);
        return builder
            .given("A request to create a case in CCD")
            .uponReceiving("a request to create a case in CCD with valid authorization")
            .method("POST")
            .headers("ServiceAuthorization", serviceAuthorizationHeader)
            .headers("Authorization", bearerToken)
            .headers("Content-Type", "application/json")
            .path("/citizens/UserID/jurisdictions/jurisdictionId/case-types/caseType/cases")
            .matchQuery("ignore-warning", "true")
            .body(new ObjectMapper().writeValueAsString(buildCaseDataContent()), "application/json")
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(createCaseResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createCaseInCcd")
    public void verifyCreateCaseInCcd() {
        log.info("bearer token is: {}", bearerToken);
        log.info("serviceAuthorizationHeader is: {}", serviceAuthorizationHeader);
        CaseDetails caseDetails = coreCaseDataApi.submitForCitizen(bearerToken, serviceAuthorizationHeader, "UserID",
                                                                   "jurisdictionId", "caseType",
                                                                   true, buildCaseDataContent()
        );
        assertNotNull(caseDetails);
        assertEquals("DRAFT", caseDetails.getState());
        assertEquals("PRLAPPS", caseDetails.getCaseTypeId());
        assertEquals("1658508917240231", caseDetails.getId().toString());
        assertEquals("C100", caseDetails.getData().get("caseTypeOfApplication"));
    }

    private PactDslJsonBody createCaseResponse() {
        return new PactDslJsonBody()
            .stringType("id", "1658508917240231")
            .stringType("case_type", "PRLAPPS")
            .stringType("callbackResponseStatus", "Success")
            .stringType("state", "DRAFT")
            .integerType("securityLevel", 23)
            .date("createdDate", "dd/mm/yyyy")
            .date("lastModified", "dd/mm/yyyy")
            .object("data")
            .stringType("caseTypeOfApplication", "C100")
            .closeObject().asBody();
    }

    private CaseDataContent buildCaseDataContent() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseTypeOfApplication", "C100");
        return CaseDataContent.builder().data(caseData).eventToken("EventToken").caseReference("CaseReference").build();
    }
}
