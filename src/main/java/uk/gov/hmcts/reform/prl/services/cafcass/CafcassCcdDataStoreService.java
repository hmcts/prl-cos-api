package uk.gov.hmcts.reform.prl.services.cafcass;

import feign.FeignException;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;

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
    @Retry(name = "searchCasesRetryConfig", fallbackMethod = "searchCasesFallback")
    public SearchResult searchCases(String authorisation, String searchString, String serviceAuthorisation, String caseType) {

        return coreCaseDataApi.searchCases(authorisation, serviceAuthorisation, caseType,
                                           searchString);

    }

    public SearchResult searchCasesFallback(String authorisation,
                                            String searchString,
                                            String serviceAuthorisation,
                                            String caseType, Exception ex) {
        log.error("CCD Search failed completely after retries. Returning empty SearchResult. Error: {}",
                  ex.getMessage());

        return SearchResult.builder()
            .cases(new ArrayList<>())
            .total(0)
            .build();
    }
}
