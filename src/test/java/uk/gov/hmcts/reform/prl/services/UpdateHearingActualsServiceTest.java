package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
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

        List<Hearings> hearings = List.of(Hearings.hearingsWith()
                                              .caseRef("123")
                                              .caseHearings(List.of(CaseHearing.caseHearingWith()
                                                                        .hmcStatus("LISTED")
                                                                        .hearingID(123L)
                                                                        .hearingDaySchedule(List.of(HearingDaySchedule.hearingDayScheduleWith()
                                                                                                        .hearingStartDateTime(
                                                                                                            LocalDateTime.now())
                                                                                                        .build()))
                                                                        .build()))
                                              .build());

        when(hearingApiClient.getHearingsForAllCaseIdsWithCourtVenue(any(), any(), anyList())).thenReturn(hearings);
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
                                                                                                            "123").build()).build())
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
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent1 = new StartAllTabsUpdateDataContent(
            s2sAuthToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(
                objectMapper),
            caseData,
            null
        );
        when(allTabService.getStartAllTabsUpdate(Mockito.anyString())).thenReturn(startAllTabsUpdateDataContent1);
        updateHearingActualsService.updateHearingActuals();
        verify(allTabService, times(1)).getStartAllTabsUpdate(
            Mockito.any()
        );
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

        verifyNoInteractions(allTabService);
    }
}
