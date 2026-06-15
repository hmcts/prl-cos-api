package uk.gov.hmcts.reform.prl.services.requestorder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Bool;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Match;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Must;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Query;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Should;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Sort;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.StateFilter;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.services.requestorder.HearingChasePolicy.hearingIdOf;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

/**
 * Fires ENABLE_REQUEST_SOLICITOR_ORDER_TASK once per hearing that needs chasing for a draft
 * order (FPVTL-2408/2409).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RequestOrderTaskService {

    private static final ZoneId UK_ZONE = ZoneId.of("Europe/London");
    private static final String ES_PAGE_SIZE = "100";
    private static final String CURRENT_HEARING_ID = "currentHearingId";
    private static final String TRACKING_FIELD = "requestOrderTaskTrackingByHearing";

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final HearingService hearingService;
    private final AllTabServiceImpl allTabService;
    private final HearingChasePolicy chasePolicy;
    private final ObjectMapper objectMapper;

    public void processRequestOrderTasks() {
        log.info("Running Request Order task cron job...");

        QueryParam.QueryParamBuilder queryParamBuilder = QueryParam.builder()
            .size(ES_PAGE_SIZE)
            .dataToReturn(List.of(
                "reference",
                "data.caseTypeOfApplication",
                "data.draftOrderCollection",
                "data.orderCollection",
                "data.requestOrderTaskTrackingByHearing"
            ));

        Semaphore semaphore = new Semaphore(50);
        // Initial query
        Optional<SearchResult> searchResult = executeQuery(
            buildQuery(
                startAfter -> queryParamBuilder,
                ""
            ));

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            searchResult.ifPresent(result -> {
                log.info("Processing total record count of {}",
                         result.getTotal());
                if (result.getTotal() > 0) {
                        log.info("Processing initial record count of {}",
                                 result.getCases().size());
                        List<CaseDetails> cases = result.getCases();
                        process(executor, semaphore, cases);

                        String searchAfterValue = cases.getLast().getId().toString();
                        log.info("search after value {}", searchAfterValue);
                        boolean keepSearching;
                        do {
                            // Subsequent query
                            Optional<SearchResult> subsequentSearchResult = executeQuery(
                                buildQuery(
                                    startAfter -> queryParamBuilder
                                        .searchAfter(List.of(startAfter)),
                                    searchAfterValue
                                ));

                            keepSearching = subsequentSearchResult
                                .map(SearchResult::getCases)
                                .map(records -> !records.isEmpty())
                                .orElse(false);

                            if (keepSearching) {
                                log.info("Processing subsequent record count of {}",
                                         subsequentSearchResult.map(records -> records.getCases().size()));

                                subsequentSearchResult
                                    .map(SearchResult::getCases)
                                    .ifPresent(subSequentCases ->
                                                   process(executor, semaphore, subSequentCases));

                                searchAfterValue = subsequentSearchResult
                                    .map(SearchResult::getCases)
                                    .map(List::getLast)
                                    .map(CaseDetails::getId)
                                    .map(Object::toString)
                                    .orElse("");

                                log.info("search after value {}", searchAfterValue);
                            }
                        } while (keepSearching);
                    }
                }
            );
        }
    }

    /**
     * Matches C100/FL401 cases in one of the three hearing states.
     */
    private QueryParam buildQuery(Function<String, QueryParam.QueryParamBuilder> queryParamFunction, String searchAfter) {
        List<Should> caseTypes = List.of(
            Should.builder().match(Match.builder().caseTypeOfApplication(C100_CASE_TYPE).build()).build(),
            Should.builder().match(Match.builder().caseTypeOfApplication(FL401_CASE_TYPE).build()).build());

        StateFilter stateFilter = StateFilter.builder().should(List.of(
            Should.builder().match(Match.builder().state(State.JUDICIAL_REVIEW.getValue()).build()).build(),
            Should.builder().match(Match.builder().state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue()).build()).build(),
            Should.builder().match(Match.builder().state(State.DECISION_OUTCOME.getValue()).build()).build()
        )).build();

        Bool filter = Bool.builder()
            .should(caseTypes)
            .minimumShouldMatch(1)
            .must(Must.builder().stateFilter(stateFilter).build())
            .build();

        return queryParamFunction.apply(searchAfter)
            .query(Query.builder().bool(filter).build())
            .sort(List.of(Sort.builder().referenceKeyword("asc").build()))
            .build();
    }

    private void process(ExecutorService executor,
                         Semaphore semaphore,
                         List<CaseDetails> cases) {
        cases.forEach(caseDetails -> {
            try {
                semaphore.acquire();
                executor.submit(() -> {
                    try {
                        processCase(caseDetails);
                    } catch (Exception e) {
                        log.error("Error while processing Request Order task for case {}", caseDetails.getId(), e);
                    } finally {
                        semaphore.release();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // restore interrupt status
                log.error("Interrupted while processing case {}", caseDetails.getId(), e);
            }
        });
    }

    private void processCase(CaseDetails caseDetails) {
        String caseId = String.valueOf(caseDetails.getId());

        Hearings hearings = fetchHearings(caseId);
        if (hearings == null || nullSafeCollection(hearings.getCaseHearings()).isEmpty()) {
            log.info("Request Order: skipping caseId={} - no hearings from HMC", caseId);
            return;
        }

        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        Iterator<CaseHearing> caseHearingIterator = hearings.getCaseHearings().iterator();

        if (caseHearingIterator.hasNext()) {
            evaluateHearing(caseData, caseHearingIterator.next());
            evaluateMultipleHearing(caseHearingIterator, caseId);
        }
    }

    private void evaluateMultipleHearing(Iterator<CaseHearing> caseHearingIterator, String caseId) {
        while (caseHearingIterator.hasNext()) {
            CaseHearing hearing = caseHearingIterator.next();
            StartAllTabsUpdateDataContent start = allTabService.getStartUpdateForSpecificEvent(
                caseId,
                CaseEvent.ENABLE_REQUEST_SOLICITOR_ORDER_TASK.getValue());

            evaluateDecision(start.caseData(), hearing)
                .ifPresent(ledger -> {

                    String hearingId = hearingIdOf(hearing);
                    ledger.recordFired(hearingId, LocalDate.now(UK_ZONE));

                    Map<String, Object> caseDataUpdated = new HashMap<>();
                    caseDataUpdated.put(CURRENT_HEARING_ID, hearingId);
                    caseDataUpdated.put(TRACKING_FIELD, ledger.asCollection());

                    allTabService.submitAllTabsUpdate(
                        start.authorisation(),
                        caseId,
                        start.startEventResponse(),
                        start.eventRequestData(),
                        caseDataUpdated);
                });
        }
    }

    private Optional<HearingTrackingLedger> evaluateDecision(CaseData caseData,
                                                             CaseHearing hearing) {
        String hearingId = hearingIdOf(hearing);
        HearingTrackingLedger ledger = HearingTrackingLedger.from(caseData);
        ChaseDecision decision = chasePolicy.decide(hearing, caseData, ledger, LocalDate.now(UK_ZONE));
        log.info("Request Order: caseId={} hearingId={} {}", caseData.getId(), hearingId, decision.description());
        if (decision.shouldFire()) {
            return Optional.of(ledger);
        }
        return Optional.empty();
    }

    private void evaluateHearing(CaseData caseData,
                                 CaseHearing hearing) {
        evaluateDecision(caseData, hearing)
            .ifPresent(ledger -> {
                String hearingId = hearingIdOf(hearing);
                ledger.recordFired(hearingId, LocalDate.now(UK_ZONE));
                fireRequestOrderEvent(String.valueOf(caseData.getId()), hearingId, ledger);
            });
    }

    private Hearings fetchHearings(String caseId) {
        try {
            Hearings hearings = hearingService.getHearings(
                systemUserService.getSysUserToken(),
                caseId);
            if (hearings == null) {
                log.info("Request Order: HMC returned null for caseId={}", caseId);
            }
            return hearings;
        } catch (Exception e) {
            log.error("Request Order: failed to fetch hearings for caseId={}: {}", caseId, e.getMessage(), e);
            return null;
        }
    }

    private void fireRequestOrderEvent(String caseId, String hearingId, HearingTrackingLedger ledger) {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(CURRENT_HEARING_ID, hearingId);
        caseDataUpdated.put(TRACKING_FIELD, ledger.asCollection());
        StartAllTabsUpdateDataContent start = allTabService.getStartUpdateForSpecificEvent(
            caseId,
            CaseEvent.ENABLE_REQUEST_SOLICITOR_ORDER_TASK.getValue());

        allTabService.submitAllTabsUpdate(
            start.authorisation(),
            caseId,
            start.startEventResponse(),
            start.eventRequestData(),
            caseDataUpdated);
    }

    private Optional<SearchResult> executeQuery(QueryParam queryParam) {
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String searchString = objectMapper.writeValueAsString(queryParam);
            log.info("json query {}",searchString);
            SearchResult searchResult = coreCaseDataApi.searchCases(
                systemUserService.getSysUserToken(),
                authTokenGenerator.generate(),
                CASE_TYPE,
                searchString);
            return Optional.ofNullable(searchResult);
        } catch (JsonProcessingException e) {
            log.error("Request Order: exception parsing query param", e);
        }
        return Optional.empty();
    }
}
