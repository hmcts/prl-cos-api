package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
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
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
    private final HearingService hearingService;

    @Value("${prepare-hearing-bundle.calendar-days-before-hearing:7}")
    private int calendarDaysBeforeHearing;

    private static final int PAGE_SIZE = 100;

    public void searchForHearingsIn5DaysAndCreateTasks() {
        LocalDate dateToCheck = LocalDate.now().plusDays(calendarDaysBeforeHearing);
        // Fetch all cases with a hearing in 5 days
        log.info("Running create 'Prepare Hearing Bundle' Work Allocation Task creation job");
        String userToken = systemUserService.getSysUserToken();

        List<Long> caseIds = getCasesWithNextHearingDateByDate(dateToCheck, userToken);
        try {
            if (isNotEmpty(caseIds)) {
                log.info("Cases exist with hearings on {}", dateToCheck);
                caseIds = hearingService.filterCasesWithHearingsStartingOnDate(caseIds, userToken, dateToCheck);
                createPrepareBundleWaTask(caseIds);
            } else {
                log.info("No cases exist with hearings on {}", dateToCheck);
            }
        } catch (Exception e) {
            log.error("Error while creating tasks for 'Prepare Hearing Bundle'", e);
        }
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

    public List<Long> getCasesWithNextHearingDateByDate(LocalDate dateToCheck, String userToken) {
        ObjectMapper esQueryObjectMapper = esQueryService.getObjectMapper();
        try {
            List<Long> caseDetailsList = new ArrayList<>();
            QueryParam ccdQueryParam = buildCcdQueryParam(dateToCheck);

            String searchString = esQueryObjectMapper.writeValueAsString(ccdQueryParam);
            log.info(searchString);
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
                    QueryParam paginatedQueryParam = buildCcdQueryParam(i * PAGE_SIZE, dateToCheck);
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

    private QueryParam buildCcdQueryParam(LocalDate dateToCheck) {
        return buildCcdQueryParam(0, dateToCheck);
    }

    private QueryParam buildCcdQueryParam(int from, LocalDate dateToCheck) {
        List<Should> shoulds = List.of(
            Should.builder().match(Match.builder().caseTypeOfApplication("C100").build()).build(),
            Should.builder().match(Match.builder().caseTypeOfApplication("FL401").build()).build()
        );

        // get all cases with _at least one hearing_ in the next 5 working days, there may be 2, one tomorrow
        // and one in 5 days, which will be obscured by the nextHearingDate of tomorrow
        Range nextHearingDateRange = Range.builder()
            .nextHearingDate(LastModified.builder()
                                 .gte(LocalDate.now().toString())
                                 .lte(dateToCheck.toString())
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
            .minimumShouldMatch(1)
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
