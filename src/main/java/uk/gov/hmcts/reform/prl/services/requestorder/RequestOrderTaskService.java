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
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Bool;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Match;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Must;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Query;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Should;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.StateFilter;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
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
    private static final String C100 = "C100";
    private static final String FL401 = "FL401";
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
        List<CaseDetails> candidates = retrieveCandidateCases(buildQueryParam());
        if (!isNotEmpty(candidates)) {
            return;
        }
        candidates.forEach(caseDetails -> {
            try {
                processCase(caseDetails);
            } catch (Exception e) {
                log.error("Error while processing Request Order task for case {}", caseDetails.getId(), e);
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
        for (CaseHearing hearing : hearings.getCaseHearings()) {
            evaluateHearing(caseId, caseData, hearing);
        }
    }

    private void evaluateHearing(String caseId,
                                 CaseData caseData,
                                 CaseHearing hearing) {
        String hearingId = HearingChasePolicy.hearingIdOf(hearing);
        LocalDate today = LocalDate.now(UK_ZONE);
        HearingTrackingLedger ledger = HearingTrackingLedger.from(caseData);
        ChaseDecision decision = chasePolicy.decide(hearing, caseData, ledger, today);
        log.info("Request Order: caseId={} hearingId={} {}", caseId, hearingId, decision.description());
        if (!decision.shouldFire()) {
            return;
        }
        ledger.recordFired(hearingId, today);
        fireRequestOrderEvent(caseId, hearingId, ledger);
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

    /**
     * Matches C100/FL401 cases in one of the three hearing states.
     */
    private QueryParam buildQueryParam() {
        List<Should> caseTypes = List.of(
            Should.builder().match(Match.builder().caseTypeOfApplication(C100).build()).build(),
            Should.builder().match(Match.builder().caseTypeOfApplication(FL401).build()).build());

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

        return QueryParam.builder()
            .query(Query.builder().bool(filter).build())
            .size(ES_PAGE_SIZE)
            .dataToReturn(List.of(
                "data.caseTypeOfApplication",
                "data.draftOrderCollection",
                "data.orderCollection",
                "data.requestOrderTaskTrackingByHearing"))
            .build();
    }

    private List<CaseDetails> retrieveCandidateCases(QueryParam queryParam) {
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String searchString = objectMapper.writeValueAsString(queryParam);
            SearchResult searchResult = coreCaseDataApi.searchCases(
                systemUserService.getSysUserToken(),
                authTokenGenerator.generate(),
                CASE_TYPE, searchString);
            SearchResultResponse response = objectMapper.convertValue(searchResult, SearchResultResponse.class);
            if (response == null) {
                return Collections.emptyList();
            }
            log.info("Request Order: total candidate cases retrieved {}", response.getTotal());
            return response.getCases();
        } catch (JsonProcessingException e) {
            log.error("Request Order: exception parsing query param", e);
            return Collections.emptyList();
        }
    }
}
