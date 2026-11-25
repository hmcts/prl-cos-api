package uk.gov.hmcts.reform.prl.clients;

import com.github.tomakehurst.wiremock.WireMockServer;
import feign.FeignException;
import javassist.NotFoundException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.os.OsCourtFinderApi;
import uk.gov.hmcts.reform.prl.exception.OsCourtLookupException;
import uk.gov.hmcts.reform.prl.models.LocalAuthorityCourt;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.court.CourtOnboardingResponse;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.LocalAuthorityCourtDataLoader;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.OsCourtFinderService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CourtAllocationIntegrationTest.Config.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {"postcodelookup.api.key=test-key", "courts.filter=234946:configured@example.com,123456",
        "courts.daFilter=234946:configured@example.com", "courts.caDefaultCourtEpimmsID=234946"})
class CourtAllocationIntegrationTest {
    private static final WireMockServer UPSTREAM = new WireMockServer(0);

    static {
        UPSTREAM.start();
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = {OsCourtFinderApi.class, LocationRefDataApi.class})
    @Import({OsCourtFinderService.class, CourtFinderService.class, LocationRefDataService.class})
    static class Config {
    }

    @DynamicPropertySource
    static void endpoints(DynamicPropertyRegistry registry) {
        registry.add("postcodelookup.api.url", UPSTREAM::baseUrl);
        registry.add("locationfinder.api.url", UPSTREAM::baseUrl);
    }

    @Autowired
    private CourtFinderService courtFinderService;
    @Autowired
    private LocationRefDataService locationRefDataService;
    @MockBean
    private SystemUserService systemUserService;
    @MockBean
    private AuthTokenGenerator authTokenGenerator;
    @MockBean
    private LocalAuthorityCourtDataLoader localAuthorityCourtDataLoader;

