package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CirDeadlineServiceTest {

    private static final String AUTH_TOKEN = "authToken";
    private static final String S2S_TOKEN = "s2sToken";
    private static final long CASE_ID = 1773333307130623L;

    @InjectMocks
    private CirDeadlineService cirDeadlineService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AllTabServiceImpl allTabService;

    @Mock
    private ObjectMapper objectMapper;

    private CaseDetails caseDetails;
    private SearchResult searchResult;
    private SearchResultResponse searchResultResponse;
    private StartAllTabsUpdateDataContent startAllTabsUpdateDataContent;

    @Before
    public void setUp() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);

        caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .data(Map.of())
            .build();

        searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();

        searchResultResponse = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();

        startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            AUTH_TOKEN,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            Map.of(),
            null,
            null
        );
    }

    @Test
    public void checkAndCreateCirOverdueTasks_firesCirOverdueEventForEachCase() {
        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(searchResult);
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class))
            .thenReturn(searchResultResponse);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString()))
            .thenReturn(startAllTabsUpdateDataContent);

        cirDeadlineService.checkAndCreateCirOverdueTasks();

        verify(allTabService, times(1))
            .getStartUpdateForSpecificEvent(String.valueOf(CASE_ID), "cirOverdueTaskCreation");
        verify(allTabService, times(1))
            .submitAllTabsUpdate(any(), anyString(), any(), any(), any());
    }

    @Test
    public void checkAndCreateCirOverdueTasks_noOverdueCases_doesNotFireEvent() {
        SearchResultResponse emptyResponse = SearchResultResponse.builder()
            .total(0)
            .cases(Collections.emptyList())
            .build();
        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(SearchResult.builder().total(0).cases(Collections.emptyList()).build());
        when(objectMapper.convertValue(any(), any(Class.class))).thenReturn(emptyResponse);

        cirDeadlineService.checkAndCreateCirOverdueTasks();

        verify(allTabService, never()).getStartUpdateForSpecificEvent(anyString(), anyString());
        verify(allTabService, never()).submitAllTabsUpdate(any(), anyString(), any(), any(), any());
    }

    @Test
    public void checkAndCreateCirOverdueTasks_eventFireFails_continuesProcessingOtherCases() {
        CaseDetails case2 = CaseDetails.builder().id(9999L).data(Map.of()).build();
        SearchResultResponse multiResponse = SearchResultResponse.builder()
            .total(2)
            .cases(List.of(caseDetails, case2))
            .build();
        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(SearchResult.builder().total(2).cases(List.of(caseDetails, case2)).build());
        when(objectMapper.convertValue(any(SearchResult.class), any(Class.class))).thenReturn(multiResponse);
        when(allTabService.getStartUpdateForSpecificEvent(String.valueOf(CASE_ID), "cirOverdueTaskCreation"))
            .thenThrow(new RuntimeException("CCD error"));
        when(allTabService.getStartUpdateForSpecificEvent(String.valueOf(9999L), "cirOverdueTaskCreation"))
            .thenReturn(startAllTabsUpdateDataContent);

        cirDeadlineService.checkAndCreateCirOverdueTasks();

        // second case should still be processed despite first failing
        verify(allTabService, times(1))
            .getStartUpdateForSpecificEvent(String.valueOf(9999L), "cirOverdueTaskCreation");
        verify(allTabService, times(1))
            .submitAllTabsUpdate(any(), anyString(), any(), any(), any());
    }

    @Test
    public void retrieveOverdueCirCases_searchThrowsException_returnsEmptyList() {
        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Search error"));

        List<CaseDetails> result = cirDeadlineService.retrieveOverdueCirCases();

        assertTrue(result.isEmpty());
    }

    @Test
    public void retrieveOverdueCirCases_nullResponse_returnsEmptyList() {
        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(SearchResult.builder().build());
        when(objectMapper.convertValue(any(), any(Class.class))).thenReturn(null);

        List<CaseDetails> result = cirDeadlineService.retrieveOverdueCirCases();

        assertTrue(result.isEmpty());
    }

    @Test
    public void retrieveOverdueCirCases_returnsCasesFromSearchResult() {
        when(coreCaseDataApi.searchCases(eq(AUTH_TOKEN), eq(S2S_TOKEN), eq(CASE_TYPE), anyString()))
            .thenReturn(searchResult);
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class))
            .thenReturn(searchResultResponse);

        List<CaseDetails> result = cirDeadlineService.retrieveOverdueCirCases();

        assertEquals(1, result.size());
        assertEquals(CASE_ID, result.get(0).getId().longValue());
    }
}
