package uk.gov.hmcts.reform.prl.services.courtnav;

import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.CourtSealFinderService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtLocationServiceTest {

    @Mock
    private CourtFinderService courtFinderService;

    @Mock
    private CourtSealFinderService courtSealFinderService;

    @Mock
    private LocationRefDataService locationRefDataService;

    @InjectMocks
    private CourtLocationService courtLocationService;

    private static final String AUTH = "Bearer: test-token";
    private static final String EPIMS_ID = "123456";

    private CaseData baseCaseData;

    @BeforeEach
    void setUp() {
        baseCaseData = CaseData.builder().build(); // adjust depending on your builder
    }

    @Test
    void shouldPopulateLocationFromRefDataWhenCourtVenueExists() throws NotFoundException {
        CourtVenue courtVenue = CourtVenue.builder()
            .courtName("Swansea Family Court")
            .region("Wales")
            .regionId("7")
            .build();

        when(locationRefDataService.getCourtDetailsFromEpimmsId(EPIMS_ID, AUTH))
            .thenReturn(Optional.of(courtVenue));

        when(courtSealFinderService.getCourtSeal("7")).thenReturn("sealImage");

        CaseData result = courtLocationService.getLocation(AUTH, baseCaseData, EPIMS_ID);

        assertEquals("Swansea Family Court", result.getCourtName());
        assertEquals(EPIMS_ID, result.getCourtId());
        assertEquals("7", result.getCaseManagementLocation().getRegionId());
        assertEquals("sealImage", result.getCourtSeal());
    }

    @Test
    void shouldPopulateLocationFromFactApiWhenCourtVenueNotFound() throws Exception {
        when(locationRefDataService.getCourtDetailsFromEpimmsId(EPIMS_ID, AUTH))
            .thenReturn(Optional.empty());

        Court court = Court.builder().courtName("Fallback Court").build();
        when(courtFinderService.getNearestFamilyCourt(baseCaseData)).thenReturn(court);
        when(courtFinderService.getEmailAddress(court)).thenReturn(Optional.of(
            CourtEmailAddress.builder().address("court@example.com").build()));

        CaseData result = courtLocationService.getLocation(AUTH, baseCaseData, EPIMS_ID);

        assertEquals("Fallback Court", result.getCourtName());
        assertEquals("court@example.com", result.getCourtEmailAddress());
    }
}
