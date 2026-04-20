package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestOrderHearingTracking;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Bool;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Match;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Must;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Query;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Should;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.StateFilter;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.workingdays.WorkingDayIndicator;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateHearingActualsService {

    private static final ZoneId UK_ZONE = ZoneId.of("Europe/London");
    private static final String C100 = "C100";

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final HearingApiClient hearingApiClient;
    private final AllTabServiceImpl allTabService;
    private final WorkingDayIndicator workingDayIndicator;

    private final ObjectMapper objectMapper;

    @Value("${ccd.elastic-search-api.result-size}")
    private String ccdElasticSearchApiResultSize;

    //FPVTL-2408/2409: cadence overridable per environment (e.g. set to 0 in preview for fast E2E testing).
    @Value("${request-order-task.cadence-working-days.c100:3}")
    private int c100CadenceWorkingDays;

    @Value("${request-order-task.cadence-working-days.fl401:1}")
    private int fl401CadenceWorkingDays;

    @Value("#{'${hearing_component.hearingStatusesToFilter}'.split(',')}")
    private List<String> hearingStatusesToFilter;


    public void updateHearingActuals() {

        //Fetch all cases in Hearing state with a hearing today
        log.info("Running Hearing actual task cron job...");
        List<CaseDetails> caseDetailsList = retrieveCasesWithHearingToday();
        try {
            if (isNotEmpty(caseDetailsList)) {
                log.info("Cases exist with current hearing");
                Map<String, List<String>> hearingsForToday = fetchAndFilterHearingsForTodaysDate(
                    getListOfCaseidsForHearings(caseDetailsList));
                hearingsForToday.keySet().forEach(caseId -> triggerSystemEventForWorkAllocationTask(
                    caseId, CaseEvent.ENABLE_UPDATE_HEARING_ACTUAL_TASK.getValue(), new HashMap<>()));
            }
        } catch (Exception e) {
            log.error("Error while updating hearing actuals", e);
        }
    }

    /**
     * For each candidate case, fires ENABLE_REQUEST_SOLICITOR_ORDER_TASK when the working-day
     * cadence since the most recent anchor (last completion, last fire, or hearing date) has
     * elapsed and no draft/saved order is yet mapped to the hearing. Cadence is 1 working day
     * for FL401 (FPVTL-2408) and 3 working days for C100 (FPVTL-2409).
     */
    public void processRequestOrderTasks() {
        log.info("Running Request Order task cron job...");
        List<CaseDetails> caseDetailsList = retrieveCandidateCasesForRequestOrderTask();
        if (!isNotEmpty(caseDetailsList)) {
            return;
        }
        LocalDate today = LocalDate.now(UK_ZONE);
        caseDetailsList.forEach(caseDetails -> {
            try {
                processRequestOrderForCase(caseDetails, today);
            } catch (Exception e) {
                log.error("Error while processing Request Order task for case {}", caseDetails.getId(), e);
            }
        });
    }

    private void processRequestOrderForCase(CaseDetails caseDetails, LocalDate today) {
        String caseId = String.valueOf(caseDetails.getId());
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        boolean awaitingCompletion = nullSafeCollection(caseData.getRequestOrderTaskTrackingByHearing()).stream()
            .anyMatch(e -> e.getValue().getLastFiredDate() != null);
        if (awaitingCompletion) {
            log.info("Request Order: caseId={} skipping - previous fire awaiting completion", caseId);
            return;
        }

        Hearings hearings = fetchHearingsForCase(caseId);
        if (hearings == null || hearings.getCaseHearings() == null || hearings.getCaseHearings().isEmpty()) {
            log.info("Request Order: skipping caseId={} - no hearings from HMC", caseId);
            return;
        }

        int cadence = C100.equals(caseData.getCaseTypeOfApplication())
            ? c100CadenceWorkingDays
            : fl401CadenceWorkingDays;

        List<String> filterStatuses = hearingStatusesToFilter.stream().map(String::trim).toList();
        log.info("Request Order: caseId={} has {} hearing(s) from HMC, filter={}, cadence={}",
                 caseId, hearings.getCaseHearings().size(), filterStatuses, cadence);

        Map<String, Element<RequestOrderHearingTracking>> trackingByHearingId = new LinkedHashMap<>();
        nullSafeCollection(caseData.getRequestOrderTaskTrackingByHearing())
            .forEach(e -> trackingByHearingId.put(e.getValue().getHearingId(), e));

        boolean fireEvent = false;
        for (CaseHearing hearing : hearings.getCaseHearings()) {
            String hearingId = hearing.getHearingID() == null ? null : String.valueOf(hearing.getHearingID());
            if (hearingId == null || !filterStatuses.contains(hearing.getHmcStatus())) {
                log.info("Request Order: caseId={} hearingId={} skipped - status={} not in filter",
                         caseId, hearingId, hearing.getHmcStatus());
                continue;
            }
            LocalDate hearingEndDate = computeHearingEndDate(hearing);
            if (hearingEndDate == null || hearingEndDate.isAfter(today)) {
                log.info("Request Order: caseId={} hearingId={} skipped - hearingEndDate={} not in past",
                         caseId, hearingId, hearingEndDate);
                continue;
            }
            if (checkIfHearingIdIsMappedInOrders(caseData, List.of(hearingId))) {
                log.info("Request Order: caseId={} hearingId={} skipped - linked order exists (cycle complete)",
                         caseId, hearingId);
                continue;
            }

            Element<RequestOrderHearingTracking> entry = trackingByHearingId.get(hearingId);
            LocalDate lastCompleted = entry == null ? null : entry.getValue().getLastCompletedDate();
            LocalDate anchor = lastCompleted != null ? lastCompleted : hearingEndDate;

            int workingDaysSinceAnchor = workingDayIndicator.workingDaysBetween(anchor, today);
            if (workingDaysSinceAnchor < cadence) {
                log.info("Request Order: caseId={} hearingId={} skipped - {} working day(s) since anchor {} (need {})",
                         caseId, hearingId, workingDaysSinceAnchor, anchor, cadence);
                continue;
            }

            if (entry != null) {
                entry.getValue().setLastFiredDate(today);
            } else {
                RequestOrderHearingTracking value = RequestOrderHearingTracking.builder()
                    .hearingId(hearingId)
                    .lastFiredDate(today)
                    .build();
                trackingByHearingId.put(hearingId,
                    Element.<RequestOrderHearingTracking>builder().id(UUID.randomUUID()).value(value).build());
            }
            fireEvent = true;
            log.info("Request Order: caseId={} hearingId={} cadence met - firing", caseId, hearingId);
        }

        if (fireEvent) {
            Map<String, Object> caseDataUpdated = new HashMap<>();
            caseDataUpdated.put("requestOrderTaskTrackingByHearing", new ArrayList<>(trackingByHearingId.values()));
            triggerSystemEventForWorkAllocationTask(
                caseId, CaseEvent.ENABLE_REQUEST_SOLICITOR_ORDER_TASK.getValue(), caseDataUpdated);
        }
    }

    private Hearings fetchHearingsForCase(String caseId) {
        try {
            log.info("Request Order: calling HMC getHearingDetails for caseId={}", caseId);
            Hearings hearings = hearingApiClient.getHearingDetails(
                systemUserService.getSysUserToken(), authTokenGenerator.generate(), caseId);
            if (hearings == null) {
                log.info("Request Order: HMC returned null for caseId={}", caseId);
            } else {
                log.info("Request Order: HMC response for caseId={}: hmctsServiceCode={}, caseRef={}, caseHearings={}",
                         caseId, hearings.getHmctsServiceCode(), hearings.getCaseRef(),
                         hearings.getCaseHearings() == null ? "null" : hearings.getCaseHearings().size() + " entries");
                if (hearings.getCaseHearings() != null) {
                    hearings.getCaseHearings().forEach(h ->
                        log.info("Request Order: HMC hearing for caseId={}: hearingID={}, hmcStatus={}, scheduleDays={}",
                                 caseId, h.getHearingID(), h.getHmcStatus(),
                                 h.getHearingDaySchedule() == null ? "null" : h.getHearingDaySchedule().size()));
                }
            }
            return hearings;
        } catch (Exception e) {
            log.error("Request Order: failed to fetch hearings for caseId={}: {}", caseId, e.getMessage(), e);
            return null;
        }
    }

    private LocalDate computeHearingEndDate(CaseHearing hearing) {
        return nullSafeCollection(hearing.getHearingDaySchedule()).stream()
            .map(HearingDaySchedule::getHearingEndDateTime)
            .filter(Objects::nonNull)
            .max(Comparator.naturalOrder())
            .map(LocalDateTime::toLocalDate)
            .orElse(null);
    }

    private Map<String, List<String>> fetchAndFilterHearingsForTodaysDate(List<String> listOfCaseidsForHearings) {
        return hearingApiClient.getListedHearingsForAllCaseIdsOnCurrentDate(
            systemUserService.getSysUserToken(),
            authTokenGenerator.generate(),
            listOfCaseidsForHearings
        );
    }

    private void triggerSystemEventForWorkAllocationTask(String caseId, String caseEvent, Map<String, Object> caseDataUpdated) {
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificEvent(caseId, caseEvent);
        allTabService.submitAllTabsUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            caseDataUpdated
        );
    }

    private boolean checkIfHearingIdIsMappedInOrders(CaseData caseData, List<String> hearingId) {
        if (!checkIfHearingIdIsMappedinDraftOrder(caseData, hearingId)) {
            return checkIfHearingIdIsMappedinSavedServedOrder(caseData, hearingId);
        }
        return true;
    }

    private boolean checkIfHearingIdIsMappedinDraftOrder(CaseData caseData, List<String> hearingId) {
        return nullSafeCollection(caseData.getDraftOrderCollection())
            .stream()
            .map(Element::getValue)
            .anyMatch(draftOrderElement -> nullSafeCollection(draftOrderElement.getManageOrderHearingDetails())
                .stream()
                .map(Element::getValue)
                .anyMatch(hearingData -> hearingData.getConfirmedHearingDates() != null
                    && hearingData.getConfirmedHearingDates().getValue() != null
                    && hearingId.contains(hearingData.getConfirmedHearingDates().getValue().getCode())));
    }

    private boolean checkIfHearingIdIsMappedinSavedServedOrder(CaseData caseData, List<String> hearingId) {
        return nullSafeCollection(caseData.getOrderCollection())
            .stream()
            .map(Element::getValue)
            .anyMatch(orderElement -> nullSafeCollection(orderElement.getManageOrderHearingDetails())
                .stream().map(Element::getValue)
                .anyMatch(hearingData -> hearingData.getConfirmedHearingDates() != null
                    && hearingData.getConfirmedHearingDates().getValue() != null
                    && hearingId.contains(hearingData.getConfirmedHearingDates().getValue().getCode())));
    }

    private List<String> getListOfCaseidsForHearings(List<CaseDetails> caseDetailsList) {
        return caseDetailsList.stream().map(CaseDetails::getId).map(String::valueOf).toList();

    }


    public List<CaseDetails> retrieveCasesWithHearingToday() {
        return runCcdSearch(buildTodaysHearingQueryParam());
    }

    public List<CaseDetails> retrieveCandidateCasesForRequestOrderTask() {
        return runCcdSearch(buildRequestOrderQueryParam());
    }

    private List<CaseDetails> runCcdSearch(QueryParam ccdQueryParam) {
        SearchResultResponse response = SearchResultResponse.builder().cases(new ArrayList<>()).build();
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String searchString = objectMapper.writeValueAsString(ccdQueryParam);
            String userToken = systemUserService.getSysUserToken();
            final String s2sToken = authTokenGenerator.generate();
            SearchResult searchResult = coreCaseDataApi.searchCases(userToken, s2sToken, CASE_TYPE, searchString);
            response = objectMapper.convertValue(searchResult, SearchResultResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Exception happened in parsing query param ", e);
        }
        if (null != response) {
            log.info("Total no. of cases retrieved {}", response.getTotal());
            return response.getCases();
        }
        return Collections.emptyList();
    }


    private QueryParam buildTodaysHearingQueryParam() {
        List<Should> shoulds = List.of(
                Should.builder().match(Match.builder().caseTypeOfApplication("C100").build()).build(),
                Should.builder().match(Match.builder().caseTypeOfApplication("FL401").build()).build(),
                Should.builder().match(Match.builder().nextHearingDate(LocalDate.now(UK_ZONE)).build()).build()
        );

        //Hearing state
        StateFilter stateFilter = StateFilter.builder().should(List.of(
            Should.builder().match(Match.builder().state(State.JUDICIAL_REVIEW.getValue()).build()).build(),
            Should.builder().match(Match.builder().state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue()).build()).build(),
            Should.builder().match(Match.builder().state(State.DECISION_OUTCOME.getValue()).build()).build()
        )).build();
        Must mustFilter = Must.builder().stateFilter(stateFilter).build();

        Bool finalFilter = Bool.builder().should(shoulds).minimumShouldMatch(2).must(mustFilter).build();

        return QueryParam.builder()
                .query(Query.builder().bool(finalFilter).build())
                .size("100")
            .dataToReturn(fetchFieldsRequiredForHearingActualTask())
                .build();
    }

    /**
     * Matches C100/FL401 cases in one of the three hearing-adjacent states. Intentionally does
     * not filter on nextHearingDate so past hearings are included for re-triggering. Mapped-in-order
     * and cadence checks are done in Java.
     */
    private QueryParam buildRequestOrderQueryParam() {
        List<Should> shoulds = List.of(
                Should.builder().match(Match.builder().caseTypeOfApplication("C100").build()).build(),
                Should.builder().match(Match.builder().caseTypeOfApplication("FL401").build()).build()
        );

        StateFilter stateFilter = StateFilter.builder().should(List.of(
            Should.builder().match(Match.builder().state(State.JUDICIAL_REVIEW.getValue()).build()).build(),
            Should.builder().match(Match.builder().state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue()).build()).build(),
            Should.builder().match(Match.builder().state(State.DECISION_OUTCOME.getValue()).build()).build()
        )).build();
        Must mustFilter = Must.builder().stateFilter(stateFilter).build();

        Bool finalFilter = Bool.builder().should(shoulds).minimumShouldMatch(1).must(mustFilter).build();

        return QueryParam.builder()
                .query(Query.builder().bool(finalFilter).build())
                .size("100")
                .dataToReturn(fetchFieldsRequiredForRequestOrderTask())
                .build();
    }

    private List<String> fetchFieldsRequiredForHearingActualTask() {
        return List.of(
            "data.nextHearingDate",
            "data.draftOrderCollection",
            "data.orderCollection"
        );
    }

    private List<String> fetchFieldsRequiredForRequestOrderTask() {
        return List.of(
            "data.caseTypeOfApplication",
            "data.draftOrderCollection",
            "data.orderCollection",
            "data.requestOrderTaskTrackingByHearing"
        );
    }
}