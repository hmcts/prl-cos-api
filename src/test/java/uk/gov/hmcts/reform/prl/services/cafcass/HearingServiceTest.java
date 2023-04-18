package uk.gov.hmcts.reform.prl.services.cafcass;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.cafcass.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HearingServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    private String authToken;
    private String s2sToken;

    private String caseReferenceNumber;

    @Mock
    HearingApiClient hearingApiClient;

    private  Hearings hearings;

    @InjectMocks
    private HearingService hearingService;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        s2sToken = "s2sToken";
        authToken = "Authorization";

        caseReferenceNumber = "1234567890";
    }

    @Test
    @DisplayName("test case for HearingService.")
    public void getHearingsTestSuccess() {

        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        Assert.assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService.")
    public void getHearingsTestException() {

        when(hearingApiClient.getHearingDetails(any(),any(),any())).thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        Assert.assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService with Status LISTED.")
    public void getHearingsTestSuccessHearingDataListed() {

        final List<CaseHearing> caseHearings = new ArrayList();

        final CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("LISTED").hearingType("ABA5-APL").hearingTypeValue("Appeal").hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith().hearingVenueName("BRENTFORD COUNTY COURT AND FAMILY COURT")
                        .hearingStartDateTime(LocalDateTime.parse("2023-01-24T13:00:00")).hearingEndDateTime(LocalDateTime.parse(
                            "2023-01-24T16:00:00")).build())).build();

        caseHearings.add(caseHearing);

        hearings = new Hearings();
        hearings.setCaseRef("caseReference");
        hearings.setCaseHearings(caseHearings);

        ReflectionTestUtils.setField(hearingService, "hearingStatusList", List.of("LISTED"));
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(hearingApiClient.getHearingDetails(authToken, authTokenGenerator.generate(),caseReferenceNumber)).thenReturn(hearings);
        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        Assert.assertNotNull(response);
    }

    @Test
    @DisplayName("test case for HearingService with Status LISTED.")
    public void getHearingsTestSuccessHearingDataNotListed() {

        final List<CaseHearing> caseHearings = new ArrayList();

        final CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("EXCEPTION").hearingType("ABA5-APL").hearingTypeValue("Appeal").hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith().hearingVenueName("BRENTFORD COUNTY COURT AND FAMILY COURT")
                        .hearingStartDateTime(LocalDateTime.parse("2023-01-24T13:00:00")).hearingEndDateTime(LocalDateTime.parse(
                            "2023-01-24T16:00:00")).build())).build();

        caseHearings.add(caseHearing);

        hearings = new Hearings();
        hearings.setCaseRef("caseReference");
        hearings.setCaseHearings(caseHearings);

        ReflectionTestUtils.setField(hearingService, "hearingStatusList", List.of("LISTED"));
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(hearingApiClient.getHearingDetails(authToken, authTokenGenerator.generate(),caseReferenceNumber)).thenReturn(hearings);
        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        Assert.assertNull(response);
    }

    @Test
    @DisplayName("test case for HearingService to get all the hearings for all the caseIds.")
    public void getHearingsForAllCasesTestSuccess() {

        Map<String, String> caseIdWithRegionIdMap = new HashMap<>();
        caseIdWithRegionIdMap.put("testCaseId", "testRegionId");

        final List<CaseHearing> caseHearings = new ArrayList();

        final CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("LISTED").hearingType("ABA5-APL").hearingTypeValue("Appeal").hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith().hearingVenueName("BRENTFORD COUNTY COURT AND FAMILY COURT")
                        .hearingStartDateTime(LocalDateTime.parse("2023-01-24T13:00:00")).hearingEndDateTime(LocalDateTime.parse(
                            "2023-01-24T16:00:00")).build())).build();

        caseHearings.add(caseHearing);

        hearings = new Hearings();
        hearings.setCaseRef("caseReference");
        hearings.setCaseHearings(caseHearings);
        List<Hearings> listOfHearingDetails = new ArrayList<>();
        listOfHearingDetails.add(hearings);
        ReflectionTestUtils.setField(hearingService, "hearingStatusList", List.of("LISTED"));
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(hearingApiClient.getHearingDetailsForAllCaseIds(authToken, authTokenGenerator.generate(),caseIdWithRegionIdMap))
            .thenReturn(listOfHearingDetails);
        List<Hearings> response =
            hearingService.getHearingsForAllCases(authToken, caseIdWithRegionIdMap);
        Assert.assertNotNull(response);

    }

    @Test
    @DisplayName("test case for HearingService getHearingsForAllCases Exception.")
    public void getHearingsForAllCasesTestException() {

        Map<String, String> caseIdWithRegionIdMap = new HashMap<>();
        caseIdWithRegionIdMap.put("testCaseId", "testRegionId");

        when(hearingApiClient.getHearingDetailsForAllCaseIds(any(),any(),any())).thenThrow(new HttpServerErrorException(HttpStatus.BAD_GATEWAY));

        List<Hearings> response =
            hearingService.getHearingsForAllCases(authToken, caseIdWithRegionIdMap);

        Assert.assertEquals(null, response);
    }


}
