package uk.gov.hmcts.reform.prl.services.hearings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.HmcHearingApiClient;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.cafcass.RefDataService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Integration test verifying prl-cos-api HearingService is wired to call HMC directly
 * (via HmcHearingApiClient) instead of routing through fis-hmc-api's /hearings endpoint,
 * and that venue/judge enrichment reuses prl-cos-api's existing LocationRefDataService
 * and RefDataUserService. Ported from fis-hmc-api HearingsControllerIntegrationTest.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class HearingServiceIntegrationTest {

    private static final String AUTH = "Bearer testAuthToken";
    private static final String S2S = "Bearer testServiceAuthToken";
    private static final String CASE_REFERENCE = "1234567890123456";

    @Autowired
    private HearingService hearingService;

    @MockitoBean
    private HmcHearingApiClient hmcHearingApiClient;

    @MockitoBean
    private SystemUserService systemUserService;

    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;

    @MockitoBean
    private LocationRefDataService locationRefDataService;

    @MockitoBean
    private RefDataUserService refDataUserService;

    @MockitoBean
    private RefDataService refDataService;

    @Before
    public void setUp() {
        Mockito.when(systemUserService.getSysUserToken()).thenReturn(AUTH);
        Mockito.when(authTokenGenerator.generate()).thenReturn(S2S);
        Mockito.when(refDataService.getRefDataCategoryValueMap(anyString(), anyString(), any(), anyString()))
            .thenReturn(Map.of("ABA5-FHR", "First hearing"));
    }

    @Test
    public void getHearings_callsHmcDirectlyAndEnrichesVenueAndJudge() {
        HearingDaySchedule schedule = HearingDaySchedule.hearingDayScheduleWith()
            .hearingVenueId("VENUE1")
            .hearingJudgeId("JUDGE1")
            .hearingStartDateTime(java.time.LocalDateTime.now().plusDays(1))
            .build();

        CaseHearing caseHearing = CaseHearing.caseHearingWith()
            .hearingID(2030006118L)
            .hmcStatus("LISTED")
            .hearingType("ABA5-FHR")
            .hearingDaySchedule(List.of(schedule))
            .build();

        Hearings hmcResponse = Hearings.hearingsWith()
            .caseRef(CASE_REFERENCE)
            .hmctsServiceCode("ABA5")
            .caseHearings(List.of(caseHearing))
            .build();

        Mockito.when(hmcHearingApiClient.getHearingDetails(
                eq(AUTH), eq(S2S), any(), any(), any(), eq(CASE_REFERENCE)))
            .thenReturn(hmcResponse);

        Mockito.when(locationRefDataService.getCourtDetailsFromEpimmsId(eq("VENUE1"), anyString()))
            .thenReturn(Optional.of(CourtVenue.builder()
                .courtEpimmsId("VENUE1")
                .venueName("Test Family Court")
                .courtAddress("1 Test Street")
                .build()));

        Mockito.when(refDataUserService.getAllJudicialUserDetails(any()))
            .thenReturn(List.of(JudicialUsersApiResponse.builder()
                .personalCode("JUDGE1")
                .fullName("Judge Test")
                .build()));

        Hearings result = hearingService.getHearings(AUTH, CASE_REFERENCE);

        assertNotNull(result);
        assertEquals(1, result.getCaseHearings().size());
        HearingDaySchedule enriched = result.getCaseHearings().get(0).getHearingDaySchedule().get(0);
        assertEquals("Test Family Court", enriched.getHearingVenueName());
        assertEquals("1 Test Street", enriched.getHearingVenueAddress());
        assertEquals("VENUE1", enriched.getHearingVenueLocationCode());
        assertEquals("Judge Test", enriched.getHearingJudgeName());

        // Verify direct HMC client was invoked (i.e. no FIS /hearings hop)
        Mockito.verify(hmcHearingApiClient).getHearingDetails(
            eq(AUTH), eq(S2S), any(), any(), any(), eq(CASE_REFERENCE));
    }

    @Test
    public void getHearings_returnsNullWhenHmcReturnsNull() {
        Mockito.when(hmcHearingApiClient.getHearingDetails(
                anyString(), anyString(), any(), any(), any(), eq(CASE_REFERENCE)))
            .thenReturn(null);

        Hearings result = hearingService.getHearings(AUTH, CASE_REFERENCE);

        assertNull(result);
    }
}

