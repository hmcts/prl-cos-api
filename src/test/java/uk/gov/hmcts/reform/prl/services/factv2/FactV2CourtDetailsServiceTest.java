package uk.gov.hmcts.reform.prl.services.factv2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FactV2CourtDetailsServiceTest {
    private static final String SLUG = "swansea-civil-justice-centre";

    @Mock
    private FactV2Api factV2Api;
    @Mock
    private FactV2TokenService tokenService;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private CourtFinderApi currentCourtFinderApi;

    private FactV2CourtDetailsService service;

    @BeforeEach
    void setUp() {
        service = new FactV2CourtDetailsService(factV2Api, tokenService, systemUserService, currentCourtFinderApi);
    }

    @Test
    void mapsV2CourtDetailsAndKeepsEmailMetadataForExistingSelectionRules() {
        ReflectionTestUtils.setField(service, "enabled", true);
        FactV2CourtDetails details = courtDetails();
        when(tokenService.getAccessToken()).thenReturn("fact-token");
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
        assertEquals(1, court.getCourtEmailAddresses().size());
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
        verifyNoInteractions(factV2Api, tokenService, systemUserService);
    }

    @Test
    void returnsNullWhenCourtDetailsResponseIsNull() {
        ReflectionTestUtils.setField(service, "enabled", true);
        when(tokenService.getAccessToken()).thenReturn("fact-token");
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
        assertNull(service.getCourtDetailsFromFactUrl("https://example.test/"));
        assertNull(service.getCourtDetailsFromFactUrl("mailto:court@example.test"));
        verifyNoInteractions(factV2Api, tokenService, systemUserService, currentCourtFinderApi);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "https://www.find-court-tribunal.service.gov.uk/courts/" + SLUG,
        "https://www.find-court-tribunal.service.gov.uk/courts/" + SLUG + "/?lang=en",
        "https://www.find-court-tribunal.service.gov.uk/courts/" + SLUG + "?lang=en#contact"
    })
    void extractsSlugFromFinalFactUrlSegmentAndIgnoresTrailingSlashAndQuery(String factUrl) {
        ReflectionTestUtils.setField(service, "enabled", true);
        when(tokenService.getAccessToken()).thenReturn("fact-token");
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(systemUserService.getUserId("system-token")).thenReturn("system-user-id");
        when(factV2Api.getCourtDetailsBySlug(SLUG, "Bearer fact-token", "system-user-id"))
            .thenReturn(courtDetails());

        Court result = service.getCourtDetailsFromFactUrl(factUrl);

        assertEquals(SLUG, result.getCourtSlug());
    }

    @Test
    void rejectsMalformedFactUrlBeforeCallingEitherApi() {
        assertThrows(IllegalArgumentException.class,
            () -> service.getCourtDetailsFromFactUrl("not a valid URI"));

        verifyNoInteractions(factV2Api, tokenService, systemUserService, currentCourtFinderApi);
    }

    @Test
    void searchesFactV2ForNearestDomesticAbuseCourtAndLoadsSelectedCourtDetails() {
        ReflectionTestUtils.setField(service, "enabled", true);
        when(tokenService.getAccessToken()).thenReturn("fact-token");
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(systemUserService.getUserId("system-token")).thenReturn("system-user-id");
        FactV2CourtSearchResult searchResult = new FactV2CourtSearchResult();
        searchResult.setCourtId("court-id");
        searchResult.setCourtName("Swansea Civil Justice Centre");
        searchResult.setCourtSlug(SLUG);
        searchResult.setDistance(0.4);
        FactV2CourtSearchResult otherResult = new FactV2CourtSearchResult();
        otherResult.setCourtSlug("another-court");
        when(factV2Api.getCourtsByPostcode("SA1 2FA", "Domestic abuse", "NEAREST", 10,
            "Bearer fact-token", "system-user-id")).thenReturn(List.of(searchResult, otherResult));
        when(factV2Api.getCourtDetailsBySlug(SLUG, "Bearer fact-token", "system-user-id"))
            .thenReturn(courtDetails());

        Court result = service.getNearestDomesticAbuseCourt("SA1 2FA");

        assertEquals(SLUG, result.getCourtSlug());
        verify(factV2Api).getCourtsByPostcode("SA1 2FA", "Domestic abuse", "NEAREST", 10,
            "Bearer fact-token", "system-user-id");
        verify(factV2Api).getCourtDetailsBySlug(SLUG, "Bearer fact-token", "system-user-id");
        verify(factV2Api, never()).getCourtDetailsBySlug("another-court", "Bearer fact-token", "system-user-id");
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
        verifyNoInteractions(factV2Api, tokenService, systemUserService);
    }

    @ParameterizedTest
    @MethodSource("legacySearchResponsesWithoutCourts")
    void returnsNullWhenLegacyDomesticAbuseSearchHasNoCourts(ServiceArea response) {
        when(currentCourtFinderApi.findClosestDomesticAbuseCourtByPostCode("SA1 2FA"))
            .thenReturn(response);

        assertNull(service.getNearestDomesticAbuseCourt("SA1 2FA"));
        verify(currentCourtFinderApi).findClosestDomesticAbuseCourtByPostCode("SA1 2FA");
        verifyNoMoreInteractions(currentCourtFinderApi);
        verifyNoInteractions(factV2Api, tokenService, systemUserService);
    }

    @ParameterizedTest
    @MethodSource("v2SearchResponsesWithoutUsableCourt")
    void returnsNullWhenV2DomesticAbuseSearchHasNoUsableResults(List<FactV2CourtSearchResult> response) {
        ReflectionTestUtils.setField(service, "enabled", true);
        when(tokenService.getAccessToken()).thenReturn("fact-token");
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(systemUserService.getUserId("system-token")).thenReturn("system-user-id");
        when(factV2Api.getCourtsByPostcode("SA1 2FA", "Domestic abuse", "NEAREST", 10,
            "Bearer fact-token", "system-user-id")).thenReturn(response);

        assertNull(service.getNearestDomesticAbuseCourt("SA1 2FA"));
        verify(factV2Api, never()).getCourtDetailsBySlug(anyString(), anyString(), anyString());
        verifyNoInteractions(currentCourtFinderApi);
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
        assertFalse(court.isInPerson());
        assertFalse(court.isAccessScheme());
    }

    @Test
    void excludesPhoneOnlyAndBlankContactsAndKeepsAllPopulatedEmails() throws Exception {
        FactV2CourtDetails details = new ObjectMapper().readValue("""
            {"courtContactDetails":[
              null,
              {"email":null,"phoneNumber":"01234 567890",
               "courtContactDescription":{"name":"Family court enquiries"}},
              {"email":"","courtContactDescription":{"name":"Family court enquiries"}},
              {"email":"  ","courtContactDescription":{"name":"Family court enquiries"}},
              {"email":"family@example.test","explanation":"Family queries",
               "courtContactDescription":{"name":"Family court enquiries"}},
              {"email":"general@example.test","explanation":"General enquiries"}
            ]}
            """, FactV2CourtDetails.class);

        Court court = service.toCurrentCourt(details);

        assertEquals(List.of(
            CourtEmailAddress.builder().address("family@example.test")
                .description("Family court enquiries").explanation("Family queries").build(),
            CourtEmailAddress.builder().address("general@example.test").explanation("General enquiries").build()
        ), court.getCourtEmailAddresses());
        CourtFinderService selector = new CourtFinderService(
            currentCourtFinderApi, service, mock(OsCourtFinderService.class), mock(FeatureToggleService.class));
        assertEquals("family@example.test", selector.getEmailAddress(court).orElseThrow().getAddress());
    }

    @Test
    void preservesC100EmailSelectionWhenItIsNotTheFirstEmail() {
        FactV2CourtDetails details = courtDetails();
        FactV2CourtDetails.FactV2Contact c100Contact = new FactV2CourtDetails.FactV2Contact();
        c100Contact.setEmail("private.filing@example.test");
        c100Contact.setExplanation("Including C100 applications");
        details.setCourtContactDetails(List.of(details.getCourtContactDetails().getFirst(), c100Contact));

        Court court = service.toCurrentCourt(details);
        CourtFinderService selector = new CourtFinderService(
            currentCourtFinderApi, service, mock(OsCourtFinderService.class), mock(FeatureToggleService.class));

        assertEquals(2, court.getCourtEmailAddresses().size());
        assertEquals("private.filing@example.test", selector.getEmailAddress(court).orElseThrow().getAddress());
    }

    @Test
    void mapsSparsePostalAddressesAndDxCodesAndDeduplicatesCourtTypes() {
        FactV2CourtDetails.FactV2CourtType family = new FactV2CourtDetails.FactV2CourtType();
        family.setName("Family Court");
        FactV2CourtDetails.FactV2CourtType county = new FactV2CourtDetails.FactV2CourtType();
        county.setName("County Court");
        FactV2CourtDetails.FactV2Address postal = new FactV2CourtDetails.FactV2Address();
        postal.setAddressLine1("  ");
        postal.setAddressLine2("PO Box 1");
        postal.setPostcode("SA1 1SP");
        postal.setTownCity("Swansea");
        postal.setAddressType("WRITE_TO");
        postal.setCourtTypes(List.of(family, county, family, new FactV2CourtDetails.FactV2CourtType()));
        FactV2CourtDetails.FactV2DxCode dx = new FactV2CourtDetails.FactV2DxCode();
        dx.setDxCode("DX 123");
        FactV2CourtDetails details = new FactV2CourtDetails();
        details.setCourtAddresses(Arrays.asList(null, postal, new FactV2CourtDetails.FactV2Address()));
        details.setCourtDxCodes(Arrays.asList(null, new FactV2CourtDetails.FactV2DxCode(), dx));
        details.setCourtCodes(List.of());
        details.setCourtProfessionalInformation(List.of());

        Court court = service.toCurrentCourt(details);

        assertEquals(List.of("Family Court", "County Court"), court.getCourtTypes());
        assertEquals(2, court.getAddress().size());
        assertEquals(List.of("PO Box 1"), court.getAddress().getFirst().getAddressLines());
        assertEquals("SA1 1SP", court.getAddress().getFirst().getPostcode());
        assertEquals("Swansea", court.getAddress().getFirst().getTown());
        assertEquals("WRITE_TO", court.getAddress().getFirst().getType());
        assertTrue(court.getAddress().get(1).getAddressLines().isEmpty());
        assertEquals(List.of("DX 123"), court.getDxNumber());
        assertEquals(0L, court.getCountyLocationCode());
        assertFalse(court.isInPerson());
        assertFalse(court.isAccessScheme());
    }

    @Test
    void recognisesVisitUsAddressAsAnInPersonLocation() {
        FactV2CourtDetails details = courtDetails();
        details.getCourtAddresses().getFirst().setAddressType("VISIT_US");

        assertTrue(service.toCurrentCourt(details).isInPerson());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = false)
    void handlesMissingCountyCodeAndUnspecifiedOrDisabledAccessScheme(Boolean accessScheme) {
        FactV2CourtDetails details = courtDetails();
        details.getCourtCodes().getFirst().setCountyCourtCode(null);
        details.getCourtProfessionalInformation().getFirst().setAccessScheme(accessScheme);

        Court court = service.toCurrentCourt(details);

        assertEquals(0L, court.getCountyLocationCode());
        assertEquals("Y658", court.getGbs());
        assertFalse(court.isAccessScheme());
    }

    @Test
    void propagatesCourtApiFailureWithoutFallingBackToLegacy() {
        ReflectionTestUtils.setField(service, "enabled", true);
        when(tokenService.getAccessToken()).thenReturn("fact-token");
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(systemUserService.getUserId("system-token")).thenReturn("system-user-id");
        RuntimeException failure = new IllegalStateException("Court API unavailable");
        when(factV2Api.getCourtDetailsBySlug(SLUG, "Bearer fact-token", "system-user-id")).thenThrow(failure);

        assertSame(failure, assertThrows(IllegalStateException.class, () -> service.getCourtDetails(SLUG)));
        verifyNoInteractions(currentCourtFinderApi);
    }

    @Test
    void doesNotCallCourtApiWhenTokenAcquisitionFails() {
        ReflectionTestUtils.setField(service, "enabled", true);
        when(tokenService.getAccessToken()).thenThrow(new IllegalStateException("Token unavailable"));

        assertThrows(IllegalStateException.class, () -> service.getCourtDetails(SLUG));

        verifyNoInteractions(factV2Api, systemUserService, currentCourtFinderApi);
    }

    private static Stream<ServiceArea> legacySearchResponsesWithoutCourts() {
        return Stream.of(null, ServiceArea.builder().build(), ServiceArea.builder().courts(List.of()).build());
    }

    private static Stream<List<FactV2CourtSearchResult>> v2SearchResponsesWithoutUsableCourt() {
        return Stream.of(null, List.of(), List.of(new FactV2CourtSearchResult()));
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
