package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.LocationRefDataApi;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.court.CourtDetails;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_BASE_LOCATION_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_BASE_LOCATION_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_REGION_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_REGION_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FAMILY_COURT_TYPE_ID;
import static uk.gov.hmcts.reform.prl.services.LocationRefDataService.MIDLANDS;
import static uk.gov.hmcts.reform.prl.services.LocationRefDataService.SCOTLAND;

@ExtendWith(MockitoExtension.class)
class LocationRefDataServiceTest {


    @InjectMocks
    private LocationRefDataService locationRefDataService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private LocationRefDataApi locationRefDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn("");
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "1:email,2:email,3:email,4:email");
        ReflectionTestUtils.setField(locationRefDataService,"daCourtsToFilter", "1:email,2:email,3:email,4:email");
        ReflectionTestUtils.setField(locationRefDataService,"caDefaultCourtEpimmsID", "12345");
    }

    @Test
    void testgetCourtDetailsWithNullCourtDetails() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(null);
        List<DynamicListElement> courtLocations = locationRefDataService.getCourtLocations("test");
        assertTrue(courtLocations.isEmpty());
    }

    @Test
    void testDaGetCourtDetailsWithNullCourtDetails() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(null);
        List<DynamicListElement> courtLocations = locationRefDataService.getDaCourtLocations("test");
        assertTrue(courtLocations.isEmpty());
    }

    @Test
    void testgetCourtDetailsWithException() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenThrow(FeignException.class);
        List<DynamicListElement> courtLocations = locationRefDataService.getCourtLocations("test");
        assertNull(courtLocations.getFirst().getCode());
    }

    @Test
    void testDaGetCourtDetailsWithException() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenThrow(NullPointerException.class);
        List<DynamicListElement> courtLocations = locationRefDataService.getDaCourtLocations("test");
        assertNull(courtLocations.getFirst().getCode());
    }

    @Test
    void testgetCourtDetailsWithData() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region("r").regionId("id").courtName("1")
                                                     .region("test").siteName("test")
                                                     .courtEpimmsId("2")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        List<DynamicListElement> courtLocations = locationRefDataService.getCourtLocations("test");
        assertFalse(courtLocations.isEmpty());
    }

    @Test
    void testgetCourtDetailsWithDataNotMatched() {
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "email");
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region("r").regionId("id").courtName("1")
                                                     .region("test").siteName("test")
                                                     .courtEpimmsId("2")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        List<DynamicListElement> courtLocations = locationRefDataService.getCourtLocations("test");
        assertFalse(courtLocations.isEmpty());
    }


    @Test
    void testDaGetCourtDetailsWithData() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region("r").regionId("id").courtName("1")
                                                     .region("test").siteName("test")
                                                     .courtEpimmsId("2")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        List<DynamicListElement> courtLocations = locationRefDataService.getDaCourtLocations("test");
        assertFalse(courtLocations.isEmpty());
    }

    @Test
    void testDaGetCourtDetailsWithDataNotMatched() {
        ReflectionTestUtils.setField(locationRefDataService,"daCourtsToFilter", "email");
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region("r").regionId("id").courtName("1")
                                                     .region("test").siteName("test")
                                                     .courtEpimmsId("3")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        List<DynamicListElement> courtLocations = locationRefDataService.getDaCourtLocations("test");
        assertFalse(courtLocations.isEmpty());
    }

    @Test
    void testgetCourtDetailsWithNoData() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region("r").regionId("id").courtName("1")
                                                     .region("test").siteName("test")
                                                     .courtEpimmsId("2")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "");
        List<DynamicListElement> courtLocations = locationRefDataService.getCourtLocations("test");
        assertFalse(courtLocations.isEmpty());
    }

    @Test
    void testDaGetCourtDetailsWithNoData() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region("r").regionId("id").courtName("1")
                                                     .region("test").siteName("test")
                                                     .courtEpimmsId("2")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        ReflectionTestUtils.setField(locationRefDataService,"daCourtsToFilter", "");
        List<DynamicListElement> courtLocations = locationRefDataService.getDaCourtLocations("test");
        assertFalse(courtLocations.isEmpty());
    }

    @Test
    void testGetCourtDetailsFromEpimmsId() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region("r").regionId("id").courtName("1")
                                                     .region("test").siteName("test").postcode("123")
                                                     .courtEpimmsId("2")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId("2", "test");
        assertTrue(courtVenue.isPresent());
    }

    @Test
    void testCourtListWithoutEmail() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region("r").regionId("id").courtName("1")
                                                     .region("test").siteName("test")
                                                     .courtEpimmsId("2")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "1:email,2,3:email,4:email");
        List<DynamicListElement> test = locationRefDataService.getCourtLocations("test");
        assertNotNull(test);
    }

    @Test
    void testFilteredCourtEmail() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region("r").regionId("id").courtName("1")
                                                     .region("test").siteName("test")
                                                     .courtEpimmsId("2")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "1:email,2,3:email,4:email");
        List<DynamicListElement> test = locationRefDataService.getFilteredCourtLocations("test");
        assertNotNull(test);
    }

    @Test
    void testFilteredDaCourtEmail() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region("r").regionId("id").courtName("1")
                                                     .region("test").siteName("test")
                                                     .courtEpimmsId("2")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "1:email,2,3:email,4:email");
        List<DynamicListElement> test = locationRefDataService.getDaFilteredCourtLocations("test");
        assertNotNull(test);
    }

    @Test
    void testFilteredCourtEmailWithEmptyList() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenThrow(NullPointerException.class);
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "1:email,2,3:email,4:email");
        List<DynamicListElement> test = locationRefDataService.getFilteredCourtLocations("test");
        assertNotNull(test);
    }

    @Test
    void testFilteredDaCourtEmailWithEmptyList() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenThrow(NullPointerException.class);
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "1:email,2,3:email,4:email");
        List<DynamicListElement> test = locationRefDataService.getDaFilteredCourtLocations("test");
        assertNotNull(test);
    }

    @Test
    void testFilteredDaCourtEmailWithEmptyListWhenCourtListIsNull() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(null);
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "1:email,2,3:email,4:email");
        List<DynamicListElement> test = locationRefDataService.getDaFilteredCourtLocations("test");
        assertNotNull(test);
    }

    @Test
    void testGetCourtDetailsFromEpimmsIdEmptyCourtVenue() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .build());
        Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId("2", "test");
        assertTrue(courtVenue.isEmpty());
    }

    @Test
    void testGetCourtDetailsFromEpimmsIdForScotland() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region(SCOTLAND).regionId("id").courtName("1")
                                                     .region("test").siteName("test").postcode("123")
                                                     .courtEpimmsId("2")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId("2", "test");
        assertTrue(courtVenue.isPresent());
    }

    @Test
    void testDefaultCourtForCA() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region(MIDLANDS).regionId("id").courtName("Stoke")
                                                     .siteName("test").postcode("123")
                                                     .venueName("CTSC Stoke")
                                                     .courtEpimmsId("12345")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        CaseManagementLocation caseManagementLocation = locationRefDataService.getDefaultCourtForCA("test");
        assertEquals("CTSC Stoke", caseManagementLocation.getBaseLocationName());
        assertEquals(MIDLANDS, caseManagementLocation.getRegionName());
        assertEquals("id", caseManagementLocation.getRegion());
    }

    @Test
    void returnNullWhenDefaultCourtForCaIsNotConfigured() {
        CaseManagementLocation defaultCaseManagementLocation = CaseManagementLocation.builder()
            .region(C100_DEFAULT_REGION_ID)
            .baseLocation(C100_DEFAULT_BASE_LOCATION_ID).regionName(C100_DEFAULT_REGION_NAME)
            .baseLocationName(C100_DEFAULT_BASE_LOCATION_NAME).build();
        ReflectionTestUtils.setField(locationRefDataService,"caDefaultCourtEpimmsID", null);
        CaseManagementLocation caseManagementLocation = locationRefDataService.getDefaultCourtForCA("test");
        assertEquals(defaultCaseManagementLocation, caseManagementLocation);
    }

    @Test
    void returnNullWhenCourtVenueForCaIsNotConfigured() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of())
                            .build());
        CaseManagementLocation caseManagementLocation = locationRefDataService.getDefaultCourtForCA("test");
        assertEquals("CTSC Stoke", caseManagementLocation.getBaseLocationName());
        assertEquals(MIDLANDS, caseManagementLocation.getRegionName());
        assertEquals("2", caseManagementLocation.getRegion());
    }
}
