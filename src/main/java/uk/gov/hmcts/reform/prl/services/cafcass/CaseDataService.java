package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
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
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseDataService {
    @Value("${cafcaas.search-case-type-id}")
    private String cafCassSearchCaseTypeId;

    @Value("${ccd.elastic-search-api.result-size}")
    private String ccdElasticSearchApiResultSize;

    @Value("${ccd.elastic-search-api.boost}")
    private String ccdElasticSearchApiBoost;

    @Value("#{'${cafcaas.caseState}'.split(',')}")
    private List<String> caseStateList;


    private final HearingService hearingService;

    private final CafcassCcdDataStoreService cafcassCcdDataStoreService;

    private final CafCassFilter cafCassFilter;

    private final  AuthTokenGenerator authTokenGenerator;

    private final SystemUserService systemUserService;

    public CafCassResponse getCaseData(String authorisation, String startDate, String endDate) throws IOException {

        log.info("Search API start date - {}, end date - {}", startDate, endDate);

        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        QueryParam ccdQueryParam = buildCcdQueryParam(startDate, endDate);
        String searchString = objectMapper.writeValueAsString(ccdQueryParam);

        String userToken = systemUserService.getSysUserToken();
        SearchResult searchResult = cafcassCcdDataStoreService.searchCases(
            userToken,
            searchString,
            authTokenGenerator.generate(),
            cafCassSearchCaseTypeId
        );
        log.info("total number of records retrieved from ccd - {}", searchResult.getTotal());
        CafCassResponse cafCassResponse = objectMapper.convertValue(
            searchResult,
            CafCassResponse.class
        );

        if (cafCassResponse.getCases() != null && !cafCassResponse.getCases().isEmpty()) {

            cafCassFilter.filter(cafCassResponse);
            log.info("total number of records after applying cafcass filters - {}", cafCassResponse.getCases().size());
            getHearingDetails(authorisation, cafCassResponse);
            updateHearingResponse(authorisation, authTokenGenerator.generate(), cafCassResponse);

        } else {
            cafCassResponse = CafCassResponse.builder().cases(new ArrayList<>()).build();
        }
        return cafCassResponse;
    }

    private QueryParam buildCcdQueryParam(String startDate, String endDate) {

        List<Should> shoulds = populateStatesForQuery();

        LastModified lastModified = LastModified.builder().gte(startDate).lte(endDate).boost(ccdElasticSearchApiBoost).build();
        Range range = Range.builder().lastModified(lastModified).build();

        StateFilter stateFilter = StateFilter.builder().should(shoulds).build();
        Filter filter = Filter.builder().range(range).build();
        Must must = Must.builder().stateFilter(stateFilter).build();
        Bool bool = Bool.builder().filter(filter).must(must).build();
        Query query = Query.builder().bool(bool).build();
        return QueryParam.builder().query(query).size(ccdElasticSearchApiResultSize).build();
    }

    private List<Should> populateStatesForQuery() {
        caseStateList = caseStateList.stream().map(String::trim).collect(Collectors.toList());

        List<Should> shoulds = new ArrayList<>();
        if (caseStateList != null && !caseStateList.isEmpty()) {
            for (String caseState:caseStateList
                 ) {
                shoulds.add(Should.builder().match(Match.builder().state(caseState).build()).build());
            }
        }
        return shoulds;
    }

    /**
     * Fetch the hearing data from fis hearing service.
     *@param authorisation //
     *@param cafCassResponse //
     */
    private void getHearingDetails(String authorisation, CafCassResponse cafCassResponse) {

        for (CafCassCaseDetail cafCassCaseDetail: cafCassResponse.getCases()) {
            cafCassCaseDetail.getCaseData().setHearingData(hearingService.getHearings(authorisation,
                                                                                      String.valueOf(cafCassCaseDetail.getId())));
        }
    }

    private void updateHearingResponse(String authorisation, String s2sToken, CafCassResponse cafCassResponse) {

        final Hearings hearing = cafCassResponse.getCases().stream()
            .filter(cafCassCaseDetail -> cafCassCaseDetail.getCaseData().getHearingData() != null)
            .map(cafCassCaseDetail -> cafCassCaseDetail.getCaseData().getHearingData())
            .findFirst().orElse(null);

        if (hearing != null) {
            final Map<String, String> refDataCategoryValueMap = hearingService.getRefDataCategoryValueMap(
                authorisation,
                s2sToken,
                hearing.getHmctsServiceCode()
            );

            cafCassResponse.getCases().stream().forEach(cafCassCaseDetail -> {
                final Hearings hearingData = cafCassCaseDetail.getCaseData().getHearingData();
                if (null != hearingData) {
                    hearingData.getCaseHearings().stream().forEach(caseHearing -> {
                        caseHearing.setHearingTypeValue(refDataCategoryValueMap.get(caseHearing.getHearingType()));
                    });
                }
            });
        }
    }
}
