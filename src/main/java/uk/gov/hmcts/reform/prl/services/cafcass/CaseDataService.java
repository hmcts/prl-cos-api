package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.filter.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.mapper.cafcass.CcdToCafcassObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.LastModified;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Query;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Range;

import java.io.IOException;
import java.util.List;

@Service
public class CaseDataService {
    @Value("${cafcaas.search-case-type-id}")
    private String cafCassSearchCaseTypeId;

    @Autowired
    CafcassCcdDataStoreService cafcassCcdDataStoreService;

    @Autowired
    private CcdToCafcassObjectMapper ccdToCafcassObjectMapper;

    @Autowired
    private CafCassFilter cafCassFilter;

    public CafCassResponse getCaseData(String authorisation, String serviceAuthorisation, String startDate, String endDate) throws IOException {
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        QueryParam ccdQueryParam = buildCcdQueryParam(startDate, endDate);
        SearchResult searchResult = cafcassCcdDataStoreService.searchCases(
            authorisation,
            objectMapper.writeValueAsString(ccdQueryParam),
            serviceAuthorisation,
            cafCassSearchCaseTypeId
        );
        objectMapper.writeValueAsString(searchResult);
        CafCassResponse cafCassResponse = objectMapper.convertValue(searchResult,
                                                             CafCassResponse.class);
        cafCassFilter.filter(cafCassResponse);
        return cafCassResponse;
    }

    private QueryParam buildCcdQueryParam(String startDate, String endDate) {
        LastModified lastModified = LastModified.builder().gte(startDate).lte(endDate).build();
        Range range = Range.builder().last_modified(lastModified).build();
        Query query = Query.builder().range(range).build();
        return QueryParam.builder().query(query).build();
    }
}
