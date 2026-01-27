package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Bool;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Match;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Must;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Query;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Should;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.StateFilter;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PrepareHearingBundleService {

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final AllTabServiceImpl allTabService;

    private final ObjectMapper objectMapper;

    public void searchForHearingsIn5DaysAndCreateTasks() {

        // Fetch all cases with a hearing in 5 days
        log.info("Running create 'Prepare Hearing Bundle' Work Allocation Task creation job");
        List<CaseDetails> caseDetailsList = retrieveCasesWithHearingsIn5Days();
        try {
            if (isNotEmpty(caseDetailsList)) {
                log.info("Cases exist with hearings in 5 days");
                createPrepareBundleWaTask(caseDetailsList.stream().map(CaseDetails::getId).toList());
            } else {
                log.info("No cases exist with hearings in 5 days");
            }
        } catch (Exception e) {
            log.error("Error while creating tasks for 'Prepare Hearing Bundle'", e);
        }
    }

    private void createPrepareBundleWaTask(List<Long> caseIds) {
        caseIds.forEach(caseId ->
                            triggerSystemEventForWorkAllocationTask(caseId.toString(),
                                                                    CaseEvent.ENABLE_PREPARE_HEARING_BUNDLE_TASK.getValue())
        );
    }

    private void triggerSystemEventForWorkAllocationTask(String caseId, String caseEvent) {
        log.info("Creating initiation event {} for case id : {}", caseEvent, caseId);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificEvent(caseId, caseEvent);
        allTabService.submitAllTabsUpdate(
            startAllTabsUpdateDataContent.authorisation(),
            caseId,
            startAllTabsUpdateDataContent.startEventResponse(),
            startAllTabsUpdateDataContent.eventRequestData(),
            new HashMap<>()
        );
    }

    public List<CaseDetails> retrieveCasesWithHearingsIn5Days() {
        SearchResultResponse response = SearchResultResponse.builder().cases(new ArrayList<>()).build();

        QueryParam ccdQueryParam = buildCcdQueryParam();

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


    private QueryParam buildCcdQueryParam() {
        // All cases with nextHearingDate in 5 days
        List<Should> shoulds = List.of(
                Should.builder().match(Match.builder().caseTypeOfApplication("C100").build()).build(),
                Should.builder().match(Match.builder().caseTypeOfApplication("FL401").build()).build(),
                Should.builder().match(Match.builder().nextHearingDate(LocalDate.now().plusDays(5)).build()).build()
        );

        // Hearing state(s)
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
            .dataToReturn(List.of("data.nextHearingDate"))
                .build();
    }
}
