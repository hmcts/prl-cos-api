package uk.gov.hmcts.reform.prl.services.cafcass;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.PRL_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_AUTHORIZATION;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_SERVICE_AUTHORIZATION;

@RunWith(MockitoJUnitRunner.class)
public class CafcassCcdDataStoreServiceTest {

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @InjectMocks
    private CafcassCcdDataStoreService cafcassCcdDataStoreService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("test case for CCD elastic search.")
    public void testSearchCases() throws IOException {


        String searchString = TestResourceUtil.readFileFrom("classpath:request/ccdsearchrequest.json");

        SearchResult mockResult = SearchResult.builder().cases(Arrays.asList(CaseDetails.builder()
                                                                                 .caseTypeId(PRL_CASE_TYPE).build())).build();

        when(coreCaseDataApi.searchCases(TEST_AUTHORIZATION, TEST_SERVICE_AUTHORIZATION, PRL_CASE_TYPE, searchString))
            .thenReturn(mockResult);
        SearchResult searchResult = cafcassCcdDataStoreService.searchCases(TEST_AUTHORIZATION, searchString,
                                                                           TEST_SERVICE_AUTHORIZATION,
                                                                           PRL_CASE_TYPE);
        assertNotNull(searchResult);
        assertEquals(PRL_CASE_TYPE, mockResult.getCases().get(0).getCaseTypeId());
    }
}
