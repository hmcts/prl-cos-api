package uk.gov.hmcts.reform.prl.services.hearings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Ignore
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

    @Before
    public void setup() {
        final List<CaseHearing> caseHearings = new ArrayList();

        final CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("LISTED").hearingType("ABA5-APL").hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith().hearingVenueName("BRENTFORD COUNTY COURT AND FAMILY COURT")
                        .hearingStartDateTime(LocalDateTime.parse("2023-01-24T13:00:00")).hearingEndDateTime(LocalDateTime.parse(
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

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        s2sToken = "s2sToken";
        authToken = "Authorization";

        caseReferenceNumber = "1234567890";

    }

    @Test
    @DisplayName("test case for HearingService getHearings success.")
    public void getHearingsTestSuccess() {
        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        Assert.assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService getHearings exception.")
    public void getHearingsTestException() {
        when(hearingService.getHearings(authToken,caseReferenceNumber)).thenThrow(new RuntimeException());
        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        Assert.assertEquals(null, response);

    }

    @Test
    @DisplayName("test case for HearingService getNextHearingDate success.")
    public void getNextHearingDateTestSuccess() {

        NextHearingDetails response =
            hearingService.getNextHearingDate(authToken, caseReferenceNumber);

        Assert.assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService getNextHearingDate exception .")
    public void getNextHearingDateTestException() {
        when(hearingService.getNextHearingDate(authToken,caseReferenceNumber)).thenThrow(new RuntimeException());
        NextHearingDetails response =
            hearingService.getNextHearingDate(authToken, caseReferenceNumber);

        Assert.assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService getNextHearingDate success.")
    public void getCaseLinkedDataTestSuccess() {
        CaseLinkedRequest caseLinkedRequest = CaseLinkedRequest.caseLinkedRequestWith().build();
        List<CaseLinkedData> caseLinkedDataList = new ArrayList<>();
        caseLinkedDataList.add(CaseLinkedData.caseLinkedDataWith().caseReference("123").build());

        List<CaseLinkedData> response =
            hearingService.getCaseLinkedData(authToken,caseLinkedRequest);

        Assert.assertEquals(new ArrayList<>(), response);
    }

    @Test
    @DisplayName("test case for HearingService getNextHearingDate success.")
    public void getCaseLinkedDataTestException() {
        CaseLinkedRequest caseLinkedRequest = CaseLinkedRequest.caseLinkedRequestWith().build();
        List<CaseLinkedData> caseLinkedDataList = new ArrayList<>();
        caseLinkedDataList.add(CaseLinkedData.caseLinkedDataWith().caseReference("123").build());

        when(hearingService.getCaseLinkedData(authToken,caseLinkedRequest)).thenThrow(new RuntimeException());

        List<CaseLinkedData> response =
            hearingService.getCaseLinkedData(authToken,caseLinkedRequest);

        Assert.assertEquals(new ArrayList<>(), response);
    }

    @Test
    @DisplayName("test case for HearingService getFutureHearings success.")
    public void getFutureHearingsTestSuccess() {
        Hearings response =
            hearingService.getFutureHearings(authToken, caseReferenceNumber);

        Assert.assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService getFutureHearings exception .")
    public void getFutureHearingsTestException() {
        when(hearingService.getFutureHearings(authToken,caseReferenceNumber)).thenThrow(new RuntimeException());
        Hearings response =
            hearingService.getFutureHearings(authToken, caseReferenceNumber);

        Assert.assertEquals(null, response);
    }



}
