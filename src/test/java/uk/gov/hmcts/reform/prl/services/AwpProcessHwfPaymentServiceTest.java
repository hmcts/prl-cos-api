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
import uk.gov.hmcts.reform.prl.clients.PaymentApi;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.ApplicationStatus;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.PaymentStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.SearchResultResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Payment;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestReferenceStatusResponse;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class AwpProcessHwfPaymentServiceTest {

    private final String authToken = "authToken";
    private final String s2sAuthToken = "s2sAuthToken";
    private CaseDetails caseDetails;
    private CaseData caseData;


    @InjectMocks
    private AwpProcessHwfPaymentService awpProcessHwfPaymentService;

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

    @Mock
    private PaymentApi paymentApi;


    @Before
    public void setUp() {
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
        when(authTokenGenerator.generate()).thenReturn(s2sAuthToken);

        AdditionalApplicationsBundle additionalApplicationsBundle1 = AdditionalApplicationsBundle.builder()
            .payment(Payment.builder().status(PaymentStatus.HWF.getDisplayedValue())
                         .paymentServiceRequestReferenceNumber("2024-12222222")
                         .build())
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                         .applicationStatus(ApplicationStatus.PENDING_ON_PAYMENT.getDisplayedValue())
                                         .build())
            .c2DocumentBundle(C2DocumentBundle.builder()
                                  .applicationStatus(ApplicationStatus.PENDING_ON_PAYMENT.getDisplayedValue())
                                  .build())
            .build();

        AdditionalApplicationsBundle additionalApplicationsBundle2 = AdditionalApplicationsBundle.builder()
            .payment(Payment.builder().status(PaymentStatus.PAID.getDisplayedValue()).build())
            .otherApplicationsBundle(OtherApplicationsBundle.builder().applicationStatus(ApplicationStatus.SUBMITTED.getDisplayedValue()).build())
            .build();

        AdditionalApplicationsBundle additionalApplicationsBundle3 = AdditionalApplicationsBundle.builder()
            .payment(Payment.builder().status(PaymentStatus.PAID.getDisplayedValue()).build())
            .c2DocumentBundle(C2DocumentBundle.builder().applicationStatus(ApplicationStatus.SUBMITTED.getDisplayedValue()).build())
            .build();

        AdditionalApplicationsBundle additionalApplicationsBundle4 = AdditionalApplicationsBundle.builder()
            .payment(Payment.builder().status(PaymentStatus.HWF.getDisplayedValue())
                         .paymentServiceRequestReferenceNumber("2024-12222223")
                         .build())
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                         .applicationStatus(ApplicationStatus.PENDING_ON_PAYMENT.getDisplayedValue())
                                         .build())
            .build();

        AdditionalApplicationsBundle additionalApplicationsBundle5 = AdditionalApplicationsBundle.builder()
            .payment(Payment.builder().status(PaymentStatus.HWF.getDisplayedValue())
                         .paymentServiceRequestReferenceNumber("2024-12222224")
                         .build())
            .c2DocumentBundle(C2DocumentBundle.builder().applicationStatus(ApplicationStatus.PENDING_ON_PAYMENT.getDisplayedValue()).build())
            .build();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(additionalApplicationsBundle1));
        additionalApplicationsBundle.add(element(additionalApplicationsBundle2));
        additionalApplicationsBundle.add(element(additionalApplicationsBundle3));
        additionalApplicationsBundle.add(element(additionalApplicationsBundle4));
        additionalApplicationsBundle.add(element(additionalApplicationsBundle5));


        caseData = CaseData.builder()
            .id(123L)
            .additionalApplicationsBundle(additionalApplicationsBundle)
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
                                                                                                        caseData.toMap(
                                                                                                            objectMapper),
                                                                                                        caseData, null
        );
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitAllTabsUpdate(
            anyString(),
            anyString(),
            any(),
            any(),
            any()
        )).thenReturn(CaseDetails.builder().build());

        ServiceRequestReferenceStatusResponse serviceRequestReferenceStatusResponse = ServiceRequestReferenceStatusResponse.builder()
            .serviceRequestStatus("Paid")
            .build();
        when(paymentRequestService.fetchServiceRequestReferenceStatus(anyString(), anyString()))
            .thenReturn(serviceRequestReferenceStatusResponse);
    }

    @Test
    public void testCheckHwfPaymentStatusAndUpdateApplicationStatus() {

        awpProcessHwfPaymentService.checkHwfPaymentStatusAndUpdateApplicationStatus();
        verify(paymentRequestService, times(3))
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

        awpProcessHwfPaymentService.checkHwfPaymentStatusAndUpdateApplicationStatus();

        //verify
        verify(paymentRequestService, times(0))
            .fetchServiceRequestReferenceStatus(anyString(), anyString());
    }

}
