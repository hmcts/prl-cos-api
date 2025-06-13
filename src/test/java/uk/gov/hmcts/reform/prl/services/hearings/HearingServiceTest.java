package uk.gov.hmcts.reform.prl.services.hearings;

import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedRequest;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.cafcass.RefDataService;
import uk.gov.hmcts.reform.prl.utils.AutomatedHearingTransactionRequestMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HearingServiceTest {

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
    @Mock
    CoreCaseDataApi coreCaseDataApi;
    CaseData caseData;
    private UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    PartyDetails applicant;
    PartyDetails respondent;

    @BeforeEach
    void init() {

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
            any(),
            any(),
            any()
        )).thenReturn(hearings);
        when(hearingApiClient.getHearingsByListOfCaseIds(
            any(),
            any(),
            any()
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
            any(),
            any(),
            any(),
            any()
        )).thenReturn(refDataCategoryValueMap);

        ReflectionTestUtils.setField(
            hearingService, "futureHearingStatusList",  Arrays.asList(
                "HEARING_REQUESTED","AWAITING_LISTING","LISTED","UPDATE_REQUESTED","UPDATE_SUBMITTED","EXCEPTION",
                "CANCELLATION_REQUESTED","CANCELLATION_SUBMITTED","AWAITING_ACTUALS"));

        ReflectionTestUtils.setField(
            hearingService, "hearingTypeCategoryId", "HearingType");

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder()
            .id(uuid)
            .value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);
        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder()
            .id(uuid)
            .value(respondent).build();
        List<Element<PartyDetails>> listOfRespondents = Collections.singletonList(wrappedRespondents);
        caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .caseTypeOfApplication("C100")
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .applicants(listOfApplicants)
            .respondents(listOfRespondents)
            //.orderCollection(List.of(element(uuid,orderDetails)))
            .build();

    }

    @Test
    @DisplayName("test case for HearingService getHearings success.")
    void getHearingsTestSuccess() {

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        Hearings hearingsResp = hearingService.getHearings(auth, caseReferenceNumber);

        assertNotNull(hearingsResp.getCaseHearings().getFirst().getNextHearingDate());
        assertEquals(true,hearingsResp.getCaseHearings().getFirst().isUrgentFlag());
    }

    @Test
    @DisplayName("test case for HearingService getHearings no hearings returned.")
    void getHearingsTestNoHearingReturned() {

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(hearingApiClient.getHearingDetails(auth, serviceAuthToken, caseReferenceNumber)).thenReturn(null);
        Hearings hearingsResp = hearingService.getHearings(auth, caseReferenceNumber);

        assertEquals(null, hearingsResp);

    }

    @Test
    @DisplayName("test case for HearingService getHearings exception.")
    void getHearingsTestException() {
        when(hearingApiClient.getHearingDetails(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenThrow(FeignException.errorStatus("getHearingDetails", Response.builder()
            .status(500)
            .reason("Internal Server Error")
            .request(Request.create(Request.HttpMethod.GET, "/hearings", Map.of(), null, null, null))
            .build()));
        Hearings response =
            hearingService.getHearings(auth, caseReferenceNumber);

        assertEquals(null, response);

    }

    @Test
    @DisplayName("test case for HearingService getHearings exception.")
    void getHearingsTestExceptionForRefData() {
        when(refDataService.getRefDataCategoryValueMap(
            any(),
            any(),
            any(),
            any()
        )).thenThrow(new RuntimeException());
        Hearings response =
            hearingService.getHearings(auth, caseReferenceNumber);

        assertEquals("", response.getCaseHearings().getFirst().getHearingTypeValue());

    }


    @Test
    @DisplayName("test case for HearingService getNextHearingDate success.")
    void getNextHearingDateTestSuccess() {

        NextHearingDetails nextHearingDetails = NextHearingDetails.builder().hearingID("2030006118")
            .hearingDateTime(LocalDateTime.now().plusDays(5)).build();
        when(hearingApiClient.getNextHearingDate(
            any(),
            any(),
            any()
        )).thenReturn(nextHearingDetails);

        NextHearingDetails response =
            hearingService.getNextHearingDate(auth, caseReferenceNumber);

        assertEquals("2030006118", response.getHearingID());
    }

    @Test
    @DisplayName("test case for HearingService getNextHearingDate exception .")
    void getNextHearingDateTestException() {
        when(hearingApiClient.getNextHearingDate(
            any(),
            any(),
            any()
        )).thenThrow(new RuntimeException());
        NextHearingDetails response =
            hearingService.getNextHearingDate(auth, caseReferenceNumber);

        assertEquals(null, response);
    }


    @Test
    @DisplayName("test case for HearingService getNextHearingDate success.")
    void getCaseLinkedDataTestSuccess() {
        CaseLinkedRequest caseLinkedRequest = CaseLinkedRequest.caseLinkedRequestWith().build();
        List<CaseLinkedData> caseLinkedDataList = new ArrayList<>();
        caseLinkedDataList.add(CaseLinkedData.caseLinkedDataWith().caseReference("123").build());

        List<CaseLinkedData> response =
            hearingService.getCaseLinkedData(auth,caseLinkedRequest);

        assertEquals(new ArrayList<>(), response);
    }

    @Test
    @DisplayName("test case for HearingService getNextHearingDate success.")
    void getCaseLinkedDataTestException() {
        CaseLinkedRequest caseLinkedRequest = CaseLinkedRequest.caseLinkedRequestWith().build();
        List<CaseLinkedData> caseLinkedDataList = new ArrayList<>();
        caseLinkedDataList.add(CaseLinkedData.caseLinkedDataWith().caseReference("123").build());

        when(hearingApiClient.getCaseLinkedData(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenThrow(FeignException.errorStatus("getHearingDetails", Response.builder()
            .status(500)
            .reason("Internal Server Error")
            .request(Request.create(Request.HttpMethod.POST, "/serviceLinkedCases", Map.of(), null, null, null))
            .build()));

        List<CaseLinkedData> response =
            hearingService.getCaseLinkedData(auth,caseLinkedRequest);

        assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService getFutureHearings success.")
    void getFutureHearingsTestSuccess() {
        when(hearingApiClient.getFutureHearings(
            any(),
            any(),
            any()
        )).thenReturn(hearings);
        Hearings response =
            hearingService.getFutureHearings(auth, caseReferenceNumber);

        assertEquals("2030006118", response.getCaseHearings().getFirst().getHearingID().toString());
    }

    @Test
    @DisplayName("test case for HearingService getFutureHearings exception .")
    void getFutureHearingsTestException() {
        when(hearingApiClient.getFutureHearings(
            any(),
            any(),
            any()
        )).thenThrow(new RuntimeException());

        Hearings response =
            hearingService.getFutureHearings(auth, caseReferenceNumber);

        assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService getHearings for given list of case ids success.")
    void getHearingsByListOfCaseIdsTestSuccess() {

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        Map<String, String> caseIds = new HashMap<>();
        caseIds.put(caseReferenceNumber, null);
        List<Hearings> hearingsResp = hearingService.getHearingsByListOfCaseIds(auth, caseIds);

        assertNotNull(hearingsResp);
        assertFalse(hearingsResp.isEmpty());
    }

    @Test
    @DisplayName("test case for HearingService getHearings for given list of case ids success.")
    void getHearingsByListOfCaseIdsTestException() {

        when(hearingApiClient.getHearingsByListOfCaseIds(
            any(),
            any(),
            any()
        )).thenThrow(new RuntimeException());

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        Map<String, String> caseIds = new HashMap<>();
        caseIds.put(caseReferenceNumber, null);
        List<Hearings> hearingsResp = hearingService.getHearingsByListOfCaseIds(auth, caseIds);

        assertTrue(hearingsResp.isEmpty());
    }

    @Test
    @DisplayName("test case for Automated Hearing Management.")
    void createAutomatedHearingManagementTestSuccess() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        AutomatedHearingCaseData automatedHearingCaseData = AutomatedHearingTransactionRequestMapper
            .mappingAutomatedHearingTransactionRequest(caseData, HearingData.builder().build());
        when(hearingApiClient.createAutomatedHearing(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
            .thenReturn(ResponseEntity.ok(AutomatedHearingResponse.builder().build()));
        AutomatedHearingResponse automatedHearingsResponse = hearingService.createAutomatedHearing(auth, automatedHearingCaseData);
        assertNotNull(automatedHearingsResponse);
    }

    @Test
    @DisplayName("test case for Automated Hearing Management.")
    void createAutomatedHearingManagementTestBadRequest() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        AutomatedHearingCaseData automatedHearingCaseData = AutomatedHearingTransactionRequestMapper
            .mappingAutomatedHearingTransactionRequest(caseData, HearingData.builder().build());
        when(hearingApiClient.createAutomatedHearing(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
            .thenThrow(new RuntimeException());
        AutomatedHearingResponse automatedHearingsResponse = hearingService.createAutomatedHearing(auth, automatedHearingCaseData);
        assertNull(automatedHearingsResponse);
    }

    @Test
    @DisplayName("test case for Automated Hearing Management.")
    void createAutomatedHearingManagementTestException() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(hearingApiClient.createAutomatedHearing(any(), any(), any())).thenThrow(new RuntimeException());
        AutomatedHearingCaseData automatedHearingCaseData = AutomatedHearingTransactionRequestMapper
            .mappingAutomatedHearingTransactionRequest(caseData, HearingData.builder().build());
        AutomatedHearingResponse automatedHearingsResponse = hearingService.createAutomatedHearing(auth, automatedHearingCaseData);
        assertNull(automatedHearingsResponse);
    }

    @Test
    @DisplayName("test case for Automated Hearing Management with failure response.")
    void createAutomatedHearingManagementTestFailure() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        AutomatedHearingCaseData automatedHearingCaseData = AutomatedHearingTransactionRequestMapper
            .mappingAutomatedHearingTransactionRequest(caseData, HearingData.builder().build());
        when(hearingApiClient.createAutomatedHearing(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
            .thenReturn(ResponseEntity.internalServerError().build());
        AutomatedHearingResponse automatedHearingsResponse = hearingService.createAutomatedHearing(auth, automatedHearingCaseData);
        assertNull(automatedHearingsResponse);

    }
}


