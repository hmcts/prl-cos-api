package uk.gov.hmcts.reform.prl.services.cafcass;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;

@Service
public class CafcassCcdDataStoreService {

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    /**
     *  This method will call CCD searchCases API
     *  and return the result.
     *
     * @param authorisation Authorisation header
     * @param searchString json input for search
     * @param serviceAuthorisation
     * @return SearchResult object
     */
    public SearchResult searchCases(String authorisation, String searchString, String serviceAuthorisation, String cafCassSearchCaseTypeId) {

        return coreCaseDataApi.searchCases(authorisation, serviceAuthorisation, cafCassSearchCaseTypeId,
                                           searchString);

    }
}
