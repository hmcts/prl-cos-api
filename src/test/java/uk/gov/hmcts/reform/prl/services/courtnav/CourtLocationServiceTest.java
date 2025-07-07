package uk.gov.hmcts.reform.prl.services.courtnav;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.exception.CourtLocationUnprocessableException;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CourtSealFinderService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtLocationServiceTest {

    @Mock
    private CourtSealFinderService courtSealFinderService;

    @Mock
    private LocationRefDataService locationRefDataService;

    @InjectMocks
    private CourtLocationService courtLocationService;

    private static final String AUTH = "Bearer: test-token";
    private static final String EPIMS_ID = "123456";

    @Test
    void shouldPopulateLocationWhenCourtVenueExists() {
        CaseData caseData = CaseData.builder()
            .specialCourtName(EPIMS_ID)
            .build();

        CourtVenue courtVenue = CourtVenue.builder()
            .courtName("Swansea Family Court")
            .region("Wales")
            .regionId("7")
            .build();

        when(locationRefDataService.getCourtDetailsFromEpimmsId(EPIMS_ID, AUTH))
            .thenReturn(Optional.of(courtVenue));

        when(courtSealFinderService.getCourtSeal("7")).thenReturn("sealImage");

        CaseData result = courtLocationService.populateCourtLocation(AUTH, caseData);

        assertEquals("Swansea Family Court", result.getCourtName());
        assertEquals("7", result.getCaseManagementLocation().getRegion());
        assertEquals("Wales", result.getCaseManagementLocation().getRegionName());
        assertEquals("123456", result.getCaseManagementLocation().getBaseLocation());
    }

    @Test
    void shouldThrowWhenCourtVenueNotFound() {
        CaseData caseData = CaseData.builder()
            .specialCourtName("999999")
            .build();

        when(locationRefDataService.getCourtDetailsFromEpimmsId("999999", AUTH))
            .thenReturn(Optional.empty());

        assertThrows(CourtLocationUnprocessableException.class,
                     () -> courtLocationService.populateCourtLocation(AUTH, caseData));
    }

    @Test
    void shouldThrowWhenEpimsIdIsMissing() {
        CaseData caseData = CaseData.builder()
            .build();

        assertThrows(CourtLocationUnprocessableException.class,
                     () -> courtLocationService.populateCourtLocation(AUTH, caseData));
    }

    @Test
    void shouldThrowWhenCourtVenueIsMissingRequiredFields() {
        CaseData caseData = CaseData.builder()
            .specialCourtName(EPIMS_ID)
            .build();

        CourtVenue incompleteVenue = CourtVenue.builder()
            .courtName("Some Court")
            .region(null)
            .regionId("7")
            .build();

        when(locationRefDataService.getCourtDetailsFromEpimmsId("123456", AUTH))
            .thenReturn(Optional.of(incompleteVenue));

        assertThrows(CourtLocationUnprocessableException.class,
                     () -> courtLocationService.populateCourtLocation(AUTH, caseData));
    }
}
