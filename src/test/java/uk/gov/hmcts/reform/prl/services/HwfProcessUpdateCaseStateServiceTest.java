package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestReferenceStatusResponse;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.HWF_PROCESS_CASE_UPDATE;

@ExtendWith(MockitoExtension.class)
class HwfProcessUpdateCaseStateServiceTest {
    private static final String AUTH_TOKEN = "authToken";
    private static final String S2S_TOKEN = "s2sToken";

    private HwfProcessUpdateCaseStateService hwfProcessUpdateCaseStateService;
    private ObjectMapper objectMapper;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private AllTabServiceImpl allTabService;
    @Mock
    private PaymentRequestService paymentRequestService;

    @Captor
    private ArgumentCaptor<Map<String, Object>> caseDataUpdatedCaptor;

    @BeforeEach
    void setUp() {
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(authTokenGenerator.generate()).thenReturn(S2S_TOKEN);

        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        objectMapper = builder.createXmlMapper(false).build();

        hwfProcessUpdateCaseStateService = new HwfProcessUpdateCaseStateService(systemUserService, authTokenGenerator,
                                                                                coreCaseDataApi, paymentRequestService,
                                                                                allTabService, objectMapper);
    }

    @Test
    void givenNoCases_whenCheck_thenNoUpdates() {
        SearchResult searchResult = SearchResult.builder()
            .total(0)
            .build();
        mockSearchCases(searchResult);

        hwfProcessUpdateCaseStateService.checkHwfPaymentStatusAndUpdateCaseState();
        verifyNoInteractions(paymentRequestService);
        verifyNoInteractions(allTabService);
    }

