package uk.gov.hmcts.reform.prl.services.cafcass;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.cafcass.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HearingServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    private String authToken;
    private String s2sToken;

    private String caseReferenceNumber;

    @Mock
    private HearingApiClient hearingApiClient;

    private Hearings hearings;

    private Map<String, String> caseIdWithRegionIdMap;

    private List<Hearings> hearingsList;

    @InjectMocks
    private HearingService hearingService;


    @BeforeEach
    void setup() {

        s2sToken = "s2sToken";
        authToken = "Authorization";
        caseReferenceNumber = "1234567890";

        final List<CaseHearing> caseHearings = new ArrayList();

        final CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("LISTED").hearingType("ABA5-APL").hearingTypeValue("Appeal").hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith().hearingVenueName(
                            "BRENTFORD COUNTY COURT AND FAMILY COURT")
                        .hearingStartDateTime(LocalDateTime.parse("2023-01-24T13:00:00")).hearingEndDateTime(
                            LocalDateTime.parse(
                                "2023-01-24T16:00:00")).build())).build();

        caseHearings.add(caseHearing);

        hearings = new Hearings();
        hearings.setCaseRef("caseReference");
        hearings.setCaseHearings(caseHearings);

        CaseDetails caseDetails = CaseDetails.builder().caseTypeId("dsd").build();
        CaseManagementLocation caseManagementLocation = CaseManagementLocation.builder().build();
        caseIdWithRegionIdMap = new HashMap<>();
        caseIdWithRegionIdMap.put(
            String.valueOf(caseDetails.getId()),
            caseManagementLocation.getRegion() + "-" + caseManagementLocation.getBaseLocation()
        );
        hearingsList = new ArrayList<>();
        hearingsList.add(hearings);

    }

    @Test
    @DisplayName("test case for HearingService.")
    void getHearingsTestSuccess() {

        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService.")
    void getHearingsTestException() {
        when(authTokenGenerator.generate()).thenThrow(FeignException.errorStatus(
            "getHearingDetails", Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(Request.create(Request.HttpMethod.GET, "/hearings", Map.of(), null, null, null))
                .build()
        ));

        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService with Status LISTED.")
    void getHearingsTestSuccessHearingDataListed() {

        final List<CaseHearing> caseHearings = new ArrayList();

        final CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("LISTED").hearingType("ABA5-APL").hearingTypeValue("Appeal").hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith().hearingVenueName(
                            "BRENTFORD COUNTY COURT AND FAMILY COURT")
                        .hearingStartDateTime(LocalDateTime.parse("2023-01-24T13:00:00")).hearingEndDateTime(
                            LocalDateTime.parse(
                                "2023-01-24T16:00:00")).build())).build();

        caseHearings.add(caseHearing);

        hearings = new Hearings();
        hearings.setCaseRef("caseReference");
        hearings.setCaseHearings(caseHearings);

        ReflectionTestUtils.setField(hearingService, "hearingStatusList", List.of("LISTED"));
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(hearingApiClient.getHearingDetails(
            authToken,
            authTokenGenerator.generate(),
            caseReferenceNumber
        )).thenReturn(hearings);
        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        assertNotNull(response);
    }

    @Test
    @DisplayName("test case for HearingService with Status LISTED.")
    void getHearingsTestSuccessHearingDataNotListed() {

        final List<CaseHearing> caseHearings = new ArrayList();

        final CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("EXCEPTION").hearingType("ABA5-APL").hearingTypeValue("Appeal").hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith().hearingVenueName(
                            "BRENTFORD COUNTY COURT AND FAMILY COURT")
                        .hearingStartDateTime(LocalDateTime.parse("2023-01-24T13:00:00")).hearingEndDateTime(
                            LocalDateTime.parse(
                                "2023-01-24T16:00:00")).build())).build();

        caseHearings.add(caseHearing);

        hearings = new Hearings();
        hearings.setCaseRef("caseReference");
        hearings.setCaseHearings(caseHearings);

        ReflectionTestUtils.setField(hearingService, "hearingStatusList", List.of("LISTED"));
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(hearingApiClient.getHearingDetails(
            authToken,
            authTokenGenerator.generate(),
            caseReferenceNumber
        )).thenReturn(hearings);
        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        assertNull(response);
    }

    @Test
    @DisplayName("test case to all hearings of all cases.")
    void getHearingsForAllCases() {
        ReflectionTestUtils.setField(hearingService, "hearingStatusList", List.of("LISTED"));
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(hearingApiClient.getHearingDetailsForAllCaseIds(
            authToken,
            authTokenGenerator.generate(),
            caseIdWithRegionIdMap
        ))
            .thenReturn(hearingsList);
        List<Hearings> response =
            hearingService.getHearingsForAllCases(authToken, caseIdWithRegionIdMap);
        assertNotNull(response);
    }

    @Test
    @DisplayName("test case to all hearings of all cases exception")
    void getHearingsForAllCasesTestException() {
        when(authTokenGenerator.generate()).thenThrow(new RuntimeException());

        List<Hearings> response =
            hearingService.getHearingsForAllCases(authToken, caseIdWithRegionIdMap);

        assertEquals(Collections.emptyList(), response);

    }

    @Test
    @DisplayName("Should filter out unauthorized statuses and return only LISTED hearings")
    void shouldFilterAndReturnOnlyListedHearingsWhenMultipleStatusesExist() {

        CaseHearing valid = CaseHearing.caseHearingWith().hmcStatus("LISTED").build();
        CaseHearing invalid = CaseHearing.caseHearingWith().hmcStatus("EXCEPTION").build();
        Hearings localHearings = new Hearings();
        localHearings.setCaseRef("123");
        localHearings.setCaseHearings(new ArrayList<>(List.of(valid, invalid)));

        ReflectionTestUtils.setField(hearingService, "hearingStatusList", List.of("LISTED"));

        when(authTokenGenerator.generate()).thenReturn("s2s");
        when(hearingApiClient.getHearingDetails(anyString(), anyString(), anyString()))
            .thenReturn(localHearings);

        Hearings response = hearingService.getHearings("auth", "123");

        assertNotNull(response);
        assertEquals(1, response.getCaseHearings().size());
    }

    @Test
    @DisplayName("should return a null when the hearing APi is null")
    void shouldReturnNullWhenHearingApiIsNull() {
        when(authTokenGenerator.generate()).thenReturn("s2s");
        when(hearingApiClient.getHearingDetails(anyString(), anyString(), anyString()))
            .thenReturn(null);

        Hearings response = hearingService.getHearings("auth", "123");

        assertNull(response);
    }

    @Test
    @DisplayName("should return null when the hearing Api throws an exception")
    void shouldReturnNullWhenHearingApiThrowsAnException() {
        when(authTokenGenerator.generate()).thenReturn("s2s");
        when(hearingApiClient.getHearingDetails(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException());

        Hearings response = hearingService.getHearings("auth", "123");

        assertNull(response);
    }
}
