package uk.gov.hmcts.reform.prl.services.cafcass;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

@Service
@Slf4j
public class CafcassCcdDataStoreService {

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    /**
     *  This method will call CCD searchCases API
     *  and return the result.
     *
     * @param authorisation Authorisation header
     * @param searchString json input for search
     * @param serviceAuthorisation S2S token
     * @param  caseType e.g. PRLAPPS
     * @return SearchResult object.
     */
    public SearchResult searchCases(String authorisation, String searchString, String serviceAuthorisation, String caseType) {
        log.info("333333");
        return coreCaseDataApi.searchCases(authorisation, serviceAuthorisation, caseType,
                                           searchString);

    }
}
