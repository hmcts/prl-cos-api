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
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
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
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateHearingActualsService {

    private static final ZoneId UK_ZONE = ZoneId.of("Europe/London");
    @Value("${update-hearing-actuals.concurrent-request}")
    private int concurrentRequest;

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final HearingApiClient hearingApiClient;
    private final AllTabServiceImpl allTabService;

    private final ObjectMapper objectMapper;

    private static final String ES_PAGE_SIZE = "100";


    public void updateHearingActuals() {

        //Fetch all cases in Hearing state with a hearing today
        log.info("Running Hearing actual task cron job...");

        QueryParam.QueryParamBuilder queryParamBuilder = QueryParam.builder();
        Semaphore hearingSemaphore = new Semaphore(concurrentRequest);
        Semaphore caseSemaphore = new Semaphore(concurrentRequest);
        String userToken = systemUserService.getSysUserToken();
        String s2sToken = authTokenGenerator.generate();
        LocalDate currentDate = LocalDate.now();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            String searchAfter = "";

            while (true) {
                Function<String, QueryParam.QueryParamBuilder> queryParamFunction =  caseId -> caseId.isEmpty()
                    ? queryParamBuilder
                    : queryParamBuilder.searchAfter(List.of(caseId));

                List<CaseDetails> caseDetails = retrieveCasesWithHearingToday(
                    queryParamFunction,
                    searchAfter
                );

                if (caseDetails.isEmpty()) {
                    break;
                }

                processCaseBatch(
                    executor,
                    hearingSemaphore,
                    caseSemaphore,
                    userToken,
                    s2sToken,
                    currentDate,
                    caseDetails
                );

                searchAfter = caseDetails.getLast().getId().toString();
                log.info("search after value {}", searchAfter);
            }
        } catch (Exception e) {
            log.error("Error while updating hearing actuals", e);
        }

    }

    private void processCaseBatch(
        ExecutorService executor,
        Semaphore hearingSemaphore,
        Semaphore caseSemaphore,
        String userToken,
        String s2sToken,
        LocalDate currentDate,
        List<CaseDetails> caseDetails) {

        Map<String, List<String>> hearingsForToday = fetchAndFilterHearingsForTodaysDate(
                executor,
                hearingSemaphore,
                userToken,
                s2sToken,
                getListOfCaseidsForHearings(caseDetails));

        Map<String, CaseDetails> caseDetailsById = caseDetails.stream()
                .collect(Collectors.toMap(
                    caseDetail -> String.valueOf(caseDetail.getId()),
                    Function.identity()));

        hearingsForToday.forEach(
            process(
                executor,
                caseSemaphore,
                caseDetailsById,
                currentDate));
    }

    private BiConsumer<String, List<String>> process(ExecutorService executor,
                                                     Semaphore caseSemaphore,
                                                     Map<String, CaseDetails> caseDetailsById,
                                                     LocalDate currentDate) {
        return (caseId, hearingIds) -> {
            CaseDetails caseDetails = caseDetailsById.get(caseId);
            if (caseDetails != null) {
                submitCaseProcessing(
                    executor,
                    caseSemaphore,
                    caseDetails,
                    hearingIds,
                    currentDate
                );
            }
        };
    }

    private void submitCaseProcessing(
        ExecutorService executor,
        Semaphore semaphore,
        CaseDetails caseDetails,
        List<String> hearingIds,
        LocalDate currentDate) {
        try {
            log.info("case semaphore permit count {}", semaphore.availablePermits());
            semaphore.acquire();
            CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
            executor.submit(() -> {
                try {
                    processHearing(caseData, hearingIds, currentDate);
                } catch (Exception e) {
                    log.error("Error while processing case {}", caseDetails.getId(), e);
                } finally {
                    semaphore.release();
                }
            });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted while submitting case {}", caseDetails.getId(), e);
        }
    }

    private void processHearing(CaseData searchCaseData,
                                List<String> hearingIds,
                                LocalDate currentDate) {
        for (String hearingId : hearingIds) {
            if (isAlreadyProcessedToday(searchCaseData, hearingId, currentDate)) {
                log.info("Skipping case {} and hearing {} as it has already been processed today",
                         searchCaseData.getId(),
                         hearingId);
                continue;

            }
            log.info("Firing for case {} and hearing {}",
                     searchCaseData.getId(),
                     hearingId);
            String caseId = String.valueOf(searchCaseData.getId());
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificEvent(
                caseId,
                CaseEvent.ENABLE_UPDATE_HEARING_ACTUAL_TASK.getValue()
            );
            CaseData caseData = startAllTabsUpdateDataContent.caseData();
            List<Element<UpdateHearingActualTracking>> trackingByHearingIds = Optional.ofNullable(caseData.getUpdateHearingActualTracking())
                .orElse(new ArrayList<>());

            Map<String, Object> updatedCaseData = getUpdatedCaseData(hearingId, trackingByHearingIds, currentDate);
            allTabService.submitAllTabsUpdate(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                updatedCaseData
            );
        }
    }

    private static boolean isAlreadyProcessedToday(CaseData searchCaseData,
                                                   String hearingId,
                                                   LocalDate currentDate) {
        return Optional.ofNullable(searchCaseData.getUpdateHearingActualTracking())
            .orElse(Collections.emptyList())
            .stream()
            .map(Element::getValue)
            .anyMatch(tracking ->
                          hearingId.equals(tracking.getHearingId())
                              && currentDate.equals(tracking.getLastFiredDate()));
    }

    private  Map<String, Object> getUpdatedCaseData(String hearingId,
                                                    List<Element<UpdateHearingActualTracking>> trackingByHearingIds,
                                                    LocalDate currentDate) {
        UpdateHearingActualTracking updateHearingActualTracking = trackingByHearingIds.stream()
            .map(Element::getValue)
            .filter(value -> hearingId.equals(value.getHearingId()))
            .findFirst()
            .orElseGet(() -> {
                UpdateHearingActualTracking newTracking = UpdateHearingActualTracking.builder()
                    .hearingId(hearingId)
                    .build();
                trackingByHearingIds.add(Element.<UpdateHearingActualTracking>builder().id(UUID.randomUUID())
                                             .value(newTracking)
                                             .build());
                return newTracking;
            });
        updateHearingActualTracking.setLastFiredDate(currentDate);

        return Map.of("updateHearingActualTracking",
                      trackingByHearingIds);
    }


    private Map<String, List<String>> fetchAndFilterHearingsForTodaysDate(ExecutorService executor,
                                                                          Semaphore hearingSemaphore,
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

            log.info("hearing semaphore permit count {}", hearingSemaphore.availablePermits());
            try {
                hearingSemaphore.acquire();
                futures.add(executor.submit(() -> {
                    try {
                        return hearingApiClient.getListedHearingsForAllCaseIdsOnCurrentDate(
                            userToken,
                            s2sToken,
                            caseIds
                        );
                    } catch (Exception e) {
                        log.info("Exception while processing case starting with {} and ending with {}",
                                 caseIds.getFirst(),
                                 caseIds.getLast(),
                                 e);
                        return Collections.emptyMap();
                    } finally {
                        hearingSemaphore.release();
                    }
                }));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while submitting hearing batch {}", caseIds, e);
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


    private List<CaseDetails> retrieveCasesWithHearingToday(Function<String, QueryParam.QueryParamBuilder> queryParamFunction,
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
                .size(ES_PAGE_SIZE)
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
