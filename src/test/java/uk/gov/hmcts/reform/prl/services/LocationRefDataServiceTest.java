package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.court.CourtDetails;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FAMILY_COURT_TYPE_ID;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LocationRefDataServiceTest {


    @InjectMocks
    private LocationRefDataService locationRefDataService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private LocationRefDataApi locationRefDataApi;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn("");
        ReflectionTestUtils.setField(locationRefDataService,"courtsToFilter", "1:email,2:email,3:email,4:email");
        ReflectionTestUtils.setField(locationRefDataService,"daCourtsToFilter", "1:email,2:email,3:email,4:email");
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
            .thenThrow(NullPointerException.class);
        List<DynamicListElement> courtLocations = locationRefDataService.getCourtLocations("test");
        assertNull(courtLocations.get(0).getCode());
    }

    @Test
    public void testDaGetCourtDetailsWithException() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenThrow(NullPointerException.class);
        List<DynamicListElement> courtLocations = locationRefDataService.getDaCourtLocations("test");
        assertNull(courtLocations.get(0).getCode());
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
}
