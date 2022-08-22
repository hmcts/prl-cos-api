package uk.gov.hmcts.reform.prl.services.cafcass;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.filter.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.cafcass.CcdToCafcassObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.LastModified;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Query;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.Range;

@Service
public class CaseDataService {

    @Autowired
    private CcdToCafcassObjectMapper ccdToCafcassObjectMapper;

    @Autowired
    private CafCassFilter cafCassFilter;

    public void getCaseData(String startDate, String endDate) {
        //TODO: Call the ccd api to get ccd data
        QueryParam ccdQueryParam = buildCcdQueryParam(startDate, endDate);

        //TODO: Map ccd data to cafcass data

        //TODO: Filter data based on postcode
        //CafCassResponse  cafCassResponse = new CafCassResponse();
        //cafCassFilter.filterCasesByApplicationValidPostcode(cafCassResponse);

    }

    private QueryParam buildCcdQueryParam(String startDate, String endDate) {
        LastModified lastModified = LastModified.builder().gte(startDate).lte(endDate).build();
        Range range = Range.builder().lastModified(lastModified).build();
        Query query = Query.builder().range(range).build();
        return QueryParam.builder().query(query).build();
    }
}
