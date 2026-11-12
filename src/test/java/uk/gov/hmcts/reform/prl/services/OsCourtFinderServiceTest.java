package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.clients.os.OsCourtFinderApi;
import uk.gov.hmcts.reform.prl.models.LocalAuthorityCourt;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.Dpa;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.OsPlacesResponse;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.Result;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OsCourtFinderServiceTest {
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private OsCourtFinderApi osCourtFinderApi;
    @Mock
    private LocalAuthorityCourtDataLoader localAuthorityCourtDataLoader;
    @Mock
    private LocationRefDataService locationRefDataService;
    @InjectMocks
    private OsCourtFinderService osCourtFinderService;

    @Test
    void resolvesCourtUsingOsAndRefData() throws Exception {
        LocalAuthorityCourt mapping = LocalAuthorityCourt.builder().localCustodianCode("123")
            .epimmsId("234946").build();
        when(localAuthorityCourtDataLoader.getLocalAuthorityCourtList()).thenReturn(List.of(mapping));
        when(osCourtFinderApi.findCouncilByPostcode("SA1 2FA")).thenReturn(osResponse("123", "Swansea"));
        CourtVenue venue = CourtVenue.builder().courtEpimmsId("234946")
            .siteName("Swansea Civil Justice Centre").courtName("Swansea Civil And Family Justice Centre").build();
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(locationRefDataService.getCourtDetailsFromEpimmsId("234946", "system-token"))
            .thenReturn(Optional.of(venue));

        CourtVenue actual = osCourtFinderService.getC100NearestFamilyCourt("SA1 2FA");

        assertSame(venue, actual);
        assertEquals("Swansea Civil Justice Centre", actual.getCourtDisplayName());
        assertEquals("234946", actual.getCourtEpimmsId());
    }

    @Test
    void specificPostcodeMappingDoesNotCallOs() throws Exception {
        when(localAuthorityCourtDataLoader.getLocalAuthorityCourtList()).thenReturn(List.of(
            LocalAuthorityCourt.builder().specificPostCodes(List.of("N11")).epimmsId("123456").build()));
        CourtVenue venue = CourtVenue.builder().courtEpimmsId("123456")
            .siteName("Family Court").courtName("Formal Family Court").build();
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(locationRefDataService.getCourtDetailsFromEpimmsId("123456", "system-token"))
            .thenReturn(Optional.of(venue));

        assertSame(venue, osCourtFinderService.getC100NearestFamilyCourt("N11 1JD"));
        verifyNoInteractions(osCourtFinderApi);
        assertEquals("Family Court", venue.getCourtDisplayName());
    }

    @Test
    void returnsNullWhenNoLocalAuthorityMatches() throws Exception {
        when(localAuthorityCourtDataLoader.getLocalAuthorityCourtList()).thenReturn(List.of());
        when(osCourtFinderApi.findCouncilByPostcode("SA1 2FA")).thenReturn(osResponse("123", "Swansea"));

        assertNull(osCourtFinderService.getC100NearestFamilyCourt("SA1 2FA"));
        verifyNoInteractions(locationRefDataService, systemUserService);
    }

    @Test
    void returnsNullWhenVenueIsMissing() throws Exception {
        when(localAuthorityCourtDataLoader.getLocalAuthorityCourtList()).thenReturn(List.of(
            LocalAuthorityCourt.builder().specificPostCodes(List.of("N11")).epimmsId("123456").build()));
        when(systemUserService.getSysUserToken()).thenReturn("system-token");
        when(locationRefDataService.getCourtDetailsFromEpimmsId("123456", "system-token"))
            .thenReturn(Optional.empty());

        assertNull(osCourtFinderService.getC100NearestFamilyCourt("N11 1JD"));
    }

    @Test
    void rejectsMissingPostcode() {
        assertThrows(IllegalArgumentException.class, () -> osCourtFinderService.getC100NearestFamilyCourt(null));
        assertThrows(IllegalArgumentException.class, () -> osCourtFinderService.getC100NearestFamilyCourt(""));
        verifyNoInteractions(osCourtFinderApi, locationRefDataService);
    }

    @Test
    void skipsResultsWithoutCustodianDescription() throws NotFoundException {
        when(osCourtFinderApi.findCouncilByPostcode("SA1 2FA")).thenReturn(OsPlacesResponse.builder()
            .results(List.of(Result.builder().dpa(Dpa.builder().localCustodianCode("wrong").build()).build(),
                             osResponse("123", "Swansea").getResults().getFirst())).build());
        assertEquals("123", osCourtFinderService.getLocalCustodianCodeByPostCode("SA1 2FA"));
    }

    @Test
    void returnsNullWhenOsFails() throws Exception {
        when(osCourtFinderApi.findCouncilByPostcode("SA1 2FA")).thenThrow(new RuntimeException("unavailable"));
        assertNull(osCourtFinderService.getLocalCustodianCodeByPostCode("SA1 2FA"));
    }

    @Test
    void returnsNullWhenOsResponseIsEmpty() throws Exception {
        when(osCourtFinderApi.findCouncilByPostcode("SA1 2FA")).thenReturn(OsPlacesResponse.builder().build());
        assertNull(osCourtFinderService.getLocalCustodianCodeByPostCode("SA1 2FA"));
    }

    private OsPlacesResponse osResponse(String code, String description) {
        return OsPlacesResponse.builder().results(List.of(Result.builder().dpa(Dpa.builder()
            .localCustodianCode(code).localCustodianCodeDescription(description).build()).build())).build();
    }
}
