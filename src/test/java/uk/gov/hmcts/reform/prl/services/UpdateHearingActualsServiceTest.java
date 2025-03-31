package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@ExtendWith(MockitoExtension.class)
class UpdateHearingActualsServiceTest {

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

    @InjectMocks
    private UpdateHearingActualsService updateHearingActualsService;

    private CaseData caseData;
    private CaseDetails caseDetails;

    void setupCommonStubs() {
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn(s2sAuthToken);

        caseData = CaseData.builder()
            .id(123L)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();

        doReturn(new HashMap<String, Object>())
            .when(objectMapper)
            .convertValue(any(), ArgumentMatchers.<TypeReference<Map<String, Object>>>any());

        caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseData.toMap(objectMapper))
            .build();
    }

    @Test
    void testUpdateHearingActualTaskCreatedSuccessfully() {
        setupCommonStubs();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();

        doReturn(response).when(objectMapper).convertValue(searchResult, SearchResultResponse.class);
        doReturn(caseData).when(objectMapper).convertValue(caseDetails.getData(), CaseData.class);
        when(coreCaseDataApi.searchCases(eq(authToken), eq(s2sAuthToken), eq(CASE_TYPE), anyString()))
            .thenReturn(searchResult);

        List<Element<HearingData>> hearingDataElement = List.of(
            element(HearingData.builder()
                        .confirmedHearingDates(DynamicList.builder()
                                                   .value(
                                                       DynamicListElement.builder()
                                                           .code("1234")
                                                           .build())
                                                   .build())
                        .build()));
        caseData = caseData.toBuilder()
            .draftOrderCollection(List.of(element(DraftOrder.builder()
                                                      .manageOrderHearingDetails(hearingDataElement)
                                                      .build())))
            .orderCollection(List.of(element(OrderDetails.builder()
                                                 .manageOrderHearingDetails(hearingDataElement)
                                                 .build())))
            .build();

        caseDetails = caseDetails.toBuilder()
            .data(caseData.toMap(objectMapper))
            .build();

        Map<String, List<String>> caseIdMap = Map.of("123", List.of("123"));
        when(hearingApiClient.getListedHearingsForAllCaseIdsOnCurrentDate(any(), any(), anyList()))
            .thenReturn(caseIdMap);

        StartAllTabsUpdateDataContent startContent = new StartAllTabsUpdateDataContent(
            s2sAuthToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(objectMapper),
            caseData,
            null
        );

        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(startContent);

        updateHearingActualsService.updateHearingActuals();

        verify(allTabService, times(2)).getStartUpdateForSpecificEvent(anyString(), anyString());
    }

    @Test
    void testUpdateHearingActualTaskForDraftOrderCreatedForHearingId() {
        setupCommonStubs();
        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();
        SearchResultResponse response = SearchResultResponse.builder()
            .total(1)
            .cases(List.of(caseDetails))
            .build();

        doReturn(response).when(objectMapper).convertValue(searchResult, SearchResultResponse.class);
        doReturn(caseData).when(objectMapper).convertValue(caseDetails.getData(), CaseData.class);
        when(coreCaseDataApi.searchCases(eq(authToken), eq(s2sAuthToken), eq(CASE_TYPE), isNull()))
            .thenReturn(searchResult);


        caseData = caseData.toBuilder()
            .draftOrderCollection(List.of(element(DraftOrder.builder()
                                                      .manageOrderHearingDetails(List.of(element(HearingData.builder()
                                                                                                     .confirmedHearingDates(
                                                                                                         DynamicList.builder()
                                                                                                             .value(
                                                                                                                 DynamicListElement.builder()
                                                                                                                     .code(
                                                                                                                         "123")
                                                                                                                     .build())
                                                                                                             .listItems(
                                                                                                                 List.of(
                                                                                                                     DynamicListElement.defaultListItem(
                                                                                                                         "test")))
                                                                                                             .build())
                                                                                                     .build())))
                                                      .build())))
            .build();

        caseDetails = caseDetails.toBuilder()
            .data(caseData.toMap(objectMapper))
            .build();

        Map<String, List<String>> caseIdMap = Map.of("123", List.of("123"));
        when(hearingApiClient.getListedHearingsForAllCaseIdsOnCurrentDate(any(), any(), anyList()))
            .thenReturn(caseIdMap);

        StartAllTabsUpdateDataContent startContent = new StartAllTabsUpdateDataContent(
            s2sAuthToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(objectMapper),
            caseData,
            null
        );
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), anyString())).thenReturn(startContent);

        updateHearingActualsService.updateHearingActuals();

        verify(allTabService, times(2)).getStartUpdateForSpecificEvent(anyString(), anyString());
    }


    @Test
    void testExtractSelectedHearingId_whenHearingDataIsNull_shouldReturnNull() {
        assertNull(UpdateHearingActualsService.extractSelectedHearingId(null));
    }

    @Test
    void testExtractSelectedHearingIdShouldReturnNullForNullHearingDates() {
        HearingData hearingData = HearingData.builder().confirmedHearingDates(null).build();
        assertNull(UpdateHearingActualsService.extractSelectedHearingId(hearingData));
    }

    @Test
    void testExtractSelectedHearingIdShouldReturnNullForNullDynamicListValue() {
        HearingData hearingData = HearingData.builder()
            .confirmedHearingDates(DynamicList.builder().value(null).build())
            .build();
        assertNull(UpdateHearingActualsService.extractSelectedHearingId(hearingData));
    }

    @Test
    void testExtractSelectedHearingIdShouldReturnCode() {
        HearingData hearingData = HearingData.builder()
            .confirmedHearingDates(DynamicList.builder()
                                       .value(DynamicListElement.builder().code("test-code").build())
                                       .build())
            .build();
        assertEquals("test-code", UpdateHearingActualsService.extractSelectedHearingId(hearingData));
    }
}
