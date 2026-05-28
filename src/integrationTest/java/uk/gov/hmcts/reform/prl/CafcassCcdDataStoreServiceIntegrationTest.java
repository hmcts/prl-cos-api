package uk.gov.hmcts.reform.prl;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassCcdDataStoreService;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
    "core_case_data.api.url=http://localhost:${wiremock.server.port}",
    // "launchdarkly.offline=true"
})
@AutoConfigureWireMock(port = 0)
public class CafcassCcdDataStoreServiceIntegrationTest {

    @Autowired
    private CafcassCcdDataStoreService cafcassCcdDataStoreService;

    @BeforeEach
    public void setup() {
        WireMock.reset();
    }

    @Test
    public void shouldRetry3TimesAndReturnEmptyFallbackOn502() {
        stubFor(post(urlPathEqualTo("/searchCases"))
                    .willReturn(aResponse()
                                    .withStatus(502)
                                    .withHeader("Content-Type", "application/json")));

        SearchResult result = cafcassCcdDataStoreService.searchCases("userToken", "{}", "s2sToken", "PRLAPPS");

        assertEquals(0, result.getTotal());

        WireMock.verify(3, postRequestedFor(urlPathEqualTo("/searchCases")));
    }
}
