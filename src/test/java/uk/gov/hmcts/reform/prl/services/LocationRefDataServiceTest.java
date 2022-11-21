package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.LocationRefDataApi;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.court.CourtDetails;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

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
    }

    @Test
    public void testgetCourtDetailsWithNullCourtDetails() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(null);
        List<DynamicListElement> courtLocations = locationRefDataService.getCourtLocations("test");
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
    public void testgetCourtDetailsWithData() {
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(CourtDetails.builder()
                            .courtVenues(List.of(CourtVenue.builder().region("r").regionId("id").build()))
                            .build());
        List<DynamicListElement> courtLocations = locationRefDataService.getCourtLocations("test");
        assertFalse(courtLocations.isEmpty());
    }
}
