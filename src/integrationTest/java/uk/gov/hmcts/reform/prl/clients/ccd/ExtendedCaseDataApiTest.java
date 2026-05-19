package uk.gov.hmcts.reform.prl.clients.ccd;

import uk.gov.hmcts.reform.prl.clients.ccd.ExtendedCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.models.extendedcasedetails.ExtendedCaseDetails;

import com.github.tomakehurst.wiremock.stubbing.Scenario;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
    "core_case_data.api.url=http://localhost:${wiremock.server.port}"
})
@AutoConfigureWireMock(port = 0)
public class ExtendedCaseDataApiTest {

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    private final String testCaseId = "123456";
    private final String caseType = "C100";

    @Test
    public void shouldRetryOn5xxServerError() {
        // Expect: 503 -> 503 -> 200 Success
        stubFor(get(urlPathEqualTo(testCaseId))
                    .inScenario("5xx Retry Scenario")
                    .whenScenarioStateIs(Scenario.STARTED)
                    .willReturn(aResponse().withStatus(503))
                    .willSetStateTo("Attempt 2"));

        stubFor(get(urlPathEqualTo(testCaseId))
                    .inScenario("5xx Retry Scenario")
                    .whenScenarioStateIs("Attempt 2")
                    .willReturn(aResponse().withStatus(503))
                    .willSetStateTo("Attempt 3"));

        stubFor(get(urlPathEqualTo(testCaseId))
                    .inScenario("5xx Retry Scenario")
                    .whenScenarioStateIs("Attempt 3")
                    .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("{ \"id\": \"123456\" }")));

        coreCaseDataApi.searchCases("userToken", "s2sToken", caseType, testCaseId);

        verify(3, getRequestedFor(urlPathEqualTo(testCaseId)));
    }

    @Test
    public void shouldNotRetryOn4xxClientError() {
        stubFor(get(urlPathEqualTo(testCaseId))
                    .willReturn(aResponse().withStatus(400)));

        assertThrows(FeignException.BadRequest.class, () -> {
            coreCaseDataApi.searchCases("userToken", "s2sToken", caseType, testCaseId);
        });

        verify(1, getRequestedFor(urlPathEqualTo(testCaseId)));
    }
}
