package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Payment;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CcdPaymentServiceRequestUpdate;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.UploadAdditionalApplicationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RequestUpdateCallbackServiceTest {

    private final Long caseId = 1234567887654321L;
    private final String eventName = "paymentSuccessCallback";
    public static final String authToken = "Bearer TestAuthToken";
    private final String serviceAuthToken = "Bearer testServiceAuth";
    private final String systemUserId = "systemUserID";
    private final String eventToken = "eventToken";

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    private ServiceRequestUpdateDto serviceRequestUpdateDto;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private SolicitorEmailService solicitorEmailService;

    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;

    @Mock
    private ConfidentialityTabService confidentialityTabService;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    private Court court;

    @Mock
    CcdCoreCaseDataService coreCaseDataService;

    @Mock
    private CourtFinderService courtFinderService;

    @Mock
    private PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    @InjectMocks
    RequestUpdateCallbackService requestUpdateCallbackService;

    @Mock
    UploadAdditionalApplicationUtils uploadAdditionalApplicationUtils;

    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(systemUserService.getUserId(authToken)).thenReturn(systemUserId);
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
        when(coreCaseDataService.eventRequest(CaseEvent.PAYMENT_SUCCESS_CALLBACK, systemUserId)).thenReturn(
            EventRequestData.builder().build());
        caseDetails = CaseDetails.builder().id(Long.valueOf("123")).data(Map.of("id", 1)).build();
        StartEventResponse startEventResponse = StartEventResponse.builder().eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken).build();
        when(coreCaseDataService.findCaseById(Mockito.anyString(), Mockito.anyString())).thenReturn(caseDetails);
        when(coreCaseDataService.startUpdate(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyBoolean()))
            .thenReturn(startEventResponse);
        when(partyLevelCaseFlagsService.generateC100AllPartyCaseFlags(any(), any())).thenCallRealMethod();
    }

    @Test
    void shouldSkipProcessingWhenDuplicatePaymentDetected() {
        // Given: CaseData already has 'Paid' status
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("test-ref")
            .payment(PaymentDto.builder().paymentReference("pay-ref").build())
            .build();

        CaseData duplicateCaseData = CaseData.builder()
            .paymentServiceRequestReferenceNumber("test-ref")
            .paymentCallbackServiceRequestUpdate(CcdPaymentServiceRequestUpdate.builder()
                                                     .serviceRequestStatus("Paid")
                                                     .build())
            .build();

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(duplicateCaseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        verify(coreCaseDataService, never()).startUpdate(anyString(), any(), anyString(), anyBoolean());
    }

    @Test
    void shouldStartAndSubmitEventWithCaseDetails() {
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentMethod("cash")
                         .paymentReference("reference")
                         .caseReference("reference")
                         .accountNumber("123445555")
                         .build())
            .build();

        assertThrows(NullPointerException.class, () -> {
            requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);
        });

        verifyNoInteractions(coreCaseDataApi);
    }

    @Test
    void shouldNotStartOrSubmitEventWithoutCaseDetails() {
        when(coreCaseDataApi.getCase(authToken, serviceAuthToken, "123")).thenReturn(caseDetails);

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber("123")
            .serviceRequestStatus("Paid")
            .build();

        assertThrows(NullPointerException.class, () ->
            requestUpdateCallbackService.processCallback(serviceRequestUpdateDto)
        );

        verify(coreCaseDataService, never()).startUpdate(anyString(), any(), anyString(), anyBoolean());
        verify(coreCaseDataApi, never()).submitEventForCaseWorker(any(), any(), any(), any(), any(), any(), anyBoolean(), any());
    }

    @Test
    void shouldProcessCallbackNotPaid() {
        CaseData caseData = CaseData.builder()
            .id(1L)
            .paymentServiceRequestReferenceNumber("test-reference")
            .state(State.SUBMITTED_NOT_PAID)
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("test-reference")
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentMethod("cash")
                         .paymentReference("reference")
                         .caseReference("reference")
                         .accountNumber("123445555")
                         .build())
            .serviceRequestStatus("Not Paid")
            .build();

        when(coreCaseDataService.findCaseById(anyString(), eq(caseId.toString()))).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        verify(coreCaseDataService).findCaseById(anyString(), eq(caseId.toString()));

        verify(coreCaseDataService, times(2)).startUpdate(anyString(), any(), eq(caseId.toString()), eq(true));
        verify(coreCaseDataService).submitUpdate(anyString(), any(), any(), eq(caseId.toString()), eq(true));

        verify(allTabService).mapAndSubmitAllTabsUpdate(any(), any(), any(), any(), any());

        verifyNoInteractions(solicitorEmailService);
        verifyNoInteractions(caseWorkerEmailService);
    }

    @Test
    void shouldProcessPendingCallback() {
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder()
                                                     .payment(Payment.builder()
                                                                  .paymentServiceRequestReferenceNumber("Paid")
                                                                  .build())
                                                     .build()));

        CaseData caseData = CaseData.builder()
            .id(1L)
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .paymentServiceRequestReferenceNumber("different-reference")
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("Paid")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentReference("reference")
                         .build())
            .build();

        when(coreCaseDataService.findCaseById(anyString(), eq(caseId.toString()))).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        verify(coreCaseDataService, times(1)).startUpdate(anyString(), any(), eq(caseId.toString()), eq(true));
        verify(coreCaseDataService, times(1)).submitUpdate(anyString(), any(), any(), eq(caseId.toString()), eq(true));

        verifyNoInteractions(allTabService);
        verifyNoInteractions(solicitorEmailService);
        verifyNoInteractions(caseWorkerEmailService);
        verifyNoInteractions(partyLevelCaseFlagsService);
    }

    @Test
    void shouldProcessCallbackCasePaymentSuccess() {
        CaseData caseData = CaseData.builder()
            .id(1L)
            .paymentServiceRequestReferenceNumber("matching-case-reference")
            .state(State.SUBMITTED_NOT_PAID)
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("matching-case-reference") // Forces isCasePayment = true
            .serviceRequestStatus("Paid")                       // Forces isStatusPaid = true
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentReference("reference")
                         .build())
            .build();

        when(coreCaseDataService.findCaseById(anyString(), eq(caseId.toString()))).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        // Verify it triggers the standard case payment success callback event
        verify(coreCaseDataService, times(2)).startUpdate(anyString(), any(), eq(caseId.toString()), eq(true));
    }

    @Test
    void shouldProcessCallbackAwpPaymentFailure() {
        CaseData caseData = CaseData.builder()
            .id(1L)
            .paymentServiceRequestReferenceNumber("initial-case-reference") // Mismatch forces isCasePayment = false
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("completely-different-awp-reference")
            .serviceRequestStatus("Failed")                                 // Forces isStatusPaid = false
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentReference("reference")
                         .build())
            .build();

        when(coreCaseDataService.findCaseById(anyString(), eq(caseId.toString()))).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        // Verify it triggers the AWP failure event path
        verify(coreCaseDataService).startUpdate(anyString(), any(), eq(caseId.toString()), eq(true));
    }

    @Test
    void shouldHandleCourtFinderExceptionAndContinueProcessing() throws NotFoundException {
        CaseData caseData = CaseData.builder()
            .id(1L)
            .paymentServiceRequestReferenceNumber("test-reference")
            .state(State.SUBMITTED_NOT_PAID)
            .paymentCallbackServiceRequestUpdate(null)
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("test-reference")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentReference("reference")
                         .build())
            .build();

        when(coreCaseDataService.findCaseById(anyString(), eq(caseId.toString()))).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        when(courtFinderService.getNearestFamilyCourt(any(CaseData.class)))
            .thenThrow(new NotFoundException("Postcode not recognized"));

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        verify(coreCaseDataService).findCaseById(anyString(), eq(caseId.toString()));

        verify(coreCaseDataService, times(2)).startUpdate(anyString(), any(), eq(caseId.toString()), eq(true));
        verify(coreCaseDataService, times(1)).submitUpdate(anyString(), any(), any(), eq(caseId.toString()), eq(true));

        verify(allTabService).mapAndSubmitAllTabsUpdate(any(), any(), any(), any(), any());

        verify(solicitorEmailService).sendEmail(any());
        verify(caseWorkerEmailService).sendEmail(any());
    }

    @Test
    void shouldUpdateCaseStateToSubmittedWhenCurrentCaseStateIsPendingWhenPaymentIsSuccessful() {
        CaseData caseData = CaseData.builder()
            .id(1L)
            .state(State.SUBMITTED_NOT_PAID)
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("test-reference")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentReference("reference")
                         .build())
            .build();

        CaseData updatedCaseData =
            requestUpdateCallbackService.getCaseDataWithStateAndDateSubmitted(serviceRequestUpdateDto, caseData);

        assertEquals(State.SUBMITTED_PAID, updatedCaseData.getState(),
                     "Case state should transition to SUBMITTED_PAID when payment is successful");

        assertNotNull(updatedCaseData.getDateSubmitted(), "Date submitted should be populated upon successful payment");
    }

    @Test
    void shouldUpdateCaseStateToSubmittedWhenCurrentCaseStateIsWithdrawnWhenPaymentIsSuccessful() {
        CaseData caseData = CaseData.builder()
            .id(1L)
            .state(State.CASE_WITHDRAWN)
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("test-reference")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentReference("RC-REFERENCE")
                         .build())
            .build();

        CaseData updatedCaseData =
            requestUpdateCallbackService.getCaseDataWithStateAndDateSubmitted(serviceRequestUpdateDto, caseData);

        assertEquals(State.SUBMITTED_PAID, updatedCaseData.getState(),
                     "Case state should move from WITHDRAWN to SUBMITTED_PAID upon successful payment");

        assertNotNull(updatedCaseData.getDateSubmitted(),
                      "Date submitted should be set even when coming from a withdrawn state");
    }

    @Test
    void shouldNotUpdateCaseStateToSubmittedWhenCurrentCaseStateIsCaseIssuedWhenPaymentIsSuccessful() {
        CaseData caseData = CaseData.builder()
            .id(1L)
            .state(State.CASE_ISSUED)
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("test-reference")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentReference("reference")
                         .build())
            .build();

        CaseData updatedCaseData =
            requestUpdateCallbackService.getCaseDataWithStateAndDateSubmitted(serviceRequestUpdateDto, caseData);

        assertEquals(State.CASE_ISSUED, updatedCaseData.getState(),
                     "State should NOT change back to SUBMITTED_PAID if the case is already ISSUED");
    }
}
