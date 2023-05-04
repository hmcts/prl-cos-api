package uk.gov.hmcts.reform.prl.services.cafcass;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.cafcass.HearingApiClient;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class HearingServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    private String authToken;
    private String s2sToken;

    private String caseReferenceNumber;

    @Mock
    private HearingApiClient hearingApiClient;

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

        assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService.")
    public void getHearingsTestException() {
        hearingApiClient = null;

        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        assertEquals(null, response);
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
    public void testGetHearingsForAllCases(){

        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseManagementLocation(CaseManagementLocation.builder()
                                        .regionId("1").baseLocationId("100").build())
            .build();
        List<Hearings> hearingListForAllCases = new ArrayList<>();
        List<CaseHearing> caseHearings = new ArrayList();

        CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("LISTED").hearingType("ABA5-APL").hearingTypeValue("Appeal").hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith().hearingVenueName("BRENTFORD COUNTY COURT AND FAMILY COURT")
                        .hearingStartDateTime(LocalDateTime.parse("2023-01-24T13:00:00")).hearingEndDateTime(LocalDateTime.parse(
                            "2023-01-24T16:00:00")).build())).build();

        caseHearings.add(caseHearing);
        caseHearings.add(caseHearing);

        hearings = new Hearings();
        hearings.setCaseRef("caseReference");
        hearings.setCaseHearings(caseHearings);

        hearingListForAllCases.add(hearings);

        Map<String, String> caseIdWithRegionIdMap = new HashMap<>();
        caseIdWithRegionIdMap.put(String.valueOf(caseData.getId()), caseData.getCaseManagementLocation().getRegionId()
            + "-" + caseData.getCaseManagementLocation().getBaseLocationId());

        ReflectionTestUtils.setField(hearingService, "hearingStatusList", List.of("LISTED"));
        when(authTokenGenerator.generate()).thenReturn(s2sToken);
        when(hearingApiClient.getHearingDetailsForAllCaseIds(authToken, authTokenGenerator.generate(), caseIdWithRegionIdMap)).thenReturn(hearingListForAllCases);
        hearingService.getHearingsForAllCases(authToken,caseIdWithRegionIdMap);

    }

    @Test
    public void testgetHearingsThrowsException() throws Exception{

        try {
            Map<String, String> caseIdWithRegionIdMap = new HashMap<>();

            hearingService.getHearingsForAllCases(authToken, caseIdWithRegionIdMap);
        }catch (Exception e) {
            assertEquals("Error while getHearingsForAllCases", e.getMessage());
            verify(log).error("Error while getHearingsForAllCases", Exception.class);
            }


    }


}
