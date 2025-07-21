package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
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
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.DateOfSubmission;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestReferenceStatusResponse;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_OF_SUBMISSION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_SUBMITTED_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EUROPE_LONDON_TIME_ZONE;

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

    @Captor
    private ArgumentCaptor<Map<String, Object>> caseDataUpdatedCaptor;

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
        when(allTabService.submitAllTabsUpdate(anyString(), anyString(), any(), any(), caseDataUpdatedCaptor.capture())).thenReturn(caseDetails);
        when(paymentRequestService.fetchServiceRequestReferenceStatus(anyString(), anyString())).thenReturn(
            ServiceRequestReferenceStatusResponse.builder().serviceRequestStatus("Paid").build());
    }

    @Test
    public void testCheckHwfPaymentStatusAndUpdateCaseState() {

        hwfProcessUpdateCaseStateService.checkHwfPaymentStatusAndUpdateCaseState();
        verify(paymentRequestService, times(1))
            .fetchServiceRequestReferenceStatus(anyString(), anyString());
        verify(allTabService).getStartUpdateForSpecificEvent(any(), any());
        verify(allTabService).submitAllTabsUpdate(anyString(), anyString(), any(), any(), caseDataUpdatedCaptor.capture());
        Map<String, Object> caseUpdated = caseDataUpdatedCaptor.getValue();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime), caseUpdated.get(DATE_SUBMITTED_FIELD));
        assertEquals(DateOfSubmission.builder().dateOfSubmission(CommonUtils.getIsoDateToSpecificFormat(
            DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime),
            CommonUtils.DATE_OF_SUBMISSION_FORMAT).replace("-", " ")).build(), caseUpdated.get(DATE_OF_SUBMISSION));

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
        verifyNoInteractions(allTabService);
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
        verifyNoInteractions(allTabService);
    }

    @Test
    public void shouldProcessMultiplePagesWhenRequired() {
        SearchResult page1 = SearchResult.builder()
            .total(15)
            .cases(Collections.nCopies(10, caseDetails))
            .build();
        SearchResult page2 = SearchResult.builder()
            .total(15)
            .cases(Collections.nCopies(5, caseDetails))
            .build();

        try {
            when(objectMapper.writeValueAsString(argThat(
            (ArgumentMatcher<QueryParam>) argument -> isNotEmpty(argument) && argument.getFrom().equals("10"))))
            .thenReturn("{\"from\" : \"10\"}");

            when(objectMapper.writeValueAsString(argThat(
                (ArgumentMatcher<QueryParam>) argument -> isNotEmpty(argument) && argument.getFrom().equals("0"))))
                .thenReturn("{\"from\" : \"0\"}");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        when(coreCaseDataApi.searchCases(eq(authToken), eq(s2sAuthToken), eq(CASE_TYPE), contains("\"from\" : \"0\""))).thenReturn(page1);
        when(coreCaseDataApi.searchCases(eq(authToken), eq(s2sAuthToken), eq(CASE_TYPE), contains("\"from\" : \"10\""))).thenReturn(page2);
        when(objectMapper.convertValue(page1, SearchResultResponse.class)).thenReturn(
            SearchResultResponse.builder()
                .total(15)
                .cases(Collections.nCopies(10, caseDetails))
                .build()
        );
        when(objectMapper.convertValue(page2, SearchResultResponse.class)).thenReturn(
            SearchResultResponse.builder()
                .total(15)
                .cases(Collections.nCopies(5, caseDetails))
                .build()
        );

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        hwfProcessUpdateCaseStateService.checkHwfPaymentStatusAndUpdateCaseState();

        verify(paymentRequestService, times(15))
            .fetchServiceRequestReferenceStatus(anyString(), anyString());

        verify(allTabService, times(15)).getStartUpdateForSpecificEvent(any(), any());
    }
}
