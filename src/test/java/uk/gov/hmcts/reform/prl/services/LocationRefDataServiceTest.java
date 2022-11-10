package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.clients.LocationRefDataApi;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.court.CourtDetails;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;

import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LocationRefDataServiceTest {


    @InjectMocks
    private LocationRefDataService locationRefDataService;

    @Mock
    private LocationRefDataApi locationRefDataApi;

    @Test
    public void testListOfOrdersCreated() {
        CourtDetails courtDetails = CourtDetails.builder()
            .courtType("Court")
            .courtVenues(List.of(CourtVenue.builder()
                                     .siteName(" ")
                                     .courtAddress(" ")
                                     .postcode(" ")
                                     .build()))
            .build();
        when(locationRefDataApi.getCourtDetailsByService(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(courtDetails);
        List<DynamicListElement> courtLocations = locationRefDataService.getCourtLocations("");
        assertNotNull(courtLocations);
    }
}
