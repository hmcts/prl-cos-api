package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UpdateHearingActualTracking;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    private StartAllTabsUpdateDataContent startAllTabsUpdateDataContent;
    @InjectMocks
    private UpdateHearingActualsService updateHearingActualsService;

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
            .draftOrderCollection(List.of(element(DraftOrder.builder()
                                                      .manageOrderHearingDetails(
                                                          List.of(element(HearingData.builder()
                                                                              .confirmedHearingDates(DynamicList.builder()
                                                                                                         .value(
                                                                                                             DynamicListElement.builder().code(
                                                                                                                 "1234").build()).build())
                                                                              .build())))
                                                      .build())))

            .orderCollection(List.of(element(OrderDetails.builder()
                                                 .manageOrderHearingDetails(
                                                     List.of(element(HearingData.builder()
                                                                         .confirmedHearingDates(DynamicList.builder()
                                                                                                    .value(
                                                                                                        DynamicListElement.builder().code(
                                                                                                            "1234").build()).build())
                                                                         .build())))
                                                 .build())))
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
        when(objectMapper.convertValue(searchResult1, SearchResultResponse.class)).thenReturn(response);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        updateHearingActualsService.updateHearingActuals();
        verify(allTabService, times(1)).getStartUpdateForSpecificEvent(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testUpdateHearingActualTaskWhenException() {

        caseData = caseData.toBuilder()
            .id(123L)
            .draftOrderCollection(List.of(element(DraftOrder.builder()
                                                      .manageOrderHearingDetails(
                                                          List.of(element(HearingData.builder()
                                                                              .confirmedHearingDates(DynamicList.builder()
                                                                                                         .value(
                                                                                                             DynamicListElement.builder().code(
                                                                                                                 "1234").build()).build())
                                                                              .build())))
                                                      .build())))

            .orderCollection(List.of(element(OrderDetails.builder()
                                                 .manageOrderHearingDetails(
                                                     List.of(element(HearingData.builder()
                                                                         .confirmedHearingDates(DynamicList.builder()
                                                                                                    .value(
                                                                                                        DynamicListElement.builder().code(
                                                                                                            "1234").build()).build())
                                                                         .build())))
                                                 .build())))
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
        when(objectMapper.convertValue(searchResult1, SearchResultResponse.class)).thenReturn(response);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        doThrow(FeignException.class).when(hearingApiClient).getListedHearingsForAllCaseIdsOnCurrentDate(any(), any(), anyList());

        updateHearingActualsService.updateHearingActuals();
        verifyNoInteractions(allTabService);
    }

    @Test
    public void testUpdateHearingActualTaskWhenHearingDataForDraftOrderIsNull() {

        caseData = caseData.toBuilder()
            .id(123L)
            .draftOrderCollection(List.of(element(DraftOrder.builder()
                                                      .manageOrderHearingDetails(
                                                          List.of(element(HearingData.builder()
                                                                              .confirmedHearingDates(DynamicList.builder().build())
                                                                              .build())))
                                                      .build())))

            .orderCollection(List.of(element(OrderDetails.builder()
                                                 .manageOrderHearingDetails(
                                                     List.of(element(HearingData.builder()
                                                                         .confirmedHearingDates(DynamicList.builder()
                                                                                                    .value(
                                                                                                        DynamicListElement.builder().code(
                                                                                                            "1234").build()).build())
                                                                         .build())))
                                                 .build())))
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
        when(objectMapper.convertValue(searchResult1, SearchResultResponse.class)).thenReturn(response);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        updateHearingActualsService.updateHearingActuals();
        verify(allTabService, times(1)).getStartUpdateForSpecificEvent(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testUpdateHearingActualTaskWhenHearingDataForOrderIsNull() {

        caseData = caseData.toBuilder()
            .id(123L)
            .draftOrderCollection(List.of(element(DraftOrder.builder()
                                                      .manageOrderHearingDetails(
                                                          List.of(element(HearingData.builder()
                                                                              .confirmedHearingDates(DynamicList.builder()
                                                                                                         .value(
                                                                                                             DynamicListElement.builder().code(
                                                                                                                 "1234").build()).build())
                                                                              .build())))
                                                      .build())))

            .orderCollection(List.of(element(OrderDetails.builder()
                                                 .manageOrderHearingDetails(
                                                     List.of(element(HearingData.builder()
                                                                         .confirmedHearingDates(DynamicList.builder().build())
                                                                         .build())))
                                                 .build())))
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
        when(objectMapper.convertValue(searchResult1, SearchResultResponse.class)).thenReturn(response);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        updateHearingActualsService.updateHearingActuals();
        verify(allTabService, times(1)).getStartUpdateForSpecificEvent(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testUpdateHearingActualWhenExistingTrackingHasNoLastFiredDateUpdatesIt() {
        Element<UpdateHearingActualTracking> existingTracking = element(
            UpdateHearingActualTracking.builder().hearingId("123").lastFiredDate(null).build());

        caseData = caseData.toBuilder()
            .id(123L)
            .updateHearingActualTracking(List.of(existingTracking))
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();
        caseDetails = caseDetails.toBuilder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();

        SearchResult searchResult1 = SearchResult.builder().total(1).cases(List.of(caseDetails)).build();
        SearchResultResponse response = SearchResultResponse.builder().total(1).cases(List.of(caseDetails)).build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult1);
        when(objectMapper.convertValue(searchResult1, SearchResultResponse.class)).thenReturn(response);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        updateHearingActualsService.updateHearingActuals();

        assertEquals(LocalDate.now(), existingTracking.getValue().getLastFiredDate());
        verify(allTabService).getStartUpdateForSpecificEvent("123", CaseEvent.ENABLE_UPDATE_HEARING_ACTUAL_TASK.getValue());
        verify(allTabService, never()).getStartUpdateForSpecificEvent("123", CaseEvent.ENABLE_REQUEST_SOLICITOR_ORDER_TASK.getValue());
    }

    @Test
    public void testUpdateHearingActualWhenExistingTrackingHasLastFiredDateSkipsTask() {
        Element<UpdateHearingActualTracking> existingTracking = element(
            UpdateHearingActualTracking.builder().hearingId("123").lastFiredDate(LocalDate.of(2025, 1, 1)).build());

        caseData = caseData.toBuilder()
            .id(123L)
            .updateHearingActualTracking(List.of(existingTracking))
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();
        caseDetails = caseDetails.toBuilder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();

        SearchResult searchResult1 = SearchResult.builder().total(1).cases(List.of(caseDetails)).build();
        SearchResultResponse response = SearchResultResponse.builder().total(1).cases(List.of(caseDetails)).build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult1);
        when(objectMapper.convertValue(searchResult1, SearchResultResponse.class)).thenReturn(response);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        updateHearingActualsService.updateHearingActuals();

        verify(allTabService, never()).getStartUpdateForSpecificEvent("123", CaseEvent.ENABLE_UPDATE_HEARING_ACTUAL_TASK.getValue());
        verify(allTabService, never()).getStartUpdateForSpecificEvent("123", CaseEvent.ENABLE_REQUEST_SOLICITOR_ORDER_TASK.getValue());
    }

    @Test
    public void testUpdateHearingActualWhenCaseDetailsNotFoundForHearingCaseId() {
        Map<String, List<String>> caseIdHearigIdMap = new HashMap<>();
        caseIdHearigIdMap.put("999", Arrays.asList("123"));
        when(hearingApiClient.getListedHearingsForAllCaseIdsOnCurrentDate(any(), any(), anyList()))
            .thenReturn(caseIdHearigIdMap);

        updateHearingActualsService.updateHearingActuals();

        verifyNoInteractions(allTabService);
    }

    @Test
    public void testUpdateHearingActualTaskForDraftOrderCreatedForHearingId() {
        caseData = caseData.toBuilder()
            .id(123L)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .draftOrderCollection(List.of(
                element(DraftOrder.builder()
                            .manageOrderHearingDetails(List.of(
                                element(HearingData.builder()
                                            .confirmedHearingDates(
                                                DynamicList.builder()
                                                    .value(DynamicListElement.builder().code("123").build())
                                                    .listItems(List.of(DynamicListElement.defaultListItem("test")))
                                                    .build()
                                            )
                                            .build()
                                ))
                            )
                            .build())
            ))
            .build();
        caseDetails = caseDetails.toBuilder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString())).thenReturn(searchResult);

        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class)).thenReturn(response);
        updateHearingActualsService.updateHearingActuals();

        verify(allTabService, times(1)).getStartUpdateForSpecificEvent(Mockito.anyString(), Mockito.anyString());
    }
}
