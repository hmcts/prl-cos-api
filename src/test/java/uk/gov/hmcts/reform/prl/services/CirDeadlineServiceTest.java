package uk.gov.hmcts.reform.prl.services;

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
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
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

@RunWith(MockitoJUnitRunner.class)
public class CirDeadlineServiceTest {

    private static final String AUTH_TOKEN = "authToken";
    private static final String S2S_TOKEN = "s2sToken";
    private static final long CASE_ID = 1773333307130623L;
    private static final int PAGE_SIZE = 100;

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

    private CaseDetails caseDetails;
    private StartAllTabsUpdateDataContent startAllTabsUpdateDataContent;

    @Before
    public void setUp() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);

        caseDetails = CaseDetails.builder()
            .id(CASE_ID)
            .data(Map.of())
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
            .thenReturn(SearchResult.builder().total(1).cases(List.of(caseDetails)).build());
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
        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(SearchResult.builder().total(0).cases(Collections.emptyList()).build());

        cirDeadlineService.checkAndCreateCirOverdueTasks();

        verify(allTabService, never()).getStartUpdateForSpecificEvent(anyString(), anyString());
        verify(allTabService, never()).submitAllTabsUpdate(any(), anyString(), any(), any(), any());
    }

    @Test
    public void checkAndCreateCirOverdueTasks_eventFireFails_continuesProcessingOtherCases() {
        CaseDetails case2 = CaseDetails.builder().id(9999L).data(Map.of()).build();
        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(SearchResult.builder().total(2).cases(List.of(caseDetails, case2)).build());
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
    public void retrieveOverdueCirCases_zeroTotal_returnsEmptyList() {
        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(SearchResult.builder().total(0).cases(Collections.emptyList()).build());

        List<CaseDetails> result = cirDeadlineService.retrieveOverdueCirCases();

        assertTrue(result.isEmpty());
        verify(coreCaseDataApi, times(1)).searchCases(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void retrieveOverdueCirCases_singlePage_returnsCasesFromSearchResult() {
        when(coreCaseDataApi.searchCases(eq(AUTH_TOKEN), eq(S2S_TOKEN), eq(CASE_TYPE), anyString()))
            .thenReturn(SearchResult.builder().total(1).cases(List.of(caseDetails)).build());

        List<CaseDetails> result = cirDeadlineService.retrieveOverdueCirCases();

        assertEquals(1, result.size());
        assertEquals(CASE_ID, result.get(0).getId().longValue());
        verify(coreCaseDataApi, times(1)).searchCases(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void retrieveOverdueCirCases_multiplePages_fetchesAllPages() {
        List<CaseDetails> page1Cases = buildCaseList(100, 1);
        List<CaseDetails> page2Cases = buildCaseList(50, 101);

        when(coreCaseDataApi.searchCases(eq(AUTH_TOKEN), eq(S2S_TOKEN), eq(CASE_TYPE), anyString()))
            .thenReturn(SearchResult.builder().total(150).cases(page1Cases).build())
            .thenReturn(SearchResult.builder().total(150).cases(page2Cases).build());

        List<CaseDetails> result = cirDeadlineService.retrieveOverdueCirCases();

        assertEquals(150, result.size());
        verify(coreCaseDataApi, times(2)).searchCases(anyString(), anyString(), anyString(), anyString());
    }

    private List<CaseDetails> buildCaseList(int count, int startId) {
        return IntStream.range(0, count)
            .mapToObj(i -> CaseDetails.builder().id((long) (startId + i)).data(Map.of()).build())
            .collect(toList());
    }
}
