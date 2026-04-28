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
import uk.gov.hmcts.reform.prl.models.dto.ccd.UpdateHearingActualTracking;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Bool;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Match;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Must;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Query;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Should;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.StateFilter;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
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

    private final ObjectMapper objectMapper;

    @Value("${ccd.elastic-search-api.result-size}")
    private String ccdElasticSearchApiResultSize;


    public void updateHearingActuals() {

        //Fetch all cases in Hearing state with a hearing today
        log.info("Running Hearing actual task cron job...");
        List<CaseDetails> caseDetailsList = retrieveCasesWithHearingToday();
        try {
            if (isNotEmpty(caseDetailsList)) {
                log.info("Cases exist with current hearing");
                Map<String, List<String>> hearingsForToday = fetchAndFilterHearingsForTodaysDate(
                    getListOfCaseidsForHearings(caseDetailsList));
                hearingsForToday.forEach(processHearings(caseDetailsList));
            }
        } catch (Exception e) {
            log.error("Error while updating hearing actuals", e);
        }
    }

    private BiConsumer<String, List<String>> processHearings(List<CaseDetails> caseDetailsList) {
        return (caseId, hearingIds) -> {
            CaseDetails caseDetail = caseDetailsList.stream()
                .filter(caseDetails -> String.valueOf(caseDetails.getId()).equals(caseId))
                .findFirst().orElse(null);

            if (nonNull(caseDetail)) {
                CaseData caseData = CaseUtils.getCaseData(caseDetail, objectMapper);

                Map<String, Element<UpdateHearingActualTracking>> trackingByHearingId = new LinkedHashMap<>();
                nullSafeCollection(caseData.getUpdateHearingActualTracking())
                    .forEach(e -> trackingByHearingId.put(e.getValue().getHearingId(), e));
                hearingIds.forEach(hearingId -> processIndividualHearing(caseId, hearingId, trackingByHearingId));
            }

        };
    }

    private void processIndividualHearing(String caseId, String hearingId, Map<String, Element<UpdateHearingActualTracking>> trackingByHearingId) {
        Element<UpdateHearingActualTracking> entry = trackingByHearingId.get(hearingId);

        if (isNull(entry) || isNull(entry.getValue().getLastFiredDate())) {
            LocalDate today = LocalDate.now();
            if (entry != null) {
                entry.getValue().setLastFiredDate(today);
            } else {
                UpdateHearingActualTracking updateHearingActualTracking = UpdateHearingActualTracking.builder()
                    .hearingId(hearingId)
                    .lastFiredDate(today)
                    .build();
                trackingByHearingId.put(
                    hearingId,
                    Element.<UpdateHearingActualTracking>builder().id(UUID.randomUUID())
                        .value(updateHearingActualTracking)
                        .build()
                );
            }
            Map<String, Object> caseDataUpdated = new HashMap<>();
            caseDataUpdated.put(
                "updateHearingActualTracking",
                new ArrayList<>(trackingByHearingId.values())
            );

            triggerSystemEventForWorkAllocationTask(
                caseId, CaseEvent.ENABLE_UPDATE_HEARING_ACTUAL_TASK.getValue(), caseDataUpdated);
        } else {
            log.info("UpdateHearingActual Task has already been created for hearingId {}", hearingId);
        }
    }

    /**
     * For each candidate case, fires ENABLE_REQUEST_SOLICITOR_ORDER_TASK once per qualifying
     * hearing (the unit of work is the hearing, not the case). Each event carries
     * currentHearingId so the WA initiation DMN can bind the resulting task to that specific
     * hearing via additionalProperties.hearingId. Cadence is 1 working day for FL401
     * (FPVTL-2408) and 3 working days for C100 (FPVTL-2409). The per-hearing lastFiredDate
     * guard prevents re-firing while a task for that hearing is still open.
     */
    public void processRequestOrderTasks() {

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

    private List<String> getListOfCaseidsForHearings(List<CaseDetails> caseDetailsList) {
        return caseDetailsList.stream().map(CaseDetails::getId).map(String::valueOf).toList();

    }


    public List<CaseDetails> retrieveCasesWithHearingToday() {
        return runCcdSearch(buildTodaysHearingQueryParam());
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



    private List<String> fetchFieldsRequiredForHearingActualTask() {
        return List.of(
            "data.nextHearingDate",
            "data.updateHearingActualTracking",
            "data.draftOrderCollection",
            "data.orderCollection"
        );
    }
}