    @Test
    void givenCaseWithNoHwfNumber_whenCheck_thenNoUpdate() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();

        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails(1234764578342789L, caseData)))
            .build();
        mockSearchCases(searchResult);

        hwfProcessUpdateCaseStateService.checkHwfPaymentStatusAndUpdateCaseState();
        verifyNoInteractions(paymentRequestService);
        verifyNoInteractions(allTabService);
    }

    @Test
    void givenMultiplePageResults_whenCheck_thenRetrievesAllCases() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        CaseDetails caseDetails = caseDetails(1234764578342789L, caseData);

        SearchResult searchResult1 = SearchResult.builder()
            .total(15)
            .cases(Collections.nCopies(10, caseDetails))
            .build();
        SearchResult searchResult2 = SearchResult.builder()
            .total(15)
            .cases(Collections.nCopies(5, caseDetails))
            .build();
        mockSearchCases(searchResult1, searchResult2);

        hwfProcessUpdateCaseStateService.checkHwfPaymentStatusAndUpdateCaseState();
        verifyNoInteractions(paymentRequestService);
        verifyNoInteractions(allTabService);
    }

    @Test
    void givenCaseWithHwfNumberAndNotPaid_whenCheck_thenNoUpdates() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .helpWithFeesNumber("HWF-12345")
            .paymentServiceRequestReferenceNumber("2025-1750002799132")
            .build();

        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails(1234764578342789L, caseData)))
            .build();
        mockSearchCases(searchResult);

        mockPaymentRequestService("2025-1750002799132", "PENDING");

        hwfProcessUpdateCaseStateService.checkHwfPaymentStatusAndUpdateCaseState();
        verifyNoInteractions(allTabService);
    }

    @Test
    void givenCaseWithNoSubmissionDate_whenCheck_thenUpdatesSubmissionDate() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .helpWithFeesNumber("HWF-12345")
            .paymentServiceRequestReferenceNumber("2025-1750002799132")
            .state(State.CASE_WITHDRAWN)
            .build();

        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails(1234764578342789L, caseData)))
            .build();
        mockSearchCases(searchResult);

        mockPaymentRequestService("2025-1750002799132", "Paid");
        mockAllTabService(1234764578342789L);

        hwfProcessUpdateCaseStateService.checkHwfPaymentStatusAndUpdateCaseState();
        verify(allTabService).submitAllTabsUpdate(
            anyString(),
            anyString(),
            any(),
            any(),
            caseDataUpdatedCaptor.capture()
        );
        Map<String, Object> caseDataUpdated = caseDataUpdatedCaptor.getValue();
        CaseStatus expectedCaseStatus = CaseStatus.builder()
            .state(State.SUBMITTED_PAID.getLabel())
            .build();
        assertThat(caseDataUpdated)
            .containsEntry("caseStatus", expectedCaseStatus)
            .containsKey("dateOfSubmission");
    }

    @ParameterizedTest
    @MethodSource("givenCaseWithHwfNumber_whenCheck_thenHandles")
    void givenCaseWithHwfNumber_whenCheck_thenHandles(State currentState, State expectedUpdatedState) {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .helpWithFeesNumber("HWF-12345")
            .paymentServiceRequestReferenceNumber("2025-1750002799132")
            .state(currentState)
            .dateSubmitted("2025-12-25")
            .build();

        SearchResult searchResult = SearchResult.builder()
            .total(1)
            .cases(List.of(caseDetails(1234764578342789L, caseData)))
            .build();
        mockSearchCases(searchResult);

        mockPaymentRequestService("2025-1750002799132", "Paid");
        mockAllTabService(1234764578342789L);

        hwfProcessUpdateCaseStateService.checkHwfPaymentStatusAndUpdateCaseState();
        verify(allTabService).submitAllTabsUpdate(
            anyString(),
            anyString(),
            any(),
            any(),
            caseDataUpdatedCaptor.capture()
        );
        Map<String, Object> caseDataUpdated = caseDataUpdatedCaptor.getValue();
        CaseStatus expectedCaseStatus = CaseStatus.builder()
            .state(expectedUpdatedState.getLabel())
            .build();
        assertThat(caseDataUpdated)
            .containsEntry("caseStatus", expectedCaseStatus)
            .doesNotContainKey("dateOfSubmission");
    }

    private static Stream<Arguments> givenCaseWithHwfNumber_whenCheck_thenHandles() {
        return Stream.of(State.values())
            .map(state -> {
                State expected = (state == State.CASE_WITHDRAWN || state == State.SUBMITTED_NOT_PAID)
                    ? State.SUBMITTED_PAID
                    : state;
                return Arguments.of(state, expected);
            });
    }

    @Test
    void givenMultipleCases_whenCheck_thenHandlesAllCases() {
        CaseData caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .helpWithFeesNumber("HWF-12345")
            .paymentServiceRequestReferenceNumber("ref1")
            .state(State.CASE_WITHDRAWN)
            .build();
        CaseData caseData2 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .helpWithFeesNumber("HWF-12345")
            .paymentServiceRequestReferenceNumber("ref2")
            .state(State.SUBMITTED_NOT_PAID)
            .build();

        SearchResult searchResult = SearchResult.builder()
            .total(2)
            .cases(List.of(
                caseDetails(1234764578342789L, caseData1),
                caseDetails(1234764578342790L, caseData2))
            )
            .build();
        mockSearchCases(searchResult);

        mockPaymentRequestService("ref1", "Paid");
        mockPaymentRequestService("ref2", "Paid");
        mockAllTabService(1234764578342789L);
        mockAllTabService(1234764578342790L);

        hwfProcessUpdateCaseStateService.checkHwfPaymentStatusAndUpdateCaseState();

        verify(allTabService, times(2)).submitAllTabsUpdate(
            anyString(),
            anyString(),
            any(),
            any(),
            caseDataUpdatedCaptor.capture()
        );
        List<Map<String, Object>> capturedCaseDataList = caseDataUpdatedCaptor.getAllValues();
        assertThat(capturedCaseDataList).hasSize(2);

        Map<String, Object> caseDataUpdated1 = capturedCaseDataList.get(0);
        Map<String, Object> caseDataUpdated2 = capturedCaseDataList.get(1);
        CaseStatus expectedCaseStatus = CaseStatus.builder()
            .state(State.SUBMITTED_PAID.getLabel())
            .build();
        assertThat(caseDataUpdated1)
            .containsEntry("caseStatus", expectedCaseStatus)
            .containsKey("dateOfSubmission");
        assertThat(caseDataUpdated2)
            .containsEntry("caseStatus", expectedCaseStatus)
            .containsKey("dateOfSubmission");
    }

    private void mockPaymentRequestService(String serviceRequestReferenceNumber, String serviceRequestStatus) {
        ServiceRequestReferenceStatusResponse response = ServiceRequestReferenceStatusResponse.builder()
            .serviceRequestStatus(serviceRequestStatus)
            .build();

        when(paymentRequestService.fetchServiceRequestReferenceStatus(AUTH_TOKEN, serviceRequestReferenceNumber))
            .thenReturn(response);
    }

    private void mockAllTabService(long caseId) {
        EventRequestData eventRequestData = mock(EventRequestData.class);
        StartEventResponse startEventResponse = mock(StartEventResponse.class);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = mock(StartAllTabsUpdateDataContent.class);
        when(startAllTabsUpdateDataContent.authorisation()).thenReturn("auth");
        when(startAllTabsUpdateDataContent.eventRequestData()).thenReturn(eventRequestData);
        when(startAllTabsUpdateDataContent.startEventResponse()).thenReturn(startEventResponse);
        when(allTabService.getStartUpdateForSpecificEvent(
            String.valueOf(caseId),
            HWF_PROCESS_CASE_UPDATE.getValue())
        ).thenReturn(startAllTabsUpdateDataContent);
    }

    private void mockSearchCases(SearchResult searchResult) {
        when(coreCaseDataApi.searchCases(
            eq(AUTH_TOKEN),
            eq(S2S_TOKEN),
            eq(CASE_TYPE),
            anyString()
        )).thenReturn(searchResult);
    }

    private void mockSearchCases(SearchResult searchResult1, SearchResult searchResult2) {
        when(coreCaseDataApi.searchCases(
            eq(AUTH_TOKEN),
            eq(S2S_TOKEN),
            eq(CASE_TYPE),
            anyString()
        )).thenReturn(searchResult1).thenReturn(searchResult2);
    }

    private CaseDetails caseDetails(long caseId, CaseData caseData) {
        return CaseDetails.builder()
            .id(caseId)
            .data(objectMapper.convertValue(caseData, Map.class))
            .build();
    }
}
