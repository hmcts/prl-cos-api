import com.github.tomakehurst.wiremock.stubbing.Scenario;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
    "core_case_data.api.url=${core_case_data.api.url}"
})
@AutoConfigureWireMock(port = 0)
public class ExtendedCaseDataApiRetryTest {

    @Autowired
    private ExtendedCaseDataApi caseDataApi;

    private final String testCaseId = "123456";
    private final String expectedUrl = "/cases/" + testCaseId;

    @Test
    public void shouldRetryOn5xxServerError() {
        // Expect: 503 -> 503 -> 200 Success
        stubFor(get(urlEqualTo(expectedUrl))
                    .inScenario("5xx Retry Scenario")
                    .whenScenarioStateIs(Scenario.STARTED)
                    .willReturn(aResponse().withStatus(503)) // 503 Service Unavailable
                    .willSetStateTo("Attempt 2"));

        stubFor(get(urlEqualTo(expectedUrl))
                    .inScenario("5xx Retry Scenario")
                    .whenScenarioStateIs("Attempt 2")
                    .willReturn(aResponse().withStatus(503))
                    .willSetStateTo("Attempt 3"));

        stubFor(get(urlEqualTo(expectedUrl))
                    .inScenario("5xx Retry Scenario")
                    .whenScenarioStateIs("Attempt 3")
                    .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody("{ \"id\": \"123456\" }")));

        caseDataApi.getExtendedCaseDetails("Bearer auth", "Bearer service", testCaseId);

        verify(3, getRequestedFor(urlEqualTo(expectedUrl)));
    }

    @Test
    public void shouldNotRetryOn4xxClientError() {
        stubFor(get(urlEqualTo(expectedUrl))
                    .willReturn(aResponse().withStatus(400)));

        assertThrows(FeignException.BadRequest.class, () -> {
            caseDataApi.getExtendedCaseDetails("Bearer auth", "Bearer service", testCaseId);
        });

        verify(1, getRequestedFor(urlEqualTo(expectedUrl)));
    }
}
