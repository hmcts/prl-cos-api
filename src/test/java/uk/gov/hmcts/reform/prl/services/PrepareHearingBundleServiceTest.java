package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.mapper.EsQueryObjectMapper;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrepareHearingBundleServiceTest {

    private static final String USER_TOKEN = "userToken";
    private static final String S2S_TOKEN = "s2sToken";

    @Spy
    private ObjectMapper objectMapper = EsQueryObjectMapper.getObjectMapper();

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private AllTabServiceImpl allTabService;

    @InjectMocks
    private PrepareHearingBundleService prepareHearingBundleService;

    @BeforeEach
    void setup() {
        when(systemUserService.getSysUserToken()).thenReturn(USER_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
    }

    @Test
    void shouldRetrieveCasesWithHearingsIn5Days() {
        CaseDetails caseDetails = CaseDetails.builder().id(123L).build();

        SearchResult searchResult = SearchResult.builder()
            .cases(List.of(caseDetails))
            .total(1)
            .build();

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(searchResult);

        List<CaseDetails> actualCases = prepareHearingBundleService.retrieveCasesWithHearingsIn5Days();

        assertNotNull(actualCases);
        assertEquals(1, actualCases.size());
        assertEquals(123L, actualCases.getFirst().getId());
    }

    @Test
    void shouldNotTriggerAnyEventsIfNoCasesFound() {
        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(SearchResult.builder().cases(List.of()).total(0).build());

        prepareHearingBundleService.searchForHearingsIn5DaysAndCreateTasks();

        verifyNoInteractions(allTabService);
    }

    @Test
    void shouldTriggerEventsOnlyOnCasesWithHearingsIn5Days() {
        CaseDetails case1 = CaseDetails.builder().id(111L).build();
        CaseDetails case2 = CaseDetails.builder().id(222L).build();

        SearchResult searchResult = SearchResult.builder()
            .cases(List.of(case1, case2))
            .total(2)
            .build();

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(searchResult);

        StartAllTabsUpdateDataContent mockContent = org.mockito.Mockito.mock(StartAllTabsUpdateDataContent.class);
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(mockContent);
        when(mockContent.authorisation()).thenReturn("auth");
        when(mockContent.startEventResponse()).thenReturn(null);
        when(mockContent.eventRequestData()).thenReturn(null);

        prepareHearingBundleService.searchForHearingsIn5DaysAndCreateTasks();

        verify(allTabService).getStartUpdateForSpecificEvent(eq("111"), anyString());
        verify(allTabService).getStartUpdateForSpecificEvent(eq("222"), anyString());
        verify(allTabService, times(2))
            .submitAllTabsUpdate(any(), anyString(), any(), any(), eq(Map.of()));
    }

}
