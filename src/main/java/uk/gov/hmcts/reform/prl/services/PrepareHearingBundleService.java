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
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
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
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeList;

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
    private final ObjectMapper objectMapper;

    @Value("${prepare-hearing-bundle.calendar-days-before-hearing:7}")
    private int calendarDaysBeforeHearing;

    private static final int PAGE_SIZE = 100;

    public void searchForHearingsIn5DaysAndCreateTasks() {
        LocalDate dateToCheck = LocalDate.now().plusDays(calendarDaysBeforeHearing);
        // Fetch all cases with a hearing in 5 days
        log.info("Running create 'Prepare Hearing Bundle' Work Allocation Task creation job");
        String userToken = systemUserService.getSysUserToken();

        List<CaseDetails> cases = getCasesWithNextHearingDateByDate(dateToCheck, userToken);
        try {
            if (isNotEmpty(cases)) {
                log.info("Found {} cases with nextHearingDate between now and {}", cases.size(), dateToCheck);
                cases = filterCasesWithNoRepresentation(cases);
                log.info("Found {} cases with no representation", cases.size());
                cases = hearingService.filterCasesWithHearingsStartingOnDate(cases, userToken, dateToCheck);
                log.info("After filtering against HMC, {} cases have hearings on {}", cases.size(), dateToCheck);
                createPrepareBundleWaTask(cases);
            } else {
                log.info("No cases exist with hearings on {}", dateToCheck);
            }
        } catch (Exception e) {
            log.error("Error while creating tasks for 'Prepare Hearing Bundle'", e);
        }
    }

    private List<CaseDetails> filterCasesWithNoRepresentation(List<CaseDetails> caseDetails) {
        List<CaseDetails> filteredCases = new ArrayList<>();
        caseDetails.stream().forEach(caseDetail -> {
            if (areNoPartiesRepresented(caseDetail)) {
                filteredCases.add(caseDetail);
            } else {
                log.info("Case {} has representation, skipping creation of 'Prepare Hearing Bundle' task",
                         caseDetail.getId());
            }
        });
        return filteredCases;
    }

    private boolean areNoPartiesRepresented(CaseDetails caseDetail) {
        CaseData caseData = CaseUtils.getCaseData(caseDetail, objectMapper);
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            return nullSafeList(caseData.getApplicants()).stream().noneMatch(
                el -> hasLegalRepresentation(el.getValue())
            ) &&
                nullSafeList(caseData.getRespondents()).stream().noneMatch(
                    el -> hasLegalRepresentation(el.getValue())
                );
        } else {
            return !hasLegalRepresentation(caseData.getApplicantsFL401()) && !hasLegalRepresentation(caseData.getRespondentsFL401());
        }
    }

    private boolean hasLegalRepresentation(PartyDetails partyDetails) {
        if (isNotEmpty(partyDetails)) {
            return YesNoDontKnow.yes.equals(partyDetails.getDoTheyHaveLegalRepresentation());
        }
        return false;
    }

    private void createPrepareBundleWaTask(List<CaseDetails> cases) {
        cases.forEach(caseDetails ->
                            triggerSystemEventForWorkAllocationTask(
                                caseDetails.getId().toString(),
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

    public List<CaseDetails> getCasesWithNextHearingDateByDate(LocalDate dateToCheck, String userToken) {
        ObjectMapper esQueryObjectMapper = esQueryService.getObjectMapper();
        try {
            List<CaseDetails> caseDetailsList = new ArrayList<>();
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
                caseDetailsList.addAll(searchResult.getCases());

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
                        paginatedSearchResult.getCases());
                }
            }

            log.info("Total no. of cases to process {}", caseDetailsList.size());
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
