package uk.gov.hmcts.reform.prl.services.requestorder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestOrderHearingTracking;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.workingdays.WorkingDayIndicator;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.ENABLE_REQUEST_SOLICITOR_ORDER_TASK;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RequestOrderTaskServiceTest {

    private static final String CASE_ID = "123";
    private static final String HEARING_ID = "1";
    private static final LocalDate TODAY = LocalDate.of(2026, 4, 14);

    @Mock ObjectMapper objectMapper;
    @Mock SystemUserService systemUserService;
    @Mock AuthTokenGenerator authTokenGenerator;
    @Mock CoreCaseDataApi coreCaseDataApi;
    @Mock AllTabServiceImpl allTabService;
    @Mock HearingService hearingService;
    @Mock WorkingDayIndicator workingDayIndicator;

    RequestOrderTaskService service;

    @BeforeEach
    void setUp() {
        when(systemUserService.getSysUserToken()).thenReturn("sysToken");
        when(authTokenGenerator.generate()).thenReturn("s2sToken");
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString()))
            .thenReturn(new StartAllTabsUpdateDataContent(
                "s2sToken",
                EventRequestData.builder().build(),
                StartEventResponse.builder().build(),
                null, null, null));

        HearingChasePolicy chasePolicy = new HearingChasePolicy(workingDayIndicator);
        ReflectionTestUtils.setField(chasePolicy, "c100CadenceWorkingDays", 3);
        ReflectionTestUtils.setField(chasePolicy, "fl401CadenceWorkingDays", 1);
        ReflectionTestUtils.setField(chasePolicy, "hearingStatusesToFilter",
            List.of("COMPLETED", "AWAITING_ACTUALS"));

        service = new RequestOrderTaskService(
            systemUserService, authTokenGenerator, coreCaseDataApi,
            hearingService, allTabService, chasePolicy, objectMapper);
        ReflectionTestUtils.setField(service, "concurrentRequest", 5);
    }

    @ParameterizedTest
    @CsvSource({
        "FL401, 1",
        "C100, 3"
    })
    void firesAfterHearingEnd(String caseType, int delay) {
        CaseData caseData = baseCaseBuilder(caseType).build();
        stubSearchReturning(caseData);
        stubHearings(completedHearingEndingDaysAgo(1));
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(delay);

        service.processRequestOrderTasks();

        verify(allTabService, times(1)).getStartUpdateForSpecificEvent(
            CASE_ID, ENABLE_REQUEST_SOLICITOR_ORDER_TASK.getValue());
    }

    @Test
    void firesSearch3Times() {
        CaseData caseData = baseCaseBuilder("C100").build();
        stub3SearchReturning(caseData);
        stubHearings(completedHearingEndingDaysAgo(1));
        when(workingDayIndicator.workingDaysBetween(
            any(),
            any()))
            .thenReturn(3);

        service.processRequestOrderTasks();

        verify(allTabService, times(2))
            .getStartUpdateForSpecificEvent(
                CASE_ID,
                ENABLE_REQUEST_SOLICITOR_ORDER_TASK.getValue());
        verify(coreCaseDataApi, times(3))
            .searchCases(
                anyString(),
                anyString(),
                eq(CASE_TYPE),
                any());
    }

    @Test
    void doesNotFireForC100WhenOnlyTwoWorkingDaysSinceHearing() {
        CaseData caseData = baseCaseBuilder("C100").build();
        stubSearchReturning(caseData);
        stubHearings(completedHearingEndingDaysAgo(2));
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(2);

        service.processRequestOrderTasks();

        verify(allTabService, never()).getStartUpdateForSpecificEvent(anyString(), anyString());
    }

    @Test
    void doesNotFireWhenHearingIsMappedToDraftOrder() {
        CaseData caseData = baseCaseBuilder("FL401")
            .draftOrderCollection(List.of(
                Element.<DraftOrder>builder().value(
                    DraftOrder.builder()
                        .manageOrderHearingDetails(List.of(
                            Element.<HearingData>builder().value(
                                HearingData.builder()
                                    .confirmedHearingDates(DynamicList.builder()
                                        .value(DynamicListElement.builder().code(HEARING_ID).build())
                                        .build())
                                    .build()
                            ).build()))
                        .build()
                ).build()))
            .build();
        stubSearchReturning(caseData);
        stubHearings(completedHearingEndingDaysAgo(5));
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(5);

        service.processRequestOrderTasks();

        verify(allTabService, never()).getStartUpdateForSpecificEvent(anyString(), anyString());
    }

    @Test
    void skipsHearingsNotInFilterStatus() {
        CaseData caseData = baseCaseBuilder("FL401").build();
        stubSearchReturning(caseData);
        stubHearings(hearing("LISTED", HEARING_ID, TODAY.minusDays(5)));
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(5);

        service.processRequestOrderTasks();

        verify(allTabService, never()).getStartUpdateForSpecificEvent(anyString(), anyString());
    }

    @Test
    void skipsCasesWithNoHearingsFromHmc() {
        CaseData caseData = baseCaseBuilder("FL401").build();
        stubSearchReturning(caseData);
        when(hearingService.getHearings(anyString(), anyString()))
            .thenReturn(Hearings.hearingsWith().caseRef(CASE_ID).caseHearings(List.of()).build());

        service.processRequestOrderTasks();

        verify(allTabService, never()).getStartUpdateForSpecificEvent(anyString(), anyString());
    }

    @Test
    void skipsHearingWhenItHasLastFiredDateSet() {
        CaseData caseData = baseCaseBuilder("FL401")
            .requestOrderTaskTrackingByHearing(List.of(
                Element.<RequestOrderHearingTracking>builder()
                    .id(UUID.randomUUID())
                    .value(RequestOrderHearingTracking.builder()
                        .hearingId(HEARING_ID)
                        .lastFiredDate(LocalDate.now().minusDays(1))
                        .build())
                    .build()))
            .build();
        stubSearchReturning(caseData);
        stubHearings(completedHearingEndingDaysAgo(5));

        service.processRequestOrderTasks();

        verify(allTabService, never()).getStartUpdateForSpecificEvent(anyString(), anyString());
    }

    @Test
    void reTriggerAnchorsFromLastCompletedDateIgnoringHearingEndDate() {
        LocalDate lastCompleted = LocalDate.now().minusDays(1);
        CaseData caseData = baseCaseBuilder("C100")
            .requestOrderTaskTrackingByHearing(List.of(
                Element.<RequestOrderHearingTracking>builder()
                    .id(UUID.randomUUID())
                    .value(RequestOrderHearingTracking.builder()
                        .hearingId(HEARING_ID)
                        .lastCompletedDate(lastCompleted)
                        .build())
                    .build()))
            .build();
        stubSearchReturning(caseData);
        stubHearings(completedHearingEndingDaysAgo(14));
        when(workingDayIndicator.workingDaysBetween(eq(lastCompleted), any())).thenReturn(1);

        service.processRequestOrderTasks();

        verify(allTabService, never()).getStartUpdateForSpecificEvent(anyString(), anyString());
    }

    @Test
    void firedEventCarriesCurrentHearingIdAndUpdatedCollectionWithTodayAsLastFiredDate() {
        CaseData caseData = baseCaseBuilder("FL401").build();
        stubSearchReturning(caseData);
        stubHearings(completedHearingEndingDaysAgo(1));
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(1);

        service.processRequestOrderTasks();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(allTabService).submitAllTabsUpdate(anyString(), anyString(), any(), any(), captor.capture());
        Map<String, Object> payload = captor.getValue();
        assertThat(payload.get("currentHearingId")).isEqualTo(HEARING_ID);
        @SuppressWarnings("unchecked")
        List<Element<RequestOrderHearingTracking>> collection =
            (List<Element<RequestOrderHearingTracking>>) payload.get("requestOrderTaskTrackingByHearing");
        assertThat(collection).hasSize(1);
        assertThat(collection.get(0).getValue().getHearingId()).isEqualTo(HEARING_ID);
        assertThat(collection.get(0).getValue().getLastFiredDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void firesOncePerQualifyingHearingEachWithItsOwnCurrentHearingId() {
        CaseData caseData = baseCaseBuilder("FL401").build();
        stubSearchReturning(caseData);
        when(hearingService.getHearings(anyString(), anyString()))
            .thenReturn(Hearings.hearingsWith()
                .caseRef(CASE_ID)
                .caseHearings(List.of(
                    hearing("COMPLETED", "10", TODAY.minusDays(2)),
                    hearing("COMPLETED", "20", TODAY.minusDays(2)),
                    hearing("COMPLETED", "30", TODAY.minusDays(2))))
                .build());
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(2);
        when(allTabService
                 .getStartUpdateForSpecificEvent(
                     CASE_ID,
                     CaseEvent.ENABLE_REQUEST_SOLICITOR_ORDER_TASK.getValue()))
            .thenReturn(new StartAllTabsUpdateDataContent(
                "s2sToken",
                EventRequestData.builder().build(),
                StartEventResponse.builder().build(),
                null,
                caseData,
                null));

        service.processRequestOrderTasks();

        verify(allTabService, times(3)).getStartUpdateForSpecificEvent(
            eq(CASE_ID), eq(ENABLE_REQUEST_SOLICITOR_ORDER_TASK.getValue()));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(allTabService, times(3))
            .submitAllTabsUpdate(anyString(), anyString(), any(), any(), captor.capture());
        assertThat(captor.getAllValues())
            .extracting(m -> (String) m.get("currentHearingId"))
            .containsExactly("10", "20", "30");
    }

    @Test
    void inMixedCaseFiresOnlyForHearingsNotAlreadyInFlight() {
        CaseData caseData = baseCaseBuilder("FL401")
            .requestOrderTaskTrackingByHearing(List.of(
                Element.<RequestOrderHearingTracking>builder()
                    .id(UUID.randomUUID())
                    .value(RequestOrderHearingTracking.builder()
                        .hearingId("10")
                        .lastFiredDate(LocalDate.now().minusDays(1))
                        .build())
                    .build()))
            .build();
        stubSearchReturning(caseData);
        when(hearingService.getHearings(anyString(), anyString()))
            .thenReturn(Hearings.hearingsWith()
                .caseRef(CASE_ID)
                .caseHearings(List.of(
                    hearing("COMPLETED", "10", TODAY.minusDays(2)),
                    hearing("COMPLETED", "20", TODAY.minusDays(2)),
                    hearing("COMPLETED", "30", TODAY.minusDays(2))))
                .build());
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(0).thenReturn(2).thenReturn(2);

        when(allTabService
                 .getStartUpdateForSpecificEvent(
                     CASE_ID,
                     CaseEvent.ENABLE_REQUEST_SOLICITOR_ORDER_TASK.getValue()))
            .thenReturn(new StartAllTabsUpdateDataContent(
                "s2sToken",
                EventRequestData.builder().build(),
                StartEventResponse.builder().build(),
                null,
                caseData,
                null));


        service.processRequestOrderTasks();

        verify(allTabService, times(2)).getStartUpdateForSpecificEvent(
            eq(CASE_ID), eq(ENABLE_REQUEST_SOLICITOR_ORDER_TASK.getValue()));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(allTabService, times(2))
            .submitAllTabsUpdate(anyString(), anyString(), any(), any(), captor.capture());
        assertThat(captor.getAllValues())
            .extracting(m -> (String) m.get("currentHearingId"))
            .containsExactly("20", "30");
    }

    @Test
    void emptyCaseListShortCircuits() {
        stubSearchReturningEmpty();

        service.processRequestOrderTasks();

        verify(allTabService, never()).getStartUpdateForSpecificEvent(anyString(), anyString());
    }

    @Test
    void exceptionFromOneCaseDoesNotPreventProcessingOfTheNextCase() throws JsonProcessingException {
        String secondCaseId = "456";
        CaseData firstCase = baseCaseBuilder("FL401").build();
        CaseData secondCase = baseCaseBuilder("FL401").id(Long.valueOf(secondCaseId)).build();
        stubSearchReturningCases(firstCase, secondCase);
        stubHearings(completedHearingEndingDaysAgo(1));
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(1);
        when(allTabService.getStartUpdateForSpecificEvent(eq(CASE_ID), anyString()))
            .thenThrow(new RuntimeException("simulated failure for first case"));

        service.processRequestOrderTasks();

        verify(allTabService, times(1))
            .submitAllTabsUpdate(anyString(), eq(secondCaseId), any(), any(), any());
    }

    @Test
    void caseIsSkippedWhenHmcThrowsFetchingHearings() {
        CaseData caseData = baseCaseBuilder("FL401").build();
        stubSearchReturning(caseData);
        when(hearingService.getHearings(anyString(), anyString()))
            .thenThrow(new RuntimeException("HMC unavailable"));

        service.processRequestOrderTasks();

        verify(allTabService, never()).getStartUpdateForSpecificEvent(anyString(), anyString());
    }

    @Test
    void noCasesAreProcessedWhenQueryParamCannotBeSerialised() throws JsonProcessingException {
        when(objectMapper.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        service.processRequestOrderTasks();

        verify(coreCaseDataApi, never()).searchCases(anyString(), anyString(), anyString(), anyString());
        verify(allTabService, never()).getStartUpdateForSpecificEvent(anyString(), anyString());
    }

    private CaseData.CaseDataBuilder<?, ?> baseCaseBuilder(String caseType) {
        return CaseData.builder()
            .id(Long.valueOf(CASE_ID))
            .state(State.JUDICIAL_REVIEW)
            .caseTypeOfApplication(caseType);
    }

    private CaseHearing completedHearingEndingDaysAgo(int days) {
        return hearing("COMPLETED", HEARING_ID, TODAY.minusDays(days));
    }

    private CaseHearing hearing(String status, String hearingId, LocalDate endDate) {
        return CaseHearing.caseHearingWith()
            .hearingID(Long.valueOf(hearingId))
            .hmcStatus(status)
            .hearingDaySchedule(List.of(HearingDaySchedule.hearingDayScheduleWith()
                .hearingStartDateTime(endDate.atTime(9, 0))
                .hearingEndDateTime(endDate.atTime(16, 0))
                .build()))
            .build();
    }

    private void stubHearings(CaseHearing... caseHearings) {
        when(hearingService.getHearings(anyString(), anyString()))
            .thenReturn(Hearings.hearingsWith()
                .caseRef(CASE_ID)
                .caseHearings(List.of(caseHearings))
                .build());
    }

    private void stubSearchReturning(CaseData caseData) {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(caseData.toMap(objectMapper))
            .build();
        SearchResult firstSearchResult = SearchResult.builder().total(2).cases(List.of(caseDetails)).build();
        SearchResult secondSearchResult = SearchResult.builder()
            .total(2)
            .build();
        when(coreCaseDataApi.searchCases(anyString(), anyString(), eq(CASE_TYPE), any()))
            .thenReturn(firstSearchResult)
            .thenReturn(secondSearchResult);
        when(objectMapper.convertValue(firstSearchResult, SearchResult.class))
            .thenReturn(firstSearchResult);
        when(objectMapper.convertValue(secondSearchResult, SearchResult.class))
            .thenReturn(secondSearchResult);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
    }

    private void stub3SearchReturning(CaseData caseData) {
        CaseDetails caseDetails = CaseDetails.builder()
            .id(caseData.getId())
            .data(caseData.toMap(objectMapper))
            .build();
        SearchResult firstSearchResult = SearchResult.builder().total(3).cases(List.of(caseDetails)).build();
        SearchResult secondSearchResult = SearchResult.builder().total(3).cases(List.of(caseDetails)).build();
        SearchResult thirdSearchResult = SearchResult.builder()
            .total(3)
            .build();
        when(coreCaseDataApi.searchCases(anyString(), anyString(), eq(CASE_TYPE), any()))
            .thenReturn(firstSearchResult)
            .thenReturn(secondSearchResult)
            .thenReturn(thirdSearchResult);
        when(objectMapper.convertValue(firstSearchResult, SearchResult.class))
            .thenReturn(firstSearchResult);
        when(objectMapper.convertValue(secondSearchResult, SearchResult.class))
            .thenReturn(secondSearchResult);
        when(objectMapper.convertValue(thirdSearchResult, SearchResult.class))
            .thenReturn(thirdSearchResult);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
    }

    private void stubSearchReturningCases(CaseData... caseDataValues) {
        List<CaseDetails> details = java.util.Arrays.stream(caseDataValues)
            .map(c -> CaseDetails.builder().id(c.getId()).data(c.toMap(objectMapper)).build())
            .toList();
        SearchResult firstSearchResult = SearchResult.builder().total(details.size()).cases(details).build();
        SearchResult secondSearchResult = SearchResult.builder()
            .total(details.size())
            .cases(Collections.EMPTY_LIST)
            .build();
        when(coreCaseDataApi.searchCases(anyString(), anyString(), eq(CASE_TYPE), any()))
            .thenReturn(firstSearchResult)
            .thenReturn(secondSearchResult);
        when(objectMapper.convertValue(firstSearchResult, SearchResult.class))
            .thenReturn(firstSearchResult);
        when(objectMapper.convertValue(secondSearchResult, SearchResult.class))
            .thenReturn(secondSearchResult);
        for (int i = 0; i < details.size(); i++) {
            when(objectMapper.convertValue(details.get(i).getData(), CaseData.class))
                .thenReturn(caseDataValues[i]);
        }
    }

    private void stubSearchReturningEmpty() {
        SearchResult searchResult = SearchResult.builder().total(0).cases(List.of()).build();
        when(coreCaseDataApi.searchCases(anyString(), anyString(), eq(CASE_TYPE), any()))
            .thenReturn(searchResult);
        when(objectMapper.convertValue(searchResult, SearchResult.class))
            .thenReturn(SearchResult.builder().total(0).cases(List.of()).build());
    }
}
