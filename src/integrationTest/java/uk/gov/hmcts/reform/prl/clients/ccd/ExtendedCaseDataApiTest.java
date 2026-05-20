package uk.gov.hmcts.reform.prl.clients.ccd;

import com.client.config.FeignRetryConfig;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import(FeignRetryConfig.class)
@SpringBootTest(properties = {
    "core_case_data.api.url=http://localhost:${wiremock.server.port}"
})
@AutoConfigureWireMock(port = 0)
public class ExtendedCaseDataApiTest {

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    private final String testCaseId = "123456";
    private final String caseType = "C100";
    private final String expectedUrl = "/searchCases";

    @Test
    public void shouldRetryOn5xxServerError() {
        // Expect: 503 -> 503 -> 200 Success for a POST request
        stubFor(post(urlPathEqualTo(expectedUrl))
                    .withQueryParam("ctid", equalTo(caseType))
                    .inScenario("5xx Retry Scenario")
                    .whenScenarioStateIs(Scenario.STARTED)
                    .willReturn(aResponse().withStatus(503))
                    .willSetStateTo("Attempt 2"));

        stubFor(post(urlPathEqualTo(expectedUrl))
                    .withQueryParam("ctid", equalTo(caseType))
                    .inScenario("5xx Retry Scenario")
                    .whenScenarioStateIs("Attempt 2")
                    .willReturn(aResponse().withStatus(503))
                    .willSetStateTo("Attempt 3"));

        stubFor(post(urlPathEqualTo(expectedUrl))
                    .withQueryParam("ctid", equalTo(caseType))
                    .inScenario("5xx Retry Scenario")
                    .whenScenarioStateIs("Attempt 3")
                    .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("{ \"total\": 1, \"cases\": [{ \"id\": \"" + testCaseId + "\" }] }")));

        coreCaseDataApi.searchCases("userToken", "s2sToken", caseType, "{}");

        verify(3, postRequestedFor(urlPathEqualTo(expectedUrl)).withQueryParam("ctid", equalTo(caseType)));
    }

    @Test
    void shouldNotRetryOn4xxClientError() {
        stubFor(post(urlPathEqualTo(expectedUrl))
                    .withQueryParam("ctid", equalTo(caseType))
                    .willReturn(aResponse().withStatus(400)));

        assertThrows(FeignException.BadRequest.class, () -> {
            coreCaseDataApi.searchCases("userToken", "s2sToken", caseType, "{}");
        });

        verify(1, postRequestedFor(urlPathEqualTo(expectedUrl)).withQueryParam("ctid", equalTo(caseType)));
    }
}
