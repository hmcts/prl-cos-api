package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.LastModified;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Query;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Range;

import java.io.IOException;

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


    private final HearingService hearingService;

    private final CafcassCcdDataStoreService cafcassCcdDataStoreService;

    private final CafCassFilter cafCassFilter;

    private final  AuthTokenGenerator authTokenGenerator;

    public CafCassResponse getCaseData(String authorisation, String serviceAuthorisation, String startDate, String endDate) throws IOException {
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        QueryParam ccdQueryParam = buildCcdQueryParam(startDate, endDate);
        String searchString = objectMapper.writeValueAsString(ccdQueryParam);
        SearchResult searchResult = cafcassCcdDataStoreService.searchCases(
            authorisation,
            searchString,
            authTokenGenerator.generate(),
            cafCassSearchCaseTypeId
        );
        CafCassResponse cafCassResponse = objectMapper.convertValue(searchResult,
                                                             CafCassResponse.class);
        log.info("cafCassResponse11111 ==> {}",cafCassResponse);
        cafCassFilter.filter(cafCassResponse);
        log.info("cafCassResponse222 ==> {}",cafCassResponse);
        getHearingDetails(authorisation,cafCassResponse);
        return cafCassResponse;
    }

    private QueryParam buildCcdQueryParam(String startDate, String endDate) {
        LastModified lastModified = LastModified.builder().gte(startDate).lte(endDate).boost(ccdElasticSearchApiBoost).build();
        Range range = Range.builder().lastModified(lastModified).build();
        Query query = Query.builder().range(range).build();
        return QueryParam.builder().query(query).size(ccdElasticSearchApiResultSize).build();
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


}
