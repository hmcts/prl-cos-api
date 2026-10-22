package uk.gov.hmcts.reform.prl.services.factv2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.clients.factv2.FactV2Api;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.court.ServiceArea;
import uk.gov.hmcts.reform.prl.models.court.factv2.FactV2CourtDetails;
import uk.gov.hmcts.reform.prl.models.court.factv2.FactV2CourtSearchResult;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;
import uk.gov.hmcts.reform.prl.services.OsCourtFinderService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FactV2CourtDetailsServiceTest {
    private static final String SLUG = "swansea-civil-justice-centre";

    @Mock
    private FactV2Api factV2Api;
    @Mock
    private FactV2TokenProvider tokenProvider;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private CourtFinderApi currentCourtFinderApi;

    private FactV2CourtDetailsService service;

    @BeforeEach
    void setUp() {
        service = new FactV2CourtDetailsService(factV2Api, tokenProvider, systemUserService, currentCourtFinderApi);
    }

    @Test
    void mapsV2CourtDetailsIntoCurrentCourtModelAndKeepsEveryContactForExistingSelectionRules() {
        ReflectionTestUtils.setField(service, "enabled", true);
        FactV2CourtDetails details = courtDetails();
        when(tokenProvider.getAccessToken()).thenReturn("fact-token");
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(systemUserService.getUserId("system-token")).thenReturn("system-user-id");
        when(factV2Api.getCourtDetailsBySlug(SLUG, "Bearer fact-token", "system-user-id")).thenReturn(details);

        Court court = service.getCourtDetails(SLUG);

        assertEquals("Swansea Civil Justice Centre", court.getCourtName());
        assertEquals(SLUG, court.getCourtSlug());
        assertEquals(344L, court.getCountyLocationCode());
        assertEquals("Y658", court.getGbs());
        assertEquals(List.of("99740 Abertawe Swansea 5"), court.getDxNumber());
        assertTrue(court.isInPerson());
        assertTrue(court.isAccessScheme());
        assertEquals(2, court.getCourtEmailAddresses().size());
        assertEquals("Family court enquiries", court.getCourtEmailAddresses().getFirst().getDescription());
        assertNull(court.getCourtEmailAddresses().getFirst().getExplanation());
        assertEquals("family.swansea.countycourt@justice.gov.uk", court.getCourtEmailAddresses().getFirst().getAddress());
        assertEquals("Caravella House", court.getAddress().getFirst().getAddressLines().getFirst());
        CourtFinderService courtFinderService = new CourtFinderService(
            currentCourtFinderApi, service, mock(OsCourtFinderService.class), mock(FeatureToggleService.class));
        Optional<CourtEmailAddress> selectedAddress = courtFinderService.getEmailAddress(court);
        assertTrue(selectedAddress.isPresent());
        assertEquals("family.swansea.countycourt@justice.gov.uk",
            selectedAddress.orElseThrow().getAddress());
        verify(factV2Api).getCourtDetailsBySlug(SLUG, "Bearer fact-token", "system-user-id");
    }

    @Test
    void usesCurrentDetailsClientWhileV2IsDisabled() {
        Court expected = Court.builder().courtSlug(SLUG).build();
        when(currentCourtFinderApi.getCourtDetails(SLUG)).thenReturn(expected);

        assertEquals(expected, service.getCourtDetails(SLUG));
        verifyNoInteractions(factV2Api, tokenProvider, systemUserService);
    }

    @Test
    void returnsNullWhenCourtDetailsResponseIsNull() {
        ReflectionTestUtils.setField(service, "enabled", true);
        when(tokenProvider.getAccessToken()).thenReturn("fact-token");
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(systemUserService.getUserId("system-token")).thenReturn("system-user-id");
        when(factV2Api.getCourtDetailsBySlug(SLUG, "Bearer fact-token", "system-user-id")).thenReturn(null);

        assertNull(service.getCourtDetails(SLUG));
    }

    @Test
    void returnsNullForMissingOrBlankFactUrlPath() {
        assertNull(service.getCourtDetailsFromFactUrl(null));
        assertNull(service.getCourtDetailsFromFactUrl("  "));
        assertNull(service.getCourtDetailsFromFactUrl("https://example.test?query=value"));
        verifyNoInteractions(factV2Api, tokenProvider, systemUserService, currentCourtFinderApi);
    }

    @Test
    void extractsSlugFromFinalFactUrlSegmentAndIgnoresTrailingSlashAndQuery() {
        ReflectionTestUtils.setField(service, "enabled", true);
        when(tokenProvider.getAccessToken()).thenReturn("fact-token");
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(systemUserService.getUserId("system-token")).thenReturn("system-user-id");
        when(factV2Api.getCourtDetailsBySlug(SLUG, "Bearer fact-token", "system-user-id"))
            .thenReturn(courtDetails());

        Court result = service.getCourtDetailsFromFactUrl(
            "https://www.find-court-tribunal.service.gov.uk/courts/" + SLUG + "/?lang=en");

        assertEquals(SLUG, result.getCourtSlug());
    }

    @Test
    void searchesFactV2ForNearestDomesticAbuseCourtAndLoadsSelectedCourtDetails() {
        ReflectionTestUtils.setField(service, "enabled", true);
        when(tokenProvider.getAccessToken()).thenReturn("fact-token");
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(systemUserService.getUserId("system-token")).thenReturn("system-user-id");
        FactV2CourtSearchResult searchResult = new FactV2CourtSearchResult();
        searchResult.setCourtId("court-id");
        searchResult.setCourtName("Swansea Civil Justice Centre");
        searchResult.setCourtSlug(SLUG);
        searchResult.setDistance(0.4);
        when(factV2Api.getCourtsByPostcode("SA1 2FA", "Domestic abuse", "NEAREST", 10,
            "Bearer fact-token", "system-user-id")).thenReturn(List.of(searchResult));
        when(factV2Api.getCourtDetailsBySlug(SLUG, "Bearer fact-token", "system-user-id"))
            .thenReturn(courtDetails());

        Court result = service.getNearestDomesticAbuseCourt("SA1 2FA");

        assertEquals(SLUG, result.getCourtSlug());
        verify(factV2Api).getCourtsByPostcode("SA1 2FA", "Domestic abuse", "NEAREST", 10,
            "Bearer fact-token", "system-user-id");
        verify(factV2Api).getCourtDetailsBySlug(SLUG, "Bearer fact-token", "system-user-id");
    }

    @Test
    void usesLegacyDomesticAbuseSearchWhenV2IsDisabled() {
        Court expected = Court.builder().courtSlug(SLUG).build();
        when(currentCourtFinderApi.findClosestDomesticAbuseCourtByPostCode("SA1 2FA"))
            .thenReturn(ServiceArea.builder().courts(List.of(Court.builder().courtSlug(SLUG).build())).build());
        when(currentCourtFinderApi.getCourtDetails(SLUG)).thenReturn(expected);

        Court result = service.getNearestDomesticAbuseCourt("SA1 2FA");

        assertEquals(expected, result);
        verify(currentCourtFinderApi).findClosestDomesticAbuseCourtByPostCode("SA1 2FA");
        verifyNoInteractions(factV2Api, tokenProvider, systemUserService);
    }

    @Test
    void returnsNullWhenLegacyDomesticAbuseSearchHasNoCourts() {
        when(currentCourtFinderApi.findClosestDomesticAbuseCourtByPostCode("SA1 2FA"))
            .thenReturn(ServiceArea.builder().courts(List.of()).build());

        assertNull(service.getNearestDomesticAbuseCourt("SA1 2FA"));
        verify(currentCourtFinderApi).findClosestDomesticAbuseCourtByPostCode("SA1 2FA");
        verifyNoInteractions(factV2Api, tokenProvider, systemUserService);
    }

    @Test
    void returnsNullWhenV2DomesticAbuseSearchHasNoUsableResults() {
        ReflectionTestUtils.setField(service, "enabled", true);
        when(tokenProvider.getAccessToken()).thenReturn("fact-token");
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(systemUserService.getUserId("system-token")).thenReturn("system-user-id");
        when(factV2Api.getCourtsByPostcode("SA1 2FA", "Domestic abuse", "NEAREST", 10,
            "Bearer fact-token", "system-user-id")).thenReturn(List.of());

        assertNull(service.getNearestDomesticAbuseCourt("SA1 2FA"));
    }

    @Test
    void mapsNullDetailsToNullAndMapsEmptyDetailsToEmptyCollections() {
        assertNull(service.toCurrentCourt(null));

        FactV2CourtDetails details = new FactV2CourtDetails();
        Court court = service.toCurrentCourt(details);

        assertEquals(0L, court.getCountyLocationCode());
        assertTrue(court.getCourtTypes().isEmpty());
        assertTrue(court.getAddress().isEmpty());
        assertTrue(court.getDxNumber().isEmpty());
        assertTrue(court.getCourtEmailAddresses().isEmpty());
        assertTrue(!court.isInPerson());
        assertTrue(!court.isAccessScheme());
    }

    private FactV2CourtDetails courtDetails() {
        FactV2CourtDetails.FactV2CourtCode codes = new FactV2CourtDetails.FactV2CourtCode();
        codes.setCountyCourtCode(344L);
        codes.setFamilyCourtCode(344L);
        codes.setGbs("Y658");

        FactV2CourtDetails.FactV2Address address = new FactV2CourtDetails.FactV2Address();
        address.setAddressLine1("Caravella House");
        address.setAddressLine2("Quay Parade");
        address.setAddressType("VISIT_OR_CONTACT_US");
        address.setPostcode("SA1 1SP");
        address.setTownCity("Swansea");

        FactV2CourtDetails.FactV2ContactDescription description = new FactV2CourtDetails.FactV2ContactDescription();
        description.setName("Family court enquiries");
        FactV2CourtDetails.FactV2Contact familyContact = new FactV2CourtDetails.FactV2Contact();
        familyContact.setEmail("family.swansea.countycourt@justice.gov.uk");
        familyContact.setCourtContactDescription(description);

        FactV2CourtDetails.FactV2ContactDescription welshDescription = new FactV2CourtDetails.FactV2ContactDescription();
        welshDescription.setName("Welsh language helpline");
        FactV2CourtDetails.FactV2Contact welshContact = new FactV2CourtDetails.FactV2Contact();
        welshContact.setExplanation(null);
        welshContact.setCourtContactDescription(welshDescription);

        FactV2CourtDetails.FactV2DxCode dxCode = new FactV2CourtDetails.FactV2DxCode();
        dxCode.setDxCode("99740 Abertawe Swansea 5");
        FactV2CourtDetails.FactV2ProfessionalInformation professional = new FactV2CourtDetails.FactV2ProfessionalInformation();
        professional.setAccessScheme(true);

        FactV2CourtDetails details = new FactV2CourtDetails();
        details.setName("Swansea Civil Justice Centre");
        details.setSlug(SLUG);
        details.setOpen(true);
        details.setCourtAddresses(List.of(address));
        details.setCourtCodes(List.of(codes));
        details.setCourtDxCodes(List.of(dxCode));
        details.setCourtContactDetails(List.of(familyContact, welshContact));
        details.setCourtProfessionalInformation(List.of(professional));
        return details;
    }
}
