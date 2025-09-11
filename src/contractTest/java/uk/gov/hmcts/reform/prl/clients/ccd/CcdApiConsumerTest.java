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
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.ccd.client"})
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "ccd_submitForCitizen_api", port = "5001")
@TestPropertySource(properties = {"core_case_data.api.url=http://localhost:5001"})
@PactFolder("pacts")
@SpringBootTest
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class CcdApiConsumerTest {

    private static final String BEARER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";
    private static final String SERVICE_AUTHORIZATION_HEADER = "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Pact(provider = "ccd_submitForCitizen_api", consumer = "prl_cos")
    private RequestResponsePact createCaseInCcd(PactDslWithProvider builder) throws JsonProcessingException {
        return builder
            .given("A request to create a case in CCD")
            .uponReceiving("a request to create a case in CCD with valid authorization")
            .method("POST")
            .headers("ServiceAuthorization", SERVICE_AUTHORIZATION_HEADER)
            .headers("Authorization", BEARER_TOKEN)
            .headers("Content-Type", "application/json")
            .path("/citizens/UserID/jurisdictions/jurisdictionId/case-types/caseType/cases")
            .matchQuery("ignore-warning", "true")
            .body(new ObjectMapper().writeValueAsString(buildCaseDataContent()), "application/json")
            .willRespondWith()
            .status(HttpStatus.SC_CREATED)
            .body(createCaseResponse())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createCaseInCcd")
    public void verifyCreateCaseInCcd() {
        CaseDetails caseDetails = coreCaseDataApi.submitForCitizen(BEARER_TOKEN, SERVICE_AUTHORIZATION_HEADER, "UserID",
                                                                   "jurisdictionId", "caseType",
                                                                   true, buildCaseDataContent()
        );
        assertNotNull(caseDetails);
        assertEquals("CaseCreated", caseDetails.getState());
        assertEquals("PRLAPPS", caseDetails.getCaseTypeId());
        assertEquals("C100", caseDetails.getData().get("caseTypeOfApplication"));
    }

    private void addLocalDateTimeArray(PactDslJsonBody body, String fieldName, LocalDateTime dateTime) {
        body.array(fieldName)
            .numberValue(dateTime.getYear())
            .numberValue(dateTime.getMonthValue())
            .numberValue(dateTime.getDayOfMonth())
            .numberValue(dateTime.getHour())
            .numberValue(dateTime.getMinute())
            .closeArray();
    }

    private PactDslJsonBody createCaseResponse() {
        PactDslJsonBody body = new PactDslJsonBody()
            .stringType("case_type_id", "PRLAPPS")
            .stringType("state", "CaseCreated")
            .stringType("security_classification", "PUBLIC")
            .object("case_data")
                .stringType("caseTypeOfApplication", "C100")
            .closeObject().asBody();

        addLocalDateTimeArray(body, "created_date", LocalDateTime.of(2025, 6, 16, 10, 0));
        addLocalDateTimeArray(body, "last_modified", LocalDateTime.of(2025, 6, 16, 10, 5));
        return body;
    }

    private CaseDataContent buildCaseDataContent() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseTypeOfApplication", "C100");
        return CaseDataContent.builder().data(caseData).eventToken("EventToken").caseReference("CaseReference").build();
    }
}
