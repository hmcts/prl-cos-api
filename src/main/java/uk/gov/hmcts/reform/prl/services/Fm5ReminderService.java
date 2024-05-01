package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Bool;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Match;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Must;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Query;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Should;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.StateFilter;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Fm5ReminderService {

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;

    private final ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();


    @Async
    public void sendFm5ReminderNotifications() {
        long startTime = System.currentTimeMillis();
        //Fetch all cases in Hearing state
        List<CaseDetails> cases = retrieveCasesInHearingState();

        if (CollectionUtils.isNotEmpty(cases)) {
            //Iterate all cases to evaluate rules to trigger FM5 reminder
            for (CaseDetails details : cases) {
                CaseData caseData = CaseUtils.getCaseData(details, objectMapper);
                log.info("Retrieved case from database, caseId {}", caseData.getId());
            }
        }
        log.info("*** Time taken to send fm5 reminders - {}s ***",
                 TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime));
    }

    private List<CaseDetails> retrieveCasesInHearingState() {

        SearchResultResponse response = SearchResultResponse.builder()
            .cases(new ArrayList<>()).build();

        QueryParam ccdQueryParam = buildCcdQueryParam();

        try {
            String searchString = objectMapper.writeValueAsString(ccdQueryParam);

            String userToken = systemUserService.getSysUserToken();
            final String s2sToken = authTokenGenerator.generate();
            SearchResult searchResult = coreCaseDataApi.searchCases(
                userToken,
                searchString,
                s2sToken,
                CASE_TYPE
            );

            response = objectMapper.convertValue(
                searchResult,
                SearchResultResponse.class
            );
        } catch (JsonProcessingException e) {
            log.error("Exception happened in parsing query param ", e);
        }

        if (null != response) {
            return response.getCases();
        }
        return Collections.emptyList();
    }

    private QueryParam buildCcdQueryParam() {
        //C100 cases where fm5 reminders are not sent already
        List<Should> shoulds = List.of(Should.builder()
                                             .match(Match.builder()
                                                        .caseTypeOfApplication("C100")
                                                        .fm5RemindersSent(YesOrNo.No)
                                                        .build())
                                             .build());

        //Hearing state
        StateFilter stateFilter = StateFilter.builder()
            .should(List.of(Should.builder().match(Match.builder()
                                                       .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue())
                                                       .build())
                                .build()))
            .build();

        Must mustFilter = Must.builder().stateFilter(stateFilter).build();
        Bool finalFilter = Bool.builder()
            .should(shoulds)
            .minimumShouldMatch(1)
            .must(mustFilter)
            .build();

        return QueryParam.builder()
            .query(Query.builder().bool(finalFilter).build())
            .build();
    }

}
