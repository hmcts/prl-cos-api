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
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Sort;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.StateFilter;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateHearingActualsService {

    private static final ZoneId UK_ZONE = ZoneId.of("Europe/London");
    private static final String C100 = "C100";
    @Value("${request-order-task.concurrent-request}")
    private int concurrentRequest;

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
        QueryParam.QueryParamBuilder queryParamBuilder = QueryParam.builder();
        Semaphore hearingSemaphore = new Semaphore(concurrentRequest);
        String userToken = systemUserService.getSysUserToken();
        String s2sToken = authTokenGenerator.generate();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CaseDetails> caseDetailsList = retrieveCasesWithHearingToday(
                startAfter -> queryParamBuilder,
                "");
            if (isNotEmpty(caseDetailsList)) {
                log.info("Cases exist with current hearing");

                Map<String, List<String>> hearingsForToday = fetchAndFilterHearingsForTodaysDate(
                    executor,
                    hearingSemaphore,
                    userToken,
                    s2sToken,
                    getListOfCaseidsForHearings(caseDetailsList)
                );
                hearingsForToday.forEach(processHearings(caseDetailsList));
                String searchAfterValue = caseDetailsList.getLast().getId().toString();
                log.info("search after value {}", searchAfterValue);
                do {
                    caseDetailsList = retrieveCasesWithHearingToday(
                        startAfter -> queryParamBuilder
                            .searchAfter(List.of(startAfter)),
                        searchAfterValue);
                    if (!caseDetailsList.isEmpty()) {
                        hearingsForToday = fetchAndFilterHearingsForTodaysDate(
                            executor,
                            hearingSemaphore,
                            userToken,
                            s2sToken,
                            getListOfCaseidsForHearings(caseDetailsList));
                        hearingsForToday.forEach(processHearings(caseDetailsList));
                        searchAfterValue = caseDetailsList.getLast().getId().toString();
                        log.info("search after value {}", searchAfterValue);
                    }
                } while (!caseDetailsList.isEmpty());
            }
        } catch (Exception e) {
            log.error("Error while updating hearing actuals", e);
        }

    }

    private BiConsumer<String, List<String>> processHearings(List<CaseDetails> caseDetailsList) {
        return (caseId, hearingIds) ->
            caseDetailsList.stream()
                .filter(caseDetails -> String.valueOf(caseDetails.getId()).equals(caseId))
                .forEach(caseDetails ->
                    processHearing(caseId, hearingIds)
                );
    }

    private void processHearing(String caseId, List<String> hearingIdIterator) {
        hearingIdIterator.forEach(hearingId -> {
                StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificEvent(caseId, CaseEvent.ENABLE_UPDATE_HEARING_ACTUAL_TASK.getValue());
                CaseData caseData = startAllTabsUpdateDataContent.caseData();
                getUpdatedCaseData(caseData, hearingId)
                    .ifPresent(caseDataUpdated -> allTabService.submitAllTabsUpdate(
                        startAllTabsUpdateDataContent.authorisation(),
                        caseId,
                        startAllTabsUpdateDataContent.startEventResponse(),
                        startAllTabsUpdateDataContent.eventRequestData(),
                        caseDataUpdated
                    ));
            });
    }

    private  Optional<Map<String, Object>> getUpdatedCaseData(CaseData caseData, String hearingId) {
        UpdateHearingActualTracking updateHearingActualTracking = caseData.getUpdateHearingActualTracking().stream()
            .map(Element::getValue)
            .filter(value -> hearingId.equals(value.getHearingId()))
            .findFirst()
            .orElse(UpdateHearingActualTracking.builder()
                        .hearingId(hearingId)
                        .build());

        if (LocalDate.now().equals(updateHearingActualTracking.getLastFiredDate())) {
            return Optional.empty();
        }

        if (updateHearingActualTracking.getLastFiredDate() == null) {
            caseData.getUpdateHearingActualTracking().add(Element.<UpdateHearingActualTracking>builder().id(UUID.randomUUID())
                                                              .value(updateHearingActualTracking)
                                                              .build());
        }
        LocalDate today = LocalDate.now();
        updateHearingActualTracking.setLastFiredDate(today);

        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(
            "updateHearingActualTracking",
            caseData.getUpdateHearingActualTracking());
        return Optional.of(caseDataUpdated);
    }


    private Map<String, List<String>> fetchAndFilterHearingsForTodaysDate(ExecutorService executor,
                                                                          Semaphore semaphore,
                                                                          String userToken,
                                                                          String s2sToken,
                                                                          List<String> listOfCaseidsForHearings) {
        List<Future<Map<String, List<String>>>> futures = new ArrayList<>();
        Map<String, List<String>> caseHearingMap = new ConcurrentHashMap<>();

        for (int i = 0; i < listOfCaseidsForHearings.size(); i += concurrentRequest) {
            List<String> caseIds = List.copyOf(
                listOfCaseidsForHearings.subList(
                    i,
                    Math.min(i + concurrentRequest, listOfCaseidsForHearings.size())
                ));

            log.info("hearing semaphore permit count {}", semaphore.availablePermits());
            try {
                semaphore.acquire();
                futures.add(executor.submit(() -> {
                    try {
                        return hearingApiClient.getListedHearingsForAllCaseIdsOnCurrentDate(
                            userToken,
                            s2sToken,
                            caseIds
                        );
                    } finally {
                        semaphore.release();
                    }
                }));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while submitting hearing batch {}", caseIds, e);
                break;
            }
        }
        for (Future<Map<String, List<String>>> future : futures) {
            try {
                caseHearingMap.putAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt(); // restore interrupt status
                log.error("Interrupted while processing case", e);
            }
        }
        return caseHearingMap;
    }

    private List<String> getListOfCaseidsForHearings(List<CaseDetails> caseDetailsList) {
        return caseDetailsList.stream().map(CaseDetails::getId).map(String::valueOf).toList();
    }


    public List<CaseDetails> retrieveCasesWithHearingToday(Function<String, QueryParam.QueryParamBuilder> queryParamFunction,
                                                           String searchAfter) {
        return runCcdSearch(buildTodaysHearingQueryParam(queryParamFunction, searchAfter));
    }

    private List<CaseDetails> runCcdSearch(QueryParam ccdQueryParam) {
        SearchResultResponse response = SearchResultResponse.builder().cases(new ArrayList<>()).build();
        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String searchString = objectMapper.writeValueAsString(ccdQueryParam);
            log.info("json query {}",searchString);
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


    private QueryParam buildTodaysHearingQueryParam(Function<String, QueryParam.QueryParamBuilder> queryParamFunction, String searchAfter) {
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

        return queryParamFunction.apply(searchAfter)
                .query(Query.builder().bool(finalFilter).build())
                .size("100")
            .dataToReturn(fetchFieldsRequiredForHearingActualTask())
            .sort(List.of(Sort.builder().referenceKeyword("asc").build()))
                .build();
    }



    private List<String> fetchFieldsRequiredForHearingActualTask() {
        return List.of(
            "reference",
            "data.nextHearingDate",
            "data.updateHearingActualTracking"
        );
    }
}
