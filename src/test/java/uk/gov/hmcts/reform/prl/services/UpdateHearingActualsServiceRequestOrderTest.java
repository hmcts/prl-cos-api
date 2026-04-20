package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestOrderHearingTracking;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.workingdays.WorkingDayIndicator;

import java.time.LocalDate;
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

/**
 * Tests for the Request Order task firing logic (FPVTL-2408/2409).
 * Per-hearing tracking: the cron reads hearings from HMC, applies per-hearing cadence,
 * and fires ENABLE_REQUEST_SOLICITOR_ORDER_TASK once per case with the updated collection.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateHearingActualsServiceRequestOrderTest {

    private static final String CASE_ID = "123";
    private static final String HEARING_ID = "1";
    private static final LocalDate TODAY = LocalDate.of(2026, 4, 14);

    @Mock ObjectMapper objectMapper;
    @Mock SystemUserService systemUserService;
    @Mock AuthTokenGenerator authTokenGenerator;
    @Mock CoreCaseDataApi coreCaseDataApi;
    @Mock AllTabServiceImpl allTabService;
    @Mock HearingApiClient hearingApiClient;
    @Mock WorkingDayIndicator workingDayIndicator;

    @InjectMocks
    UpdateHearingActualsService service;

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
        ReflectionTestUtils.setField(service, "c100CadenceWorkingDays", 3);
        ReflectionTestUtils.setField(service, "fl401CadenceWorkingDays", 1);
        ReflectionTestUtils.setField(service, "hearingStatusesToFilter",
            List.of("COMPLETED", "AWAITING_ACTUALS"));
    }

    @Test
    void firesForFl401OneWorkingDayAfterHearingEnd() {
        CaseData caseData = baseCaseBuilder("FL401").build();
        stubSearchReturning(caseData);
        stubHearings(completedHearingEndingDaysAgo(1));
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(1);

        service.processRequestOrderTasks();

        verify(allTabService, times(1)).getStartUpdateForSpecificEvent(
            eq(CASE_ID), eq(CaseEvent.ENABLE_REQUEST_SOLICITOR_ORDER_TASK.getValue()));
    }

    @Test
    void firesForC100ThreeWorkingDaysAfterHearingEnd() {
        CaseData caseData = baseCaseBuilder("C100").build();
        stubSearchReturning(caseData);
        stubHearings(completedHearingEndingDaysAgo(3));
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(3);

        service.processRequestOrderTasks();

        verify(allTabService, times(1)).getStartUpdateForSpecificEvent(
            eq(CASE_ID), eq(CaseEvent.ENABLE_REQUEST_SOLICITOR_ORDER_TASK.getValue()));
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
        when(hearingApiClient.getHearingDetails(anyString(), anyString(), anyString()))
            .thenReturn(Hearings.hearingsWith().caseRef(CASE_ID).caseHearings(List.of()).build());

        service.processRequestOrderTasks();

        verify(allTabService, never()).getStartUpdateForSpecificEvent(anyString(), anyString());
    }

    @Test
    void skipsCaseWhenAnyHearingHasLastFiredDateSet() {
        // A previous cron run already fired; the task is still open in WA. Don't re-fire.
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

        service.processRequestOrderTasks();

        verify(allTabService, never()).getStartUpdateForSpecificEvent(anyString(), anyString());
        // HMC should NOT be called — the guard short-circuits before the HMC request.
        verify(hearingApiClient, never()).getHearingDetails(anyString(), anyString(), anyString());
    }

    @Test
    void reTriggerAnchorsFromLastCompletedDateIgnoringHearingEndDate() {
        // Hearing ended 14 days ago but completion was 1 day ago. C100 cadence = 3 → no fire.
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
    void firedEventCarriesUpdatedCollectionWithTodayAsLastFiredDate() {
        CaseData caseData = baseCaseBuilder("FL401").build();
        stubSearchReturning(caseData);
        stubHearings(completedHearingEndingDaysAgo(1));
        when(workingDayIndicator.workingDaysBetween(any(), any())).thenReturn(1);

        service.processRequestOrderTasks();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(allTabService).submitAllTabsUpdate(anyString(), anyString(), any(), any(), captor.capture());
        @SuppressWarnings("unchecked")
        List<Element<RequestOrderHearingTracking>> collection =
            (List<Element<RequestOrderHearingTracking>>) captor.getValue().get("requestOrderTaskTrackingByHearing");
        assertThat(collection).hasSize(1);
        assertThat(collection.get(0).getValue().getHearingId()).isEqualTo(HEARING_ID);
        assertThat(collection.get(0).getValue().getLastFiredDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void emptyCaseListShortCircuits() {
        stubSearchReturningEmpty();

        service.processRequestOrderTasks();

        verify(allTabService, never()).getStartUpdateForSpecificEvent(anyString(), anyString());
    }

    // -------- helpers --------

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
        when(hearingApiClient.getHearingDetails(anyString(), anyString(), anyString()))
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
        SearchResult searchResult = SearchResult.builder().total(1).cases(List.of(caseDetails)).build();
        when(coreCaseDataApi.searchCases(anyString(), anyString(), eq(CASE_TYPE_CONSTANT), any()))
            .thenReturn(searchResult);
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class))
            .thenReturn(SearchResultResponse.builder().total(1).cases(List.of(caseDetails)).build());
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
    }

    private void stubSearchReturningEmpty() {
        SearchResult searchResult = SearchResult.builder().total(0).cases(List.of()).build();
        when(coreCaseDataApi.searchCases(anyString(), anyString(), eq(CASE_TYPE_CONSTANT), any()))
            .thenReturn(searchResult);
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class))
            .thenReturn(SearchResultResponse.builder().total(0).cases(List.of()).build());
    }

    private static final String CASE_TYPE_CONSTANT = uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
}
