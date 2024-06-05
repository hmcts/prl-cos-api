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
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
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
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;

@Slf4j
@Service
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateHearingActualsService {

    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    private final HearingApiClient hearingApiClient;
    private final AllTabServiceImpl allTabService;

    private final ObjectMapper objectMapper;


    public void updateHearingActuals() {

        long startTime = System.currentTimeMillis();
        //Fetch all cases in Hearing state pending fm5 reminder notifications
        List<CaseDetails> caseDetailsList = retrieveCasesInHearingState();
        if (isNotEmpty(caseDetailsList)) {

        }
    }


    public List<CaseDetails> retrieveCasesInHearingState() {

        SearchResultResponse response = SearchResultResponse.builder()
            .cases(new ArrayList<>()).build();

        QueryParam ccdQueryParam = buildCcdQueryParam();

        try {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
            String searchString = objectMapper.writeValueAsString(ccdQueryParam);

            String userToken = systemUserService.getSysUserToken();
            final String s2sToken = authTokenGenerator.generate();
            SearchResult searchResult = coreCaseDataApi.searchCases(
                userToken,
                s2sToken,
                CASE_TYPE,
                searchString
            );

            response = objectMapper.convertValue(
                searchResult,
                SearchResultResponse.class
            );
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
        //C100 cases where fm5 reminders are not sent already
        List<Should> shoulds = List.of(Should.builder()
                                           .match(Match.builder()
                                                      .caseTypeOfApplication("C100")
                                                      .build())
                                           .build(),
                                       Should.builder()
                                           .match(Match.builder()
                                                      .caseTypeOfApplication("FL401")
                                                      .build())
                                           .build());

        //Hearing state
        StateFilter stateFilter = StateFilter.builder()
            .should(List.of(Should.builder().match(Match.builder()
                                                       .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue())
                                                       .build())
                                .build(),
                            Should.builder().match(Match.builder()
                                                       .state(State.DECISION_OUTCOME.getValue())
                                                       .build())
                                .build()))
            .build();
        Must mustFilter = Must.builder().stateFilter(stateFilter).build();

        Bool finalFilter = Bool.builder()
            .should(shoulds)
            .minimumShouldMatch(2)
            .must(mustFilter)
            .build();

        return QueryParam.builder()
            .query(Query.builder().bool(finalFilter).build())
            .build();
    }

}
