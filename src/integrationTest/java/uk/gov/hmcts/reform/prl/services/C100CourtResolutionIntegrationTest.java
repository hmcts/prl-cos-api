package uk.gov.hmcts.reform.prl.services;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
    "postcodelookup.api.url=http://localhost:${wiremock.server.port}",
    "postcodelookup.api.key=test-os-key",
    "locationfinder.api.url=http://localhost:${wiremock.server.port}",
    "fact.v2.api.url=http://localhost:${wiremock.server.port}",
    "fact.v2.enabled=true",
    "fact.v2.oauth.token-url=http://localhost:${wiremock.server.port}/token",
    "fact.v2.oauth.tenant-id=test-tenant",
    "fact.v2.oauth.client-id=test-client",
    "fact.v2.oauth.client-secret=test-secret",
    "fact.v2.oauth.scope=api://test-fact/.default"
})
@AutoConfigureWireMock(port = 0)
class C100CourtResolutionIntegrationTest {

    private static final String POSTCODE = "SA1 2FA";
    private static final String COURT_SLUG = "swansea-civil-justice-centre";
    private static final String SYSTEM_USER_TOKEN = "system-user-token";
    private static final String USER_ID = "93e314d5-150f-48f3-9319-ba63ae19c482";

    @Autowired
    private CourtFinderService courtFinderService;

    @MockBean
    private FeatureToggleService featureToggleService;

    @MockBean
    private SystemUserService systemUserService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void setUp() {
        WireMock.reset();
        when(featureToggleService.isOsCourtLookupFeatureEnabled()).thenReturn(true);
        when(systemUserService.getSysUserToken()).thenReturn(SYSTEM_USER_TOKEN);
        when(systemUserService.getUserId(SYSTEM_USER_TOKEN)).thenReturn(USER_ID);
        when(authTokenGenerator.generate()).thenReturn("s2s-test-token");

        stubFor(get(urlPathEqualTo("/postcode"))
            .withQueryParam("postcode", equalTo(POSTCODE))
            .withQueryParam("key", equalTo("test-os-key"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"results":[{"DPA":{"LOCAL_CUSTODIAN_CODE":"6855",
                    "LOCAL_CUSTODIAN_CODE_DESCRIPTION":"CITY AND COUNTY OF SWANSEA"}}]}
                    """)));

        stubFor(get(urlPathEqualTo("/refdata/location/court-venues/services"))
            .withQueryParam("service_code", equalTo("ABA5"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {"court_venues":[{"epimms_id":"234946","court_type_id":"18",
                    "site_name":"Swansea Civil Justice Centre","region":"Wales",
                    "fact_url":"https://www.find-court-tribunal.service.gov.uk/courts/swansea-civil-justice-centre"}]}
                    """)));

        stubFor(post(urlPathEqualTo("/token"))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("{\"access_token\":\"fact-test-token\",\"expires_in\":3600}")));

        stubFor(get(urlPathEqualTo("/courts/slug/" + COURT_SLUG + "/v1"))
            .withHeader("Authorization", equalTo("Bearer fact-test-token"))
            .withHeader("X-User-Id", equalTo(USER_ID))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    {
                      "id":"court-id",
                      "name":"Swansea Civil Justice Centre",
                      "slug":"swansea-civil-justice-centre",
                      "open":true,
                      "courtCodes":[{"countyCourtCode":234946}],
                      "courtContactDetails":[
                        {"email":"family.swansea@example.test",
                         "courtContactDescription":{"name":"Family"}},
                        {"email":"children.swansea@example.test",
                         "courtContactDescription":{"name":"Children"}}
                      ]
                    }
                    """)));

        stubFor(get(urlPathEqualTo("/search/courts/v1/postcode"))
            .withQueryParam("postcode", equalTo(POSTCODE))
            .withQueryParam("serviceArea", equalTo("Domestic abuse"))
            .withQueryParam("action", equalTo("NEAREST"))
            .withQueryParam("limit", equalTo("10"))
            .withHeader("Authorization", equalTo("Bearer fact-test-token"))
            .withHeader("X-User-Id", equalTo(USER_ID))
            .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                    [{"courtId":"court-id","courtName":"Swansea Civil Justice Centre",
                    "courtSlug":"swansea-civil-justice-centre","distance":0.4}]
                    """)));
    }

    @Test
    void resolvesC100ChildPostcodeThroughOsRefDataAndFactV2() throws Exception {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .c100RebuildData(C100RebuildData.builder().c100RebuildChildPostCode(POSTCODE).build())
            .build();

        var court = courtFinderService.getNearestFamilyCourt(caseData);

        assertEquals("Swansea Civil Justice Centre", court.getCourtName());
        assertEquals(COURT_SLUG, court.getCourtSlug());
        assertEquals(234946, court.getCountyLocationCode());
        assertEquals(2, court.getCourtEmailAddresses().size());
        assertEquals("family.swansea@example.test", court.getCourtEmailAddresses().getFirst().getAddress());

        WireMock.verify(getRequestedFor(urlPathEqualTo("/postcode"))
            .withQueryParam("postcode", equalTo(POSTCODE)));
        WireMock.verify(getRequestedFor(urlPathEqualTo("/refdata/location/court-venues/services"))
            .withQueryParam("service_code", equalTo("ABA5")));
        WireMock.verify(postRequestedFor(urlPathEqualTo("/token")));
        WireMock.verify(getRequestedFor(urlPathEqualTo("/courts/slug/" + COURT_SLUG + "/v1")));
    }

    @Test
    void resolvesFl401ApplicantPostcodeThroughFactV2AndLoadsCourtDetails() throws Exception {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(PartyDetails.builder()
                .address(Address.builder().postCode(POSTCODE).build())
                .build())
            .build();

        var court = courtFinderService.getNearestFamilyCourt(caseData);

        assertEquals("Swansea Civil Justice Centre", court.getCourtName());
        assertEquals(COURT_SLUG, court.getCourtSlug());
        WireMock.verify(getRequestedFor(urlPathEqualTo("/search/courts/v1/postcode"))
            .withQueryParam("postcode", equalTo(POSTCODE))
            .withQueryParam("serviceArea", equalTo("Domestic abuse"))
            .withQueryParam("action", equalTo("NEAREST"))
            .withQueryParam("limit", equalTo("10")));
        WireMock.verify(getRequestedFor(urlPathEqualTo("/courts/slug/" + COURT_SLUG + "/v1")));
        WireMock.verify(0, getRequestedFor(urlPathEqualTo("/search/results")));
    }
}
