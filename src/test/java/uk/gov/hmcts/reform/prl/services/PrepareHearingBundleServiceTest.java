package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

    @Mock
    private EsQueryService esQueryService;

    @Mock
    private HearingService hearingService;

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
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);
        when(esQueryService.getObjectMapper()).thenReturn(getObjectMapper());
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

        List<Long> actualCases = prepareHearingBundleService.getCasesWithNextHearingDateByDate(
            LocalDate.now().plusDays(7),
            USER_TOKEN
        );

        assertNotNull(actualCases);
        assertEquals(1, actualCases.size());
        assertEquals(123L, actualCases.getFirst());
    }

    @Test
    void shouldNotTriggerAnyEventsIfNoCasesFound() {
        when(systemUserService.getSysUserToken()).thenReturn(USER_TOKEN);

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(SearchResult.builder().cases(List.of()).total(0).build());

        prepareHearingBundleService.searchForHearingsIn5DaysAndCreateTasks();

        verifyNoInteractions(allTabService);
    }

    @Test
    void shouldTriggerEventsOnlyOnCasesWithHearingsIn5Days() {
        when(systemUserService.getSysUserToken()).thenReturn(USER_TOKEN);

        CaseDetails case1 = CaseDetails.builder().id(111L).build();
        CaseDetails case2 = CaseDetails.builder().id(222L).build();

        SearchResult searchResult = SearchResult.builder()
            .cases(List.of(case1, case2))
            .total(2)
            .build();

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(searchResult);
        when(hearingService.filterCasesWithHearingsStartingOnDate(anyList(), anyString(), any(LocalDate.class)))
            .thenReturn(List.of(111L, 222L));

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

    @Test
    void shouldPaginateWhenMoreThan100Cases() {
        // Arrange: 100 cases on first page, 5 on second
        CaseDetails caseDetails1 = CaseDetails.builder().id(1L).build();
        CaseDetails caseDetails2 = CaseDetails.builder().id(2L).build();

        List<CaseDetails> firstPageCases = Collections.nCopies(100, caseDetails1);
        List<CaseDetails> secondPageCases = Collections.nCopies(5, caseDetails2);

        SearchResult firstPage = SearchResult.builder()
            .cases(firstPageCases)
            .total(105)
            .build();
        SearchResult secondPage = SearchResult.builder()
            .cases(secondPageCases)
            .total(105)
            .build();

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(firstPage, secondPage);

        List<Long> caseIds = prepareHearingBundleService.getCasesWithNextHearingDateByDate(
            LocalDate.now().plusDays(7),
            USER_TOKEN
        );

        assertNotNull(caseIds);
        assertEquals(105, caseIds.size());

        assertEquals(100, caseIds.stream().filter(id -> id.equals(1L)).count());
        assertEquals(5, caseIds.stream().filter(id -> id.equals(2L)).count());
    }

    public ObjectMapper getObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        om.disable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        return om;
    }

}
