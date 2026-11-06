package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.clients.os.OsCourtFinderApi;
import uk.gov.hmcts.reform.prl.models.LocalAuthorityCourt;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.Dpa;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.OsPlacesResponse;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.Result;
import uk.gov.hmcts.reform.prl.services.factv2.FactV2CourtDetailsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class OsCourtFinderServiceTest {

    private static final String USER_TOKEN = "userToken";
    private static final String COURT_SLUG = "central-family-court";
    public static final String FACT_URL = "https://www.find-court-tribunal.service.gov.uk/courts/";

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private OsCourtFinderApi osCourtFinderApi;

    @Mock
    private LocalAuthorityCourtDataLoader localAuthorityCourtDataLoader;
    @Mock
    private LocationRefDataService locationRefDataService;
    @Mock
    private FactV2CourtDetailsService factV2CourtDetailsService;

    @InjectMocks
    private OsCourtFinderService osCourtFinderService;

    @Test
    void shouldFindCourt() throws NotFoundException {
        String epimmsId = "356855";
        String localCustodianCode = "5570";
        String localCustodianDescription = "Custodian Name";
        LocalAuthorityCourt localAuthorityCourt = LocalAuthorityCourt.builder()
            .localCustodianCode(localCustodianCode)
            .specificPostCodes(new ArrayList<>())
            .epimmsId(epimmsId)
            .build();

        List<LocalAuthorityCourt> localAuthorityCourtList = new ArrayList<>();
        localAuthorityCourtList.add(localAuthorityCourt);
        OsPlacesResponse osPlacesResponse = OsPlacesResponse.builder()
            .results(List.of(Result.builder()
                                 .dpa(Dpa.builder()
                                          .localCustodianCode(localCustodianCode)
                                          .localCustodianCodeDescription(localCustodianDescription).build())
                                 .build()))
            .build();

        String postcode = "EC2A2AB";

        when(osCourtFinderApi.findCouncilByPostcode(postcode)).thenReturn(osPlacesResponse);
        when(systemUserService.getSysUserToken()).thenReturn(USER_TOKEN);
        when(localAuthorityCourtDataLoader.getLocalAuthorityCourtList())
            .thenReturn(localAuthorityCourtList);
        CourtVenue courtVenue = CourtVenue.builder().factUrl(FACT_URL + COURT_SLUG).build();
        when(locationRefDataService.getCourtDetailsFromEpimmsId(epimmsId, USER_TOKEN))
            .thenReturn(Optional.of(courtVenue));

        when(factV2CourtDetailsService.getCourtDetailsFromFactUrl(FACT_URL + COURT_SLUG))
            .thenReturn(Court.builder().courtSlug(COURT_SLUG).build());

        ImmutablePair<CourtVenue, Court> courtPair = osCourtFinderService.getC100NearestFamilyCourtAndVenue(postcode);

        assertNotNull(courtPair);
        assertNotNull(courtPair.getLeft());
        assertNotNull(courtPair.getRight());
        assertEquals(courtVenue, courtPair.getLeft());
        assertEquals(COURT_SLUG, courtPair.getRight().getCourtSlug());

    }

    @Test
    void shouldFindCourtWhenSpecificPostCodePresent() throws NotFoundException {
        String epimmsId = "373584";
        String localCustodianCode = "5420";
        LocalAuthorityCourt localAuthorityCourt = LocalAuthorityCourt.builder()
            .localCustodianCode(localCustodianCode)
            .specificPostCodes(List.of("N2","N6","N8","N10","N11","N15","N17","N22"))
            .epimmsId(epimmsId)
            .build();

        List<LocalAuthorityCourt> localAuthorityCourtList = new ArrayList<>();
        localAuthorityCourtList.add(localAuthorityCourt);

        when(systemUserService.getSysUserToken()).thenReturn(USER_TOKEN);
        when(localAuthorityCourtDataLoader.getLocalAuthorityCourtList())
            .thenReturn(localAuthorityCourtList);
        CourtVenue courtVenue = CourtVenue.builder().factUrl(FACT_URL + COURT_SLUG).build();
        when(locationRefDataService.getCourtDetailsFromEpimmsId(epimmsId, USER_TOKEN))
            .thenReturn(Optional.of(courtVenue));

        when(factV2CourtDetailsService.getCourtDetailsFromFactUrl(FACT_URL + COURT_SLUG))
            .thenReturn(Court.builder().courtSlug(COURT_SLUG).build());

        ImmutablePair<CourtVenue, Court> courtPair = osCourtFinderService.getC100NearestFamilyCourtAndVenue("N111JD");

        assertNotNull(courtPair);
        assertNotNull(courtPair.getLeft());
        assertNotNull(courtPair.getRight());
        assertEquals(courtVenue, courtPair.getLeft());
        assertEquals(COURT_SLUG, courtPair.getRight().getCourtSlug());

        verifyNoInteractions(osCourtFinderApi);

    }

    @Test
    void shouldNotFindCourtWhenLocalAuthorityCourtIsEmpty() throws NotFoundException {
        String postcode = "N111JD";

        List<LocalAuthorityCourt> localAuthorityCourtList = new ArrayList<>();

        when(systemUserService.getSysUserToken()).thenReturn(USER_TOKEN);
        when(localAuthorityCourtDataLoader.getLocalAuthorityCourtList())
            .thenReturn(localAuthorityCourtList);

        Court court = osCourtFinderService.getC100NearestFamilyCourt(postcode);

        assertNull(court);
        verifyNoInteractions(locationRefDataService);
        verifyNoInteractions(factV2CourtDetailsService);
    }

    @Test
    void shouldReturnVenueWithoutFactCourtWhenFactUrlIsNull() throws NotFoundException {
        CourtVenue venue = CourtVenue.builder().factUrl(null).build();
        stubSpecificPostcodeVenue(venue);

        var result = osCourtFinderService.getC100NearestFamilyCourtAndVenue("N111JD");

        assertNotNull(result);
        assertEquals(venue, result.getLeft());
        assertNull(result.getRight());
        verifyNoInteractions(factV2CourtDetailsService);
    }

    @Test
    void shouldReturnVenueWithoutFactCourtWhenFactUrlIsBlank() throws NotFoundException {
        CourtVenue venue = CourtVenue.builder().factUrl(" ").build();
        stubSpecificPostcodeVenue(venue);

        var result = osCourtFinderService.getC100NearestFamilyCourtAndVenue("N111JD");

        assertNotNull(result);
        assertEquals(venue, result.getLeft());
        assertNull(result.getRight());
        verifyNoInteractions(factV2CourtDetailsService);
    }

    private void stubSpecificPostcodeVenue(CourtVenue venue) {
        LocalAuthorityCourt localAuthorityCourt = LocalAuthorityCourt.builder()
            .specificPostCodes(List.of("N1"))
            .epimmsId("venue-id")
            .build();
        when(localAuthorityCourtDataLoader.getLocalAuthorityCourtList()).thenReturn(List.of(localAuthorityCourt));
        when(systemUserService.getSysUserToken()).thenReturn(USER_TOKEN);
        when(locationRefDataService.getCourtDetailsFromEpimmsId("venue-id", USER_TOKEN)).thenReturn(Optional.of(venue));
    }

    @Test
    void shouldNotFindCourtWhenOsCourtFinderApiResponseIsNull() throws NotFoundException {
        String postcode = "EC2A2AB";
        String epimmsId = "373584";
        String localCustodianCode = "5420";
        LocalAuthorityCourt localAuthorityCourt = LocalAuthorityCourt.builder()
            .localCustodianCode(localCustodianCode)
            .specificPostCodes(new ArrayList<>())
            .epimmsId(epimmsId)
            .build();

        List<LocalAuthorityCourt> localAuthorityCourtList = new ArrayList<>();
        localAuthorityCourtList.add(localAuthorityCourt);

        when(osCourtFinderApi.findCouncilByPostcode(postcode)).thenReturn(OsPlacesResponse.builder().build());
        when(localAuthorityCourtDataLoader.getLocalAuthorityCourtList())
            .thenReturn(localAuthorityCourtList);

        Court court = osCourtFinderService.getC100NearestFamilyCourt(postcode);

        assertNull(court);
        verifyNoInteractions(locationRefDataService);
        verifyNoInteractions(factV2CourtDetailsService);
    }

    @Test
    void shouldReturnLocalCustodianCodeByPostCode() throws NotFoundException {
        String expectedValue = "123";
        String localCustodianDescription = "Custodian Name";
        OsPlacesResponse osPlacesResponse = OsPlacesResponse.builder()
            .results(List.of(Result.builder()
                                 .dpa(Dpa.builder()
                                          .localCustodianCode(expectedValue)
                                          .localCustodianCodeDescription(localCustodianDescription)
                                          .build())
                                 .build()))
            .build();
        when(osCourtFinderApi.findCouncilByPostcode("EC2A2AB")).thenReturn(osPlacesResponse);

        String result = osCourtFinderService.getLocalCustodianCodeByPostCode("EC2A2AB");

        assertEquals(expectedValue, result);
        verify(osCourtFinderApi).findCouncilByPostcode(anyString());
    }

    @Test
    void shouldReturnLocalCustodianCodeByPostCodeWhenOneDpaHasBlankCustodianDescription() throws NotFoundException {
        String localCustodianCode1 = "123";
        String localCustodianCode2 = "456";
        String localCustodianDescription = "Custodian Name";
        Result result1 = Result.builder().dpa(Dpa.builder()
                                                  .localCustodianCode(localCustodianCode1)
                                                  .build()).build();
        Result result2 = Result.builder().dpa(Dpa.builder()
                                                 .localCustodianCode(localCustodianCode2)
                                                  .localCustodianCodeDescription(localCustodianDescription)
                                                 .build()).build();
        OsPlacesResponse osPlacesResponse = OsPlacesResponse.builder()
            .results(List.of(result1, result2)).build();
        when(osCourtFinderApi.findCouncilByPostcode("EC2A2AB")).thenReturn(osPlacesResponse);

        String result = osCourtFinderService.getLocalCustodianCodeByPostCode("EC2A2AB");

        assertEquals(localCustodianCode2, result);
        verify(osCourtFinderApi).findCouncilByPostcode(anyString());
    }

    @Test
    void shouldReturnNullIfOsApiReturnNoResultByPostCode() throws NotFoundException {

        when(osCourtFinderApi.findCouncilByPostcode("EC2A2AB")).thenReturn(OsPlacesResponse.builder().build());

        String result = osCourtFinderService.getLocalCustodianCodeByPostCode("EC2A2AB");

        assertNull(result);
        verify(osCourtFinderApi).findCouncilByPostcode(anyString());
    }

    @Test
    void shouldReturnNullIfOsApiReturnNull() throws NotFoundException {

        when(osCourtFinderApi.findCouncilByPostcode("EC2A2AB")).thenReturn(null);

        String result = osCourtFinderService.getLocalCustodianCodeByPostCode("EC2A2AB");

        assertNull(result);
        verify(osCourtFinderApi).findCouncilByPostcode(anyString());
    }
}
