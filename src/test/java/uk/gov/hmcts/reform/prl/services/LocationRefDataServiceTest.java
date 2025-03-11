package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.LocationRefDataApi;
import uk.gov.hmcts.reform.prl.enums.edgecases.EdgeCaseTypeOfApplicationEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.court.CourtDetails;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DssCaseDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_BASE_LOCATION_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_BASE_LOCATION_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_REGION_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_DEFAULT_REGION_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FAMILY_COURT_TYPE_ID;
import static uk.gov.hmcts.reform.prl.services.LocationRefDataService.MIDLANDS;
import static uk.gov.hmcts.reform.prl.services.LocationRefDataService.SCOTLAND;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LocationRefDataServiceTest {


    @InjectMocks
    private LocationRefDataService locationRefDataService;

    @Mock
    private LocationRefDataApi locationRefDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private CaseData caseData;

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn("");
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "1:email,2:email,3:email,4:email");
        ReflectionTestUtils.setField(locationRefDataService,"daCourtsToFilter", "1:email,2:email,3:email,4:email");
        ReflectionTestUtils.setField(locationRefDataService,"caDefaultCourtEpimmsID", "12345");
        ReflectionTestUtils.setField(locationRefDataService,"edgeCasesFgmFmpoCourtsToFilter", "12345");

        caseData = CaseData.builder().build();
    }

    @Test
    public void testgetCourtDetailsWithNullCourtDetails() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(null);
        List<DynamicListElement> courtLocations = locationRefDataService.getCourtLocations("test");
        assertTrue(courtLocations.isEmpty());
    }

    @Test
    public void testDaGetCourtDetailsWithNullCourtDetails() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(null);
        List<DynamicListElement> courtLocations = locationRefDataService.getDaCourtLocations("test");
        assertTrue(courtLocations.isEmpty());
    }

    @Test
    public void testgetCourtDetailsWithException() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenThrow(FeignException.class);
        List<DynamicListElement> courtLocations = locationRefDataService.getCourtLocations("test");
        assertNull(courtLocations.getFirst().getCode());
    }

    @Test
    public void testDaGetCourtDetailsWithException() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenThrow(NullPointerException.class);
        List<DynamicListElement> courtLocations = locationRefDataService.getDaCourtLocations("test");
        assertNull(courtLocations.getFirst().getCode());
    }

    @Test
    public void testgetCourtDetailsWithData() {
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
    public void testgetCourtDetailsWithDataNotMatched() {
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
    public void testDaGetCourtDetailsWithData() {
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
    public void testDaGetCourtDetailsWithDataNotMatched() {
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
    public void testgetCourtDetailsWithNoData() {
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
    public void testDaGetCourtDetailsWithNoData() {
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
    public void testGetCourtDetailsFromEpimmsId() {
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
    public void testCourtListWithoutEmail() {
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
    public void testFilteredCourtEmail() {
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
    public void testFilteredDaCourtEmail() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region("r").regionId("id").courtName("1")
                                                     .region("test").siteName("test")
                                                     .courtEpimmsId("2")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "1:email,2,3:email,4:email");
        List<DynamicListElement> test = locationRefDataService.getDaFilteredCourtLocations("test", caseData);
        assertNotNull(test);
    }

    @Test
    public void testFilteredCourtEmailWithEmptyList() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenThrow(NullPointerException.class);
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "1:email,2,3:email,4:email");
        List<DynamicListElement> test = locationRefDataService.getFilteredCourtLocations("test");
        assertNotNull(test);
    }

    @Test
    public void testFilteredDaCourtEmailWithEmptyList() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenThrow(NullPointerException.class);
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "1:email,2,3:email,4:email");
        List<DynamicListElement> test = locationRefDataService.getDaFilteredCourtLocations("test", caseData);
        assertNotNull(test);
    }

    @Test
    public void testFilteredDaCourtEmailWithEmptyListWhenCourtListIsNull() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(null);
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "1:email,2,3:email,4:email");
        List<DynamicListElement> test = locationRefDataService.getDaFilteredCourtLocations("test", caseData);
        assertNotNull(test);
    }

    @Test
    public void testGetCourtDetailsFromEpimmsIdEmptyCourtVenue() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .build());
        Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId("2", "test");
        assertTrue(courtVenue.isEmpty());
    }

    @Test
    public void testGetCourtDetailsFromEpimmsIdForScotland() {
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
    public void testDefaultCourtForCA() {
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
    public void returnNullWhenDefaultCourtForCaIsNotConfigured() {
        CaseManagementLocation defaultCaseManagementLocation = CaseManagementLocation.builder()
            .region(C100_DEFAULT_REGION_ID)
            .baseLocation(C100_DEFAULT_BASE_LOCATION_ID).regionName(C100_DEFAULT_REGION_NAME)
            .baseLocationName(C100_DEFAULT_BASE_LOCATION_NAME).build();
        ReflectionTestUtils.setField(locationRefDataService,"caDefaultCourtEpimmsID", null);
        CaseManagementLocation caseManagementLocation = locationRefDataService.getDefaultCourtForCA("test");
        assertEquals(defaultCaseManagementLocation, caseManagementLocation);
    }

    @Test
    public void returnNullWhenCourtVenueForCaIsNotConfigured() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of())
                            .build());
        CaseManagementLocation caseManagementLocation = locationRefDataService.getDefaultCourtForCA("test");
        assertEquals("CTSC Stoke", caseManagementLocation.getBaseLocationName());
        assertEquals(MIDLANDS, caseManagementLocation.getRegionName());
        assertEquals("2", caseManagementLocation.getRegion());
    }

    @Test
    public void testFilterEdgeCaseCourtsList() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region("r").regionId("id").courtName("1")
                                                     .region("test").siteName("test")
                                                     .courtEpimmsId("12345")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        caseData = caseData.toBuilder()
            .dssCaseDetails(DssCaseDetails.builder()
                                .edgeCaseTypeOfApplication(EdgeCaseTypeOfApplicationEnum.FGM)
                                .build())
            .build();
        List<DynamicListElement> test = locationRefDataService.getDaFilteredCourtLocations("test", caseData);
        assertNotNull(test);
    }

    @Test
    public void testFilterEdgeCaseCourtsListWithNoMatchingCourts() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region("r").regionId("id").courtName("1")
                                                     .region("test").siteName("test")
                                                     .courtEpimmsId("12345")
                                                     .courtTypeId(FAMILY_COURT_TYPE_ID).build()))
                            .build());
        caseData = caseData.toBuilder()
            .dssCaseDetails(DssCaseDetails.builder()
                                .edgeCaseTypeOfApplication(EdgeCaseTypeOfApplicationEnum.FMPO)
                                .build())
            .build();
        ReflectionTestUtils.setField(locationRefDataService,"edgeCasesFgmFmpoCourtsToFilter", "123");
        List<DynamicListElement> test = locationRefDataService.getDaFilteredCourtLocations("test", caseData);
        assertNotNull(test);
    }

    @Test
    public void testFilterEdgeCaseCourtsListWhenRefDataApiFails() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenThrow(NullPointerException.class);
        List<DynamicListElement> test = locationRefDataService.getDaFilteredCourtLocations("test", caseData);
        assertNotNull(test);
    }

    @Test
    public void testFilterEdgeCaseCourtsListWithEmptyCourtList() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(null);
        List<DynamicListElement> test = locationRefDataService.getDaFilteredCourtLocations("test", caseData);
        assertNotNull(test);
    }
}
