package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class UpdateHearingActualsServiceTest {
    private final String authToken = "authToken";
    private final String s2sAuthToken = "s2sAuthToken";
    @Mock
    ObjectMapper objectMapper;
    @Mock
    SystemUserService systemUserService;
    @Mock
    AuthTokenGenerator authTokenGenerator;
    @Mock
    CoreCaseDataApi coreCaseDataApi;
    @Mock
    AllTabServiceImpl allTabService;
    @Mock
    HearingApiClient hearingApiClient;
    private CaseDetails caseDetails;
    private CaseData caseData;

    @InjectMocks
    private UpdateHearingActualsService updateHearingActualsService;

    @Before
    public void setUp() {
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn(s2sAuthToken);

        caseData = CaseData.builder()
            .id(123L)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();
        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();

        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult);

        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class)).thenReturn(response);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        Map<String, List<String>> caseIdHearigIdMap = new HashMap<>();
        caseIdHearigIdMap.put("123", Collections.singletonList("123"));
        when(hearingApiClient.getListedHearingsForAllCaseIdsOnCurrentDate(any(), any(), anyList())).thenReturn(caseIdHearigIdMap);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            s2sAuthToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(
                objectMapper),
            caseData,
            null
        );
        when(allTabService.getStartUpdateForSpecificEvent(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(startAllTabsUpdateDataContent);
    }

    @Test
    public void testUpdateHearingActualTaskCreatedSuccessfully() {

        caseData = caseData.toBuilder()
            .id(123L)
            .draftOrderCollection(List.of(element(DraftOrder.builder()
                                                      .manageOrderHearingDetails(
                                                          List.of(element(HearingData.builder()
                                                                              .confirmedHearingDates(DynamicList.builder()
                                                                                                         .value(
                                                                                                             DynamicListElement.builder().code(
                                                                                                                 "1234").build()).build())
                                                                              .build())))
                                                      .build())))

            .orderCollection(List.of(element(OrderDetails.builder()
                                                 .manageOrderHearingDetails(
                                                     List.of(element(HearingData.builder()
                                                                         .confirmedHearingDates(DynamicList.builder()
                                                                                                    .value(
                                                                                                        DynamicListElement.builder().code(
                                                                                                            "1234").build()).build())
                                                                         .build())))
                                                 .build())))
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();
        caseDetails = caseDetails.toBuilder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();

        SearchResult searchResult1 = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult1);
        when(objectMapper.convertValue(searchResult1, SearchResultResponse.class)).thenReturn(response);

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        updateHearingActualsService.updateHearingActuals();
        verify(allTabService, times(2)).getStartUpdateForSpecificEvent(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testUpdateHearingActualTaskForDraftOrderCreatedForHearingId() {
        caseData = caseData.toBuilder()
            .id(123L)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .draftOrderCollection(List.of(
                element(DraftOrder.builder()
                            .manageOrderHearingDetails(List.of(
                                element(HearingData.builder()
                                            .confirmedHearingDates(
                                                DynamicList.builder()
                                                    .value(DynamicListElement.builder().code("123").build())
                                                    .listItems(List.of(DynamicListElement.defaultListItem("test")))
                                                    .build()
                                            )
                                            .build()
                                ))
                            )
                            .build())
            ))
            .build();
        caseDetails = caseDetails.toBuilder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString())).thenReturn(searchResult);

        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class)).thenReturn(response);
        updateHearingActualsService.updateHearingActuals();

        verify(allTabService, times(1)).getStartUpdateForSpecificEvent(Mockito.anyString(), Mockito.anyString());
    }


    @Test
    public void testExtractSelectedHearingId_whenHearingDataIsNull_shouldReturnNull() {
        String result = updateHearingActualsService.extractSelectedHearingId(null);
        assertNull(result);
    }

    @Test
    public void testExtractSelectedHearingId_whenConfirmedHearingDatesIsNull_shouldReturnNull() {
        HearingData hearingData = HearingData.builder()
            .confirmedHearingDates(null)
            .build();

        String result = updateHearingActualsService.extractSelectedHearingId(hearingData);
        assertNull(result);
    }

    @Test
    public void testExtractSelectedHearingId_whenDynamicListValueIsNull_shouldReturnNull() {
        HearingData hearingData = HearingData.builder()
            .confirmedHearingDates(DynamicList.builder().value(null).build())
            .build();

        String result = updateHearingActualsService.extractSelectedHearingId(hearingData);
        assertNull(result);
    }

    @Test
    public void testExtractSelectedHearingId_whenValid_shouldReturnCode() {
        DynamicListElement element = DynamicListElement.builder().code("test-code").build();
        HearingData hearingData = HearingData.builder()
            .confirmedHearingDates(DynamicList.builder().value(element).build())
            .build();

        String result = updateHearingActualsService.extractSelectedHearingId(hearingData);
        assertEquals("test-code", result);
    }

}
