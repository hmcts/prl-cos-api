package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestReferenceStatusResponse;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class HwfProcessUpdateCaseStateServiceTest {

    private final String authToken = "authToken";
    private final String s2sAuthToken = "s2sAuthToken";
    private CaseDetails caseDetails;
    private CaseData caseData;


    @InjectMocks
    private HwfProcessUpdateCaseStateService hwfProcessUpdateCaseStateService;

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
    PaymentRequestService paymentRequestService;


    @Before
    public void setUp() {
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn(s2sAuthToken);


        caseData = CaseData.builder()
            .id(123L)
            .state(State.SUBMITTED_NOT_PAID)
            .helpWithFeesNumber("HWF-1BC-AF")
            .paymentServiceRequestReferenceNumber("2024-1709204678984")
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


        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(s2sAuthToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        caseData.toMap(objectMapper),
                                                                                                        caseData, null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), any())).thenReturn(CaseDetails.builder().build());
        when(paymentRequestService.fetchServiceRequestReferenceStatus(anyString(), anyString())).thenReturn(
            ServiceRequestReferenceStatusResponse.builder().serviceRequestStatus("Paid").build());
    }

    @Test
    public void testCheckHwfPaymentStatusAndUpdateCaseState() {

        hwfProcessUpdateCaseStateService.checkHwfPaymentStatusAndUpdateCaseState();
        verify(paymentRequestService, times(1))
            .fetchServiceRequestReferenceStatus(anyString(), anyString());

    }

    @Test
    public void testCheckHwfPaymentStatusAndUpdateCaseStateWithoutServiceRequest() {

        caseData = CaseData.builder()
            .id(123L)
            .state(State.SUBMITTED_NOT_PAID)
            .helpWithFeesNumber("HWF-1BC-AF")
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
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class)).thenReturn(null);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        hwfProcessUpdateCaseStateService.checkHwfPaymentStatusAndUpdateCaseState();

        verify(paymentRequestService, times(0))
            .fetchServiceRequestReferenceStatus(anyString(), anyString());

    }

    @Test
    public void testCheckHwfPaymentStatusAndUpdateCaseStateWithEmptyList() {
        SearchResult searchResult = SearchResult.builder()
            .total(0)
            .build();
        when(coreCaseDataApi.searchCases(authToken, s2sAuthToken, CASE_TYPE, null)).thenReturn(searchResult);
        when(objectMapper.convertValue(searchResult, SearchResultResponse.class)).thenReturn(null);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        hwfProcessUpdateCaseStateService.checkHwfPaymentStatusAndUpdateCaseState();

        //verify
        verify(paymentRequestService, times(0))
            .fetchServiceRequestReferenceStatus(anyString(), anyString());
    }


}
