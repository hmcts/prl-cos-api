package uk.gov.hmcts.reform.prl.services.cafcass;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CafcassCcdDataStoreService {
    private final CoreCaseDataApi coreCaseDataApi;

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
    @Retry(name = "searchCasesRetryConfig")
    public SearchResult searchCases(String authorisation,
                                    String searchString,
                                    String serviceAuthorisation,
                                    String caseType) {
        return coreCaseDataApi.searchCases(authorisation, serviceAuthorisation, caseType, searchString);
    }
}