    @BeforeEach
    void setUp() {
        UPSTREAM.resetAll();
        when(systemUserService.getSysUserToken()).thenReturn("Bearer system-token");
        when(authTokenGenerator.generate()).thenReturn("service-token");
        when(localAuthorityCourtDataLoader.getLocalAuthorityCourtList()).thenReturn(List.of(
            LocalAuthorityCourt.builder().localCustodianCode("123").epimmsId("234946").build()));
        UPSTREAM.stubFor(get(urlPathEqualTo("/postcode"))
            .withQueryParam("postcode", equalTo("SA1 2FA"))
            .withQueryParam("key", equalTo("test-key"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("""
                {"results":[{"DPA":{"LOCAL_CUSTODIAN_CODE":"123","LOCAL_CUSTODIAN_CODE_DESCRIPTION":"Swansea"}}]}
                """)));
        UPSTREAM.stubFor(get(urlPathEqualTo("/refdata/location/court-venues/services"))
            .withQueryParam("service_code", equalTo("ABA5"))
            .withHeader("Authorization", equalTo("Bearer system-token"))
            .withHeader("ServiceAuthorization", equalTo("service-token"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("""
                {"court_venues":[{"epimms_id":"234946","court_type_id":"18","region":"Wales",
                "site_name":"Swansea Civil Justice Centre","court_name":"Swansea Civil And Family Justice Centre",
                "court_address":"Quay West","postcode":"SA1 1SP","venue_name":"Swansea"}]}
                """)));
    }

    @AfterAll
    static void stop() {
        UPSTREAM.stop();
    }

    @Test
    void allocatesC100CourtAndConfiguredEmailUsingOnlyOsAndRefData() throws Exception {
        CaseData caseData = CaseData.builder().caseTypeOfApplication("C100")
            .c100RebuildData(C100RebuildData.builder().c100RebuildChildPostCode("SA1 2FA").build()).build();

        CourtVenue court = courtFinderService.getNearestFamilyCourt(caseData);

        assertThat(court).isNotNull();
        assertThat(court.getCourtEpimmsId()).isEqualTo("234946");
        assertThat(court.getCourtDisplayName()).isEqualTo("Swansea Civil Justice Centre");
        assertThat(court.getCourtName()).isEqualTo("Swansea Civil And Family Justice Centre");
        assertThat(locationRefDataService.getCourtLocations("Bearer system-token"))
            .extracting("code").containsExactly("234946:configured@example.com");
        UPSTREAM.verify(1, getRequestedFor(urlPathEqualTo("/postcode")));
        UPSTREAM.verify(2, getRequestedFor(urlPathEqualTo("/refdata/location/court-venues/services")));
        assertThat(UPSTREAM.getAllServeEvents()).hasSize(3);
    }

    @ParameterizedTest
    @CsvSource({"123456,true", "234946,false", "999999,false"})
    void returnsOnboardingDecisionForOnboardedOfflineAndUnconfiguredCourts(String epimmsId, boolean expectedOnboarded)
        throws Exception {
        when(localAuthorityCourtDataLoader.getLocalAuthorityCourtList()).thenReturn(List.of(
            LocalAuthorityCourt.builder().localCustodianCode("123").epimmsId(epimmsId).build()));
        UPSTREAM.stubFor(get(urlPathEqualTo("/refdata/location/court-venues/services"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("""
                {"court_venues":[{"epimms_id":"%s","court_type_id":"18","region":"Wales",
                "site_name":"Swansea Civil Justice Centre"}]}
                """.formatted(epimmsId))));

        CourtOnboardingResponse response = courtFinderService.getC100CourtOnboardingStatus("SA1 2FA");

        assertThat(response.getEpimmsId()).isEqualTo(epimmsId);
        assertThat(response.getCourtName()).isEqualTo("Swansea Civil Justice Centre");
        assertThat(response.isOnboarded()).isEqualTo(expectedOnboarded);
        UPSTREAM.verify(1, getRequestedFor(urlPathEqualTo("/postcode")));
        UPSTREAM.verify(1, getRequestedFor(urlPathEqualTo("/refdata/location/court-venues/services")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SA1 2FA", "sa1 2fa", "sa12fa"})
    void postcodeOverrideResolvesRefDataVenueWithoutCallingOs(String postcode) throws Exception {
        when(localAuthorityCourtDataLoader.getLocalAuthorityCourtList()).thenReturn(List.of(
            LocalAuthorityCourt.builder().specificPostCodes(List.of("SA1")).epimmsId("234946").build()));

        CourtOnboardingResponse response = courtFinderService.getC100CourtOnboardingStatus(postcode);

        assertThat(response.getEpimmsId()).isEqualTo("234946");
        assertThat(response.getCourtName()).isEqualTo("Swansea Civil Justice Centre");
        UPSTREAM.verify(0, getRequestedFor(urlPathEqualTo("/postcode")));
        UPSTREAM.verify(1, getRequestedFor(urlPathEqualTo("/refdata/location/court-venues/services")));
    }

    @Test
    void missingVenueDoesNotFallBackToAnotherService() throws Exception {
        UPSTREAM.stubFor(get(urlPathEqualTo("/refdata/location/court-venues/services"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{\"court_venues\":[]}")));

        assertThat(courtFinderService.getC100NearestFamilyCourt("SA1 2FA")).isNull();
        assertThat(UPSTREAM.getAllServeEvents()).hasSize(2);
    }

    @Test
    void osServiceFailureIsNotReportedAsAnUnmatchedPostcode() {
        UPSTREAM.stubFor(get(urlPathEqualTo("/postcode"))
            .willReturn(aResponse().withStatus(503)));

        assertThatThrownBy(() -> courtFinderService.getC100CourtOnboardingStatus("SA1 2FA"))
            .isInstanceOf(OsCourtLookupException.class)
            .hasCauseInstanceOf(FeignException.class);
        UPSTREAM.verify(0, getRequestedFor(urlPathEqualTo("/refdata/location/court-venues/services")));
    }

    @Test
    void successfulOsLookupWithNoResultsIsStillReportedAsAnUnmatchedPostcode() {
        UPSTREAM.stubFor(get(urlPathEqualTo("/postcode"))
            .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody("{\"results\":[]}")));

        assertThatThrownBy(() -> courtFinderService.getC100CourtOnboardingStatus("SA1 2FA"))
            .isInstanceOf(NotFoundException.class);
        UPSTREAM.verify(0, getRequestedFor(urlPathEqualTo("/refdata/location/court-venues/services")));
    }

    @Test
    void resolvesFl401SelectedVenueWithoutPostcodeLookup() throws Exception {
        CourtVenue court = courtFinderService.getNearestFamilyCourt(
            CaseData.builder().caseTypeOfApplication("FL401").courtId("234946").build());

        assertThat(court.getCourtEpimmsId()).isEqualTo("234946");
        UPSTREAM.verify(0, getRequestedFor(urlPathEqualTo("/postcode")));
        assertThat(UPSTREAM.getAllServeEvents()).hasSize(1);
    }
}
