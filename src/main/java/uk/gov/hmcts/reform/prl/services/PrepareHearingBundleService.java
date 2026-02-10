package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Bool;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Filter;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.LastModified;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Match;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Must;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Query;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Range;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Should;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.StateFilter;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrepareHearingBundleService {

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AllTabServiceImpl allTabService;
    private final EsQueryService esQueryService;
    private final HearingApiClient hearingApiClient;

    private static final LocalDate DATE_TO_CHECK = LocalDate.now().plusDays(7);

    private static final int PAGE_SIZE = 100;

    public void searchForHearingsIn5DaysAndCreateTasks() {
        // Fetch all cases with a hearing in 5 days
        log.info("Running create 'Prepare Hearing Bundle' Work Allocation Task creation job");
        String auth = systemUserService.getSysUserToken();
        String serviceAuth = authTokenGenerator.generate();

        List<Long> caseIds = retrieveCasesWithHearingsIn5Days();
        try {
            if (isNotEmpty(caseIds)) {
                log.info("Cases exist with hearings on {}", DATE_TO_CHECK);
                caseIds = filterCasesWithHearingsInExactly5Days(caseIds, auth, serviceAuth);
                createPrepareBundleWaTask(caseIds);
            } else {
                log.info("No cases exist with hearings on {}", DATE_TO_CHECK);
            }
        } catch (Exception e) {
            log.error("Error while creating tasks for 'Prepare Hearing Bundle'", e);
        }
    }

    private List<Long> filterCasesWithHearingsInExactly5Days(List<Long> caseIds, String auth, String serviceAuth) {
        List<Long> filteredCaseIds = new ArrayList<>();
        if (caseIds == null || caseIds.isEmpty()) {
            return filteredCaseIds;
        }
        int batchSize = 25;

        for (int i = 0; i < caseIds.size(); i += batchSize) {
            List<String> batch = caseIds.subList(i, Math.min(i + batchSize, caseIds.size()))
                .stream().map(String::valueOf).toList();
            try {
                List<Hearings> hearingsList = hearingApiClient.getHearingsForAllCaseIdsWithCourtVenue(auth, serviceAuth, batch);
                for (Hearings hearing : hearingsList) {
                    String caseId = hearing.getCaseRef();
                    if (isNotEmpty(hearing.getCaseHearings()) && hasHearingStartingOnDate(hearing)) {
                        filteredCaseIds.add(Long.parseLong(caseId));
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching hearings for case batch: {}", batch, e);
            }
        }
        return filteredCaseIds;
    }

    public boolean hasHearingStartingOnDate(Hearings hearing) {
        if (isEmpty(hearing) || isEmpty(hearing.getCaseHearings())) {
            return false;
        }
        return hearing.getCaseHearings().stream().anyMatch(ch -> {
            if (isEmpty(ch.getHearingDaySchedule())) {
                return false;
            }
            return ch.getHearingDaySchedule().stream()
                // Get the earliest day of the hearing
                .map(HearingDaySchedule::getHearingStartDateTime)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .min(LocalDate::compareTo)
                // and check if it's 5 working days from now
                .map(DATE_TO_CHECK::equals)
                .orElse(false);
        });
    }

    private void createPrepareBundleWaTask(List<Long> caseIds) {
        caseIds.forEach(caseId ->
                            triggerSystemEventForWorkAllocationTask(
                                caseId.toString(),
                                CaseEvent.ENABLE_PREPARE_HEARING_BUNDLE_TASK.getValue()
                            )
        );
    }

    private void triggerSystemEventForWorkAllocationTask(String caseId, String caseEvent) {
        try {
            log.info("Creating initiation event {} for case id : {}", caseEvent, caseId);
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificEvent(
                caseId,
                caseEvent
            );
            allTabService.submitAllTabsUpdate(
                startAllTabsUpdateDataContent.authorisation(),
                caseId,
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                new HashMap<>()
            );
        } catch (Exception e) {
            // Catch generic exception to avoid failure of the entire batch
            log.error("Error while triggering system event {} for case id : {}", caseEvent, caseId, e);
        }
    }

    public List<Long> retrieveCasesWithHearingsIn5Days() {
        ObjectMapper esQueryObjectMapper = esQueryService.getObjectMapper();
        try {
            List<Long> caseDetailsList = new ArrayList<>();
            QueryParam ccdQueryParam = buildCcdQueryParam();

            String searchString = esQueryObjectMapper.writeValueAsString(ccdQueryParam);
            log.info(searchString);
            String userToken = systemUserService.getSysUserToken();
            final String s2sToken = authTokenGenerator.generate();
            SearchResult searchResult = coreCaseDataApi.searchCases(userToken, s2sToken, CASE_TYPE, searchString);

            int totalCases = searchResult.getTotal();
            int pages = (int) Math.ceil((double) totalCases / PAGE_SIZE);
            log.info("Search result size {}, split across {} pages", totalCases, pages);

            if (totalCases == 0) {
                return caseDetailsList;
            }

            if (isNotEmpty(searchResult)) {
                // add first page from initial search
                caseDetailsList.addAll(searchResult.getCases().stream().map(CaseDetails::getId).toList());

                // process remaining pages
                for (int i = 1; i < pages; i++) {
                    log.info("Processing page {} of {}", i + 1, pages);
                    QueryParam paginatedQueryParam = buildCcdQueryParam(i * PAGE_SIZE);
                    String paginatedSearchString = esQueryObjectMapper.writeValueAsString(paginatedQueryParam);

                    SearchResult paginatedSearchResult = coreCaseDataApi.searchCases(
                        userToken,
                        s2sToken,
                        CASE_TYPE,
                        paginatedSearchString
                    );
                    caseDetailsList.addAll(
                        paginatedSearchResult.getCases().stream().map(CaseDetails::getId).toList());
                }
            }

            log.info("Total no. of case ids to process {}", caseDetailsList.size());
            return caseDetailsList;
        } catch (JsonProcessingException e) {
            log.error("Exception happened in parsing query param ", e);
            return Collections.emptyList();
        }
    }

    private QueryParam buildCcdQueryParam() {
        return buildCcdQueryParam(0);
    }

    private QueryParam buildCcdQueryParam(int from) {
        List<Should> shoulds = List.of(
            Should.builder().match(Match.builder().caseTypeOfApplication("C100").build()).build(),
            Should.builder().match(Match.builder().caseTypeOfApplication("FL401").build()).build()
        );

        // get all cases with _at least one hearing_ in the next 5 working days, there may be 2, one tomorrow
        // and one in 5 days, which will be obscured by the nextHearingDate of tomorrow
        Range nextHearingDateRange = Range.builder()
            .nextHearingDate(LastModified.builder()
                                 .gte(LocalDate.now().toString())
                                 .lte(DATE_TO_CHECK.toString())
                                 .build())
            .build();

        // Hearing state(s)
        StateFilter stateFilter = esQueryService.getFilterForStates(
            List.of(
                State.JUDICIAL_REVIEW,
                State.PREPARE_FOR_HEARING_CONDUCT_HEARING,
                State.DECISION_OUTCOME
            ));

        Must mustFilter = Must.builder().stateFilter(stateFilter).build();

        Bool finalFilter = Bool.builder()
            .filter(Filter.builder().range(nextHearingDateRange).build())
            .should(shoulds)
            .minimumShouldMatch(2)
            .must(mustFilter)
            .build();

        return QueryParam.builder()
            .from(String.valueOf(from))
            .query(Query.builder().bool(finalFilter).build())
            .size(String.valueOf(PAGE_SIZE))
            .dataToReturn(List.of("data.nextHearingDate"))
            .build();
    }
}
