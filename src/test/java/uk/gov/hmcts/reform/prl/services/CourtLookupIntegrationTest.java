package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.clients.os.OsCourtFinderApi;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.Dpa;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.OsPlacesResponse;
import uk.gov.hmcts.reform.prl.models.ordnancesurvey.Result;
import uk.gov.hmcts.reform.prl.utils.csv.CsvReader;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CourtLookupIntegrationTest {

    private static final String SYSTEM_USER_TOKEN = "system-user-token";
    private static final String COURTS_BASE_URL = "https://www.find-court-tribunal.service.gov.uk/courts/";

    @Test
    void shouldResolveCurrentC100AreaPostcodesThroughOsAndLocalAuthorityMapping() throws Exception {
        OsCourtFinderApi osCourtFinderApi = mock(OsCourtFinderApi.class);
        SystemUserService systemUserService = mock(SystemUserService.class);
        LocationRefDataService locationRefDataService = mock(LocationRefDataService.class);
        CourtFinderApi courtFinderApi = mock(CourtFinderApi.class);
        FeatureToggleService featureToggleService = mock(FeatureToggleService.class);

        LocalAuthorityCourtDataLoader localAuthorityCourtDataLoader = new LocalAuthorityCourtDataLoader(new CsvReader());
        OsCourtFinderService osCourtFinderService = new OsCourtFinderService(
            systemUserService,
            osCourtFinderApi,
            localAuthorityCourtDataLoader,
            locationRefDataService,
            courtFinderApi
        );
        CourtFinderService courtFinderService = new CourtFinderService(
            courtFinderApi,
            osCourtFinderService,
            featureToggleService
        );

        when(featureToggleService.isOsCourtLookupFeatureEnabled()).thenReturn(true);
        when(systemUserService.getSysUserToken()).thenReturn(SYSTEM_USER_TOKEN);

        List<PostcodeCase> postcodeCases = new ObjectMapper().readValue(
            getClass().getResourceAsStream("/court-lookup/c100-live-postcode-cases.json"),
            new TypeReference<>() { }
        );

        for (PostcodeCase postcodeCase : postcodeCases) {
            when(osCourtFinderApi.findCouncilByPostcode(postcodeCase.postcode())).thenReturn(osResponse(postcodeCase));
            when(locationRefDataService.getCourtDetailsFromEpimmsId(postcodeCase.epimmsId(), SYSTEM_USER_TOKEN))
                .thenReturn(Optional.of(CourtVenue.builder()
                                            .factUrl(COURTS_BASE_URL + postcodeCase.courtSlug())
                                            .build()));
            when(courtFinderApi.getCourtDetails(postcodeCase.courtSlug()))
                .thenReturn(Court.builder().courtName(postcodeCase.courtName()).courtSlug(postcodeCase.courtSlug()).build());

            Court resolvedCourt = courtFinderService.getC100NearestFamilyCourt(postcodeCase.postcode());

            assertEquals(postcodeCase.courtName(), resolvedCourt.getCourtName(), postcodeCase.postcode());
        }

        postcodeCases.forEach(postcodeCase -> verify(osCourtFinderApi).findCouncilByPostcode(postcodeCase.postcode()));
        postcodeCases.stream().map(PostcodeCase::epimmsId).distinct().forEach(epimmsId -> {
            int expectedCalls = (int) postcodeCases.stream()
                .filter(postcodeCase -> postcodeCase.epimmsId().equals(epimmsId))
                .count();
            verify(locationRefDataService, times(expectedCalls))
                .getCourtDetailsFromEpimmsId(epimmsId, SYSTEM_USER_TOKEN);
        });
        postcodeCases.stream().map(PostcodeCase::courtSlug).distinct().forEach(courtSlug -> {
            int expectedCalls = (int) postcodeCases.stream()
                .filter(postcodeCase -> postcodeCase.courtSlug().equals(courtSlug))
                .count();
            verify(courtFinderApi, times(expectedCalls)).getCourtDetails(courtSlug);
        });

        verify(courtFinderApi, never()).findClosestChildArrangementsCourtByPostcode(anyString());
    }

    @Test
    void shouldResolveTwentyLiveFl401PostcodesThroughOsAndLocalAuthorityMapping() throws Exception {
        OsCourtFinderApi osCourtFinderApi = mock(OsCourtFinderApi.class);
        SystemUserService systemUserService = mock(SystemUserService.class);
        LocationRefDataService locationRefDataService = mock(LocationRefDataService.class);
        CourtFinderApi courtFinderApi = mock(CourtFinderApi.class);
        FeatureToggleService featureToggleService = mock(FeatureToggleService.class);

        LocalAuthorityCourtDataLoader localAuthorityCourtDataLoader = new LocalAuthorityCourtDataLoader(new CsvReader());
        OsCourtFinderService osCourtFinderService = new OsCourtFinderService(
            systemUserService,
            osCourtFinderApi,
            localAuthorityCourtDataLoader,
            locationRefDataService,
            courtFinderApi
        );
        CourtFinderService courtFinderService = new CourtFinderService(
            courtFinderApi,
            osCourtFinderService,
            featureToggleService
        );

        when(featureToggleService.isOsCourtLookupFeatureEnabled()).thenReturn(true);
        when(systemUserService.getSysUserToken()).thenReturn(SYSTEM_USER_TOKEN);

        List<Fl401PostcodeCase> postcodeCases = new ObjectMapper().readValue(
            getClass().getResourceAsStream("/court-lookup/fl401-live-postcode-cases.json"),
            new TypeReference<>() { }
        );

        assertEquals(20, postcodeCases.size());

        for (Fl401PostcodeCase postcodeCase : postcodeCases) {
            when(osCourtFinderApi.findCouncilByPostcode(postcodeCase.postcode()))
                .thenReturn(osResponse(postcodeCase.localCustodianCode()));
            when(locationRefDataService.getCourtDetailsFromEpimmsId(postcodeCase.epimmsId(), SYSTEM_USER_TOKEN))
                .thenReturn(Optional.of(CourtVenue.builder()
                                            .factUrl(COURTS_BASE_URL + postcodeCase.courtSlug())
                                            .build()));
            when(courtFinderApi.getCourtDetails(postcodeCase.courtSlug()))
                .thenReturn(Court.builder()
                                .courtName(postcodeCase.osMappedCourtName())
                                .courtSlug(postcodeCase.courtSlug())
                                .build());

            CaseData caseData = CaseData.builder()
                .caseTypeOfApplication("FL401")
                .applicantsFL401(PartyDetails.builder()
                                     .address(Address.builder().postCode(postcodeCase.postcode()).build())
                                     .build())
                .build();

            Court resolvedCourt = courtFinderService.getNearestFamilyCourt(caseData);

            assertEquals(postcodeCase.osMappedCourtName(), resolvedCourt.getCourtName(),
                         postcodeCase.postcode() + " (FaCT V1 baseline: " + postcodeCase.factV1CourtName() + ")");
            verify(osCourtFinderApi).findCouncilByPostcode(postcodeCase.postcode());
        }

        verify(courtFinderApi, never()).findClosestDomesticAbuseCourtByPostCode(anyString());
        verify(courtFinderApi, never()).findClosestChildArrangementsCourtByPostcode(anyString());
    }

    private static OsPlacesResponse osResponse(PostcodeCase postcodeCase) {
        return osResponse(postcodeCase.localCustodianCode());
    }

    private static OsPlacesResponse osResponse(String localCustodianCode) {
        return OsPlacesResponse.builder()
            .results(List.of(Result.builder()
                                 .dpa(Dpa.builder()
                                          .localCustodianCode(localCustodianCode)
                                          .localCustodianCodeDescription("Test local authority")
                                          .build())
                                 .build()))
            .build();
    }

    private record PostcodeCase(String postcode, String localCustodianCode, String epimmsId,
                                String courtSlug, String courtName) {
    }

    private record Fl401PostcodeCase(String postcode, String localCustodianCode, String epimmsId,
                                     String courtSlug, String osMappedCourtName, String factV1CourtName) {
    }
}
