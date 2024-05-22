package uk.gov.hmcts.reform.prl.services.hearings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.cafcass.RefDataService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HearingServiceTest {

    @Value("#{'${hearing_component.futureHearingStatus}'.split(',')}")
    private List<String> futureHearingStatusList;

    @Value("${refdata.category-id}")
    private String hearingTypeCategoryId;

    @InjectMocks
    HearingService hearingService;

    @Mock
    RefDataService refDataService;

    @Mock
    private HearingApiClient hearingApiClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private final String auth = "auth-token";

    private final String serviceAuthToken = "Bearer testServiceAuth";

    private final String caseReferenceNumber = "caseReferenceNumber";

    private  HearingDaySchedule hearingDaySchedule;

    private CaseHearing caseHearing;

    private Hearings hearings;

    Map<String, String> refDataCategoryValueMap = new HashMap<>();

    @Before
    public void init() {

        LocalDateTime hearingStartDate = LocalDateTime.now().plusDays(5);
        hearingDaySchedule =
            HearingDaySchedule.hearingDayScheduleWith()
                .hearingStartDateTime(hearingStartDate)
                .build();
        List<HearingDaySchedule> hearingDayScheduleList = new ArrayList<>();
        hearingDayScheduleList.add(hearingDaySchedule);

        caseHearing = CaseHearing.caseHearingWith().hmcStatus("LISTED")
            .hearingType("ABA5-FFH")
            .hearingDaySchedule(hearingDayScheduleList)
            .hearingID(2030006118L).build();

        hearings = Hearings.hearingsWith()
            .caseRef(caseReferenceNumber)
            .hmctsServiceCode("ABA5")
            .caseHearings(Collections.singletonList(caseHearing))
            .build();
        when(hearingApiClient.getHearingDetails(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(hearings);
        when(hearingApiClient.getHearingsByListOfCaseIds(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(List.of(hearings));

        refDataCategoryValueMap.put("ABA5-FFH", "Full/Final hearing");
        refDataCategoryValueMap.put("ABA5-CHR", "Celebration hearing");
        refDataCategoryValueMap.put("ABA5-2GA", "2nd Gatekeeping Appointment");
        refDataCategoryValueMap.put("ABA5-FHR", "First hearing");
        refDataCategoryValueMap.put("ABA5-FRF", "Financial remedy first appointment");
        refDataCategoryValueMap.put("ABA5-FRD", "Financial remedy directions");
        refDataCategoryValueMap.put("ABA5-FHR", "Financial remedy interim order");
        refDataCategoryValueMap.put("ABA5-FRI", "Issues Resolution Hearing");

        when(refDataService.getRefDataCategoryValueMap(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(refDataCategoryValueMap);

        ReflectionTestUtils.setField(
            hearingService, "futureHearingStatusList",  Arrays.asList(
                "HEARING_REQUESTED","AWAITING_LISTING","LISTED","UPDATE_REQUESTED","UPDATE_SUBMITTED","EXCEPTION",
                "CANCELLATION_REQUESTED","CANCELLATION_SUBMITTED","AWAITING_ACTUALS"));

        ReflectionTestUtils.setField(
            hearingService, "hearingTypeCategoryId", "HearingType");

    }

    @Test
    @DisplayName("test case for HearingService getHearings success.")
    public void getHearingsTestSuccess() {

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        Hearings hearingsResp = hearingService.getHearings(auth, caseReferenceNumber);

        assertNotNull(hearingsResp.getCaseHearings().get(0).getNextHearingDate());
        assertEquals(true,hearingsResp.getCaseHearings().get(0).isUrgentFlag());
    }

    @Test
    @DisplayName("test case for HearingService getHearings no hearings returned.")
    public void getHearingsTestNoHearingReturned() {

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(hearingApiClient.getHearingDetails(auth, serviceAuthToken, caseReferenceNumber)).thenReturn(null);
        Hearings hearingsResp = hearingService.getHearings(auth, caseReferenceNumber);

        assertEquals(null, hearingsResp);

    }

    @Test
    @DisplayName("test case for HearingService getHearings exception.")
    public void getHearingsTestException() {
        when(hearingApiClient.getHearingDetails(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenThrow(new RuntimeException());
        Hearings response =
            hearingService.getHearings(auth, caseReferenceNumber);

        Assert.assertEquals(null, response);

    }

    @Test
    @DisplayName("test case for HearingService getHearings exception.")
    public void getHearingsTestExceptionForRefData() {
        when(refDataService.getRefDataCategoryValueMap(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenThrow(new RuntimeException());
        Hearings response =
            hearingService.getHearings(auth, caseReferenceNumber);

        Assert.assertEquals("", response.getCaseHearings().get(0).getHearingTypeValue());

    }


    @Test
    @DisplayName("test case for HearingService getNextHearingDate success.")
    public void getNextHearingDateTestSuccess() {

        NextHearingDetails nextHearingDetails = NextHearingDetails.builder().hearingID("2030006118")
            .hearingDateTime(LocalDateTime.now().plusDays(5)).build();
        when(hearingApiClient.getNextHearingDate(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(nextHearingDetails);

        NextHearingDetails response =
            hearingService.getNextHearingDate(auth, caseReferenceNumber);

        Assert.assertEquals("2030006118", response.getHearingID());
    }

    @Test
    @DisplayName("test case for HearingService getNextHearingDate exception .")
    public void getNextHearingDateTestException() {
        when(hearingApiClient.getNextHearingDate(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenThrow(new RuntimeException());
        NextHearingDetails response =
            hearingService.getNextHearingDate(auth, caseReferenceNumber);

        Assert.assertEquals(null, response);
    }


    @Test
    @DisplayName("test case for HearingService getNextHearingDate success.")
    public void getCaseLinkedDataTestSuccess() {
        CaseLinkedRequest caseLinkedRequest = CaseLinkedRequest.caseLinkedRequestWith().build();
        List<CaseLinkedData> caseLinkedDataList = new ArrayList<>();
        caseLinkedDataList.add(CaseLinkedData.caseLinkedDataWith().caseReference("123").build());

        List<CaseLinkedData> response =
            hearingService.getCaseLinkedData(auth,caseLinkedRequest);

        Assert.assertEquals(new ArrayList<>(), response);
    }

    @Test
    @DisplayName("test case for HearingService getNextHearingDate success.")
    public void getCaseLinkedDataTestException() {
        CaseLinkedRequest caseLinkedRequest = CaseLinkedRequest.caseLinkedRequestWith().build();
        List<CaseLinkedData> caseLinkedDataList = new ArrayList<>();
        caseLinkedDataList.add(CaseLinkedData.caseLinkedDataWith().caseReference("123").build());

        when(hearingApiClient.getCaseLinkedData(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenThrow(new RuntimeException());

        List<CaseLinkedData> response =
            hearingService.getCaseLinkedData(auth,caseLinkedRequest);

        Assert.assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService getFutureHearings success.")
    public void getFutureHearingsTestSuccess() {
        when(hearingApiClient.getFutureHearings(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(hearings);
        Hearings response =
            hearingService.getFutureHearings(auth, caseReferenceNumber);

        Assert.assertEquals("2030006118", response.getCaseHearings().get(0).getHearingID().toString());
    }

    @Test
    @DisplayName("test case for HearingService getFutureHearings exception .")
    public void getFutureHearingsTestException() {
        when(hearingApiClient.getFutureHearings(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenThrow(new RuntimeException());

        Hearings response =
            hearingService.getFutureHearings(auth, caseReferenceNumber);

        Assert.assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService getHearings for given list of case ids success.")
    public void getHearingsByListOfCaseIdsTestSuccess() {

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        Map<String, String> caseIds = new HashMap<>();
        caseIds.put(caseReferenceNumber, null);
        List<Hearings> hearingsResp = hearingService.getHearingsByListOfCaseIds(auth, caseIds);

        assertNotNull(hearingsResp);
        assertFalse(hearingsResp.isEmpty());
    }

    @Test
    @DisplayName("test case for HearingService getHearings for given list of case ids success.")
    public void getHearingsByListOfCaseIdsTestException() {

        when(hearingApiClient.getHearingsByListOfCaseIds(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenThrow(new RuntimeException());

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        Map<String, String> caseIds = new HashMap<>();
        caseIds.put(caseReferenceNumber, null);
        List<Hearings> hearingsResp = hearingService.getHearingsByListOfCaseIds(auth, caseIds);

        Assert.assertTrue(hearingsResp.isEmpty());
    }
}


