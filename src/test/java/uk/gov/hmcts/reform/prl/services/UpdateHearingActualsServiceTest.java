package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UpdateHearingActualTracking;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class UpdateHearingActualsServiceTest {
    private final String authToken = "authToken";
    private final String s2sAuthToken = "s2sAuthToken";
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private AllTabServiceImpl allTabService;
    @Mock
    private HearingApiClient hearingApiClient;
    private CaseDetails caseDetails;
    private CaseData caseData;
    private SearchResultResponse emptyResponse;
    private StartAllTabsUpdateDataContent startAllTabsUpdateDataContent;
    @InjectMocks
    private UpdateHearingActualsService updateHearingActualsService;
    @Captor
    private ArgumentCaptor<Map<String, Object>> caseDataUpdatedCaptor;

    @Before
    public void setUp() {
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn(s2sAuthToken);
        ReflectionTestUtils.setField(updateHearingActualsService, "concurrentRequest", 5);

        caseData = CaseData.builder()
            .id(123L)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();
        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();
        emptyResponse = SearchResultResponse.builder()
            .total(1)
            .cases(Collections.emptyList())
            .build();

        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult);

        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class)).thenReturn(response);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        Map<String, List<String>> caseIdHearigIdMap = new HashMap<>();
        caseIdHearigIdMap.put("123", Arrays.asList("123"));
        when(hearingApiClient.getListedHearingsForAllCaseIdsOnCurrentDate(any(), any(), anyList())).thenReturn(caseIdHearigIdMap);
        startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            s2sAuthToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(
                objectMapper),
            caseData,
            null
        );
        when(allTabService.getStartUpdateForSpecificEvent(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(startAllTabsUpdateDataContent);
    }

    @Test
    public void testUpdateHearingActualTaskCreatedSuccessfully() {

        caseData = caseData.toBuilder()
            .id(123L)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();
        caseDetails = caseDetails.toBuilder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();

        SearchResult searchResult1 = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult1);
        when(objectMapper.convertValue(searchResult1, SearchResultResponse.class))
            .thenReturn(response)
            .thenReturn(response)
            .thenReturn(emptyResponse);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        updateHearingActualsService.updateHearingActuals();
        verify(allTabService, times(2)).getStartUpdateForSpecificEvent(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testUpdateHearingActualTaskWhenHearingApiExceptionShouldContinueProcessingNextBatch() {

        caseData = caseData.toBuilder()
            .id(123L)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();
        caseDetails = caseDetails.toBuilder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();

        SearchResult searchResult1 = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult1);
        when(objectMapper.convertValue(searchResult1, SearchResultResponse.class))
            .thenReturn(response)
            .thenReturn(response)
            .thenReturn(emptyResponse);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        Map<String, List<String>> caseIdHearigIdMap = new HashMap<>();
        caseIdHearigIdMap.put("123", Arrays.asList("123"));
        when(hearingApiClient.getListedHearingsForAllCaseIdsOnCurrentDate(any(), any(), anyList()))
            .thenThrow(new RuntimeException("simulated failure for first execution"))
            .thenReturn(caseIdHearigIdMap);


        updateHearingActualsService.updateHearingActuals();
        verify(allTabService, times(1)).getStartUpdateForSpecificEvent(Mockito.anyString(), Mockito.anyString());
        verify(hearingApiClient, times(2))
            .getListedHearingsForAllCaseIdsOnCurrentDate(any(), any(), anyList());
    }

    @Test
    public void testUpdateHearingActualTaskWhenCaseEventFailsOnTheFirstCallContinuesWithTheSecondCall() {

        caseData = caseData.toBuilder()
            .id(123L)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();
        caseDetails = caseDetails.toBuilder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();

        SearchResult searchResult1 = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null))
            .thenReturn(searchResult1);
        when(objectMapper.convertValue(searchResult1, SearchResultResponse.class))
            .thenReturn(response)
            .thenReturn(emptyResponse);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(allTabService.submitAllTabsUpdate(any(), any(), any(), any(), any()))
            .thenThrow(new RuntimeException("simulated failure for first execution"))
            .thenReturn(caseDetails);
        Map<String, List<String>> caseIdHearigIdMap = new HashMap<>();
        caseIdHearigIdMap.put("123", Arrays.asList("123", "456"));
        when(hearingApiClient.getListedHearingsForAllCaseIdsOnCurrentDate(any(), any(), anyList())).thenReturn(caseIdHearigIdMap);

        updateHearingActualsService.updateHearingActuals();
        verify(allTabService, times(1)).getStartUpdateForSpecificEvent(Mockito.anyString(), Mockito.anyString());
    }


    @Test
    public void testUpdateHearingActualWhenUpdateHearingTrackerNotPresent() {
        caseData = caseData.toBuilder()
            .id(123L)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();
        ObjectMapper localObjectMapper = new ObjectMapper();
        localObjectMapper.findAndRegisterModules();
        caseDetails = caseDetails.toBuilder()
            .id(123L)
            .data(caseData.toMap(localObjectMapper))
            .build();

        SearchResult searchResult1 = SearchResult.builder().total(1).cases(List.of(caseDetails)).build();
        SearchResultResponse response = SearchResultResponse.builder().total(1).cases(List.of(caseDetails)).build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult1);
        when(objectMapper.convertValue(searchResult1, SearchResultResponse.class))
            .thenReturn(response)
            .thenReturn(emptyResponse);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent localStartAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            s2sAuthToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(
                objectMapper),
            caseData,
            null
        );
        when(allTabService.getStartUpdateForSpecificEvent("123", CaseEvent.ENABLE_UPDATE_HEARING_ACTUAL_TASK.getValue()))
            .thenReturn(localStartAllTabsUpdateDataContent);

        updateHearingActualsService.updateHearingActuals();

        verify(allTabService).getStartUpdateForSpecificEvent("123", CaseEvent.ENABLE_UPDATE_HEARING_ACTUAL_TASK.getValue());
        verify(allTabService).submitAllTabsUpdate(any(), any(), any(), any(), caseDataUpdatedCaptor.capture());

        CaseData modifiedCaseData = localObjectMapper.convertValue(
            caseDataUpdatedCaptor.getValue(),
            new TypeReference<>() {
            }
        );
        assertThat(modifiedCaseData.getUpdateHearingActualTracking())
            .extracting(Element::getValue)
            .anySatisfy(tracking -> {
                assertThat(tracking.getHearingId()).isEqualTo("123");
                assertThat(tracking.getLastFiredDate()).isToday();
            });
    }


    @Test
    public void testUpdateHearingActualWhenExistingTrackingHasLastFiredDateAsTodaySkipsTask() {
        Element<UpdateHearingActualTracking> existingTracking = element(
            UpdateHearingActualTracking.builder().hearingId("123").lastFiredDate(LocalDate.now()).build());

        List<Element<UpdateHearingActualTracking>> trackingByHearingIds = new ArrayList<>();
        trackingByHearingIds.add(existingTracking);
        caseData = caseData.toBuilder()
            .id(123L)
            .updateHearingActualTracking(trackingByHearingIds)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();
        ObjectMapper localObjectMapper = new ObjectMapper();
        localObjectMapper.findAndRegisterModules();
        caseDetails = caseDetails.toBuilder()
            .id(123L)
            .data(caseData.toMap(localObjectMapper))
            .build();

        SearchResult searchResult1 = SearchResult.builder().total(1).cases(List.of(caseDetails)).build();
        SearchResultResponse response = SearchResultResponse.builder().total(1).cases(List.of(caseDetails)).build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult1);
        when(objectMapper.convertValue(searchResult1, SearchResultResponse.class))
            .thenReturn(response)
            .thenReturn(emptyResponse);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        updateHearingActualsService.updateHearingActuals();

        verify(allTabService, never()).getStartUpdateForSpecificEvent("123", CaseEvent.ENABLE_UPDATE_HEARING_ACTUAL_TASK.getValue());
    }

    @Test
    public void testUpdateHearingActualWhenExistingTrackingHasLastFiredDateInThePastFireTask() {
        Element<UpdateHearingActualTracking> existingTracking = element(
            UpdateHearingActualTracking.builder().hearingId("123").lastFiredDate(LocalDate.now().minusDays(2)).build());

        List<Element<UpdateHearingActualTracking>> trackingByHearingIds = new ArrayList<>();
        trackingByHearingIds.add(existingTracking);
        caseData = caseData.toBuilder()
            .id(123L)
            .updateHearingActualTracking(trackingByHearingIds)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();
        ObjectMapper localObjectMapper = new ObjectMapper();
        localObjectMapper.findAndRegisterModules();
        caseDetails = caseDetails.toBuilder()
            .id(123L)
            .data(caseData.toMap(localObjectMapper))
            .build();

        SearchResult searchResult1 = SearchResult.builder().total(1).cases(List.of(caseDetails)).build();
        SearchResultResponse response = SearchResultResponse.builder().total(1).cases(List.of(caseDetails)).build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult1);
        when(objectMapper.convertValue(searchResult1, SearchResultResponse.class))
            .thenReturn(response)
            .thenReturn(emptyResponse);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent localStartAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            s2sAuthToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(
                objectMapper),
            caseData,
            null
        );
        when(allTabService.getStartUpdateForSpecificEvent("123", CaseEvent.ENABLE_UPDATE_HEARING_ACTUAL_TASK.getValue()))
            .thenReturn(localStartAllTabsUpdateDataContent);

        updateHearingActualsService.updateHearingActuals();

        verify(allTabService).getStartUpdateForSpecificEvent("123", CaseEvent.ENABLE_UPDATE_HEARING_ACTUAL_TASK.getValue());
        verify(allTabService).submitAllTabsUpdate(any(), any(), any(), any(), caseDataUpdatedCaptor.capture());

        CaseData modifiedCaseData = localObjectMapper.convertValue(
            caseDataUpdatedCaptor.getValue(),
            new TypeReference<>() {
            }
        );
        assertThat(modifiedCaseData.getUpdateHearingActualTracking())
            .extracting(Element::getValue)
            .anySatisfy(tracking -> {
                assertThat(tracking.getHearingId()).isEqualTo("123");
                assertThat(tracking.getLastFiredDate()).isToday();
            });
    }
}
