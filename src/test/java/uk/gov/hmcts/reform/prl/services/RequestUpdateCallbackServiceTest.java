package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.AdditionalApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Payment;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CcdPaymentServiceRequestUpdate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UploadAdditionalApplicationData;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.UploadAdditionalApplicationUtils;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
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
    public static final String AUTH_TOKEN = "Bearer TestAuthToken";

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
    AllTabServiceImpl allTabService;

    @Mock
    private Court court;

    @Mock
    private UploadAdditionalApplicationUtils uploadAdditionalApplicationUtils;

    @Mock
    CcdCoreCaseDataService coreCaseDataService;

    @Mock
    private CourtFinderService courtFinderService;

    @Mock
    private PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    @InjectMocks
    RequestUpdateCallbackService requestUpdateCallbackService;

    private CaseDetails caseDetails;
    private StartEventResponse startEventResponse;

    @BeforeEach
    void setUp() {
        String systemUserId = "systemUserID";
        when(systemUserService.getUserId(AUTH_TOKEN)).thenReturn(systemUserId);
        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);

        caseDetails = CaseDetails.builder().id(caseId).data(Map.of("id", 1)).build();
        String eventName = "paymentSuccessCallback";
        String eventToken = "eventToken";
        startEventResponse = StartEventResponse.builder()
            .eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken)
            .build();

        when(uploadAdditionalApplicationUtils.getAwPTaskNameWhenPaymentCompleted(any()))
            .thenReturn("AWP Task");
        when(coreCaseDataService.findCaseById(anyString(), anyString())).thenReturn(caseDetails);
        when(coreCaseDataService.eventRequest(
            any(CaseEvent.class),
            anyString()
        )).thenReturn(EventRequestData.builder().build());
        when(coreCaseDataService.startUpdate(anyString(), any(), anyString(), anyBoolean())).thenReturn(
            startEventResponse);
        when(coreCaseDataService.createCaseDataContent(any(), any())).thenReturn(CaseDataContent.builder().build());
    }

    @Test
    void shouldSkipProcessingWhenDuplicatePaymentDetected() {
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("test-ref")
            .payment(PaymentDto.builder().paymentReference("pay-ref").build())
            .serviceRequestStatus("Paid")
            .build();

        CaseData duplicateCaseData = CaseData.builder()
            .paymentServiceRequestReferenceNumber("test-ref")
            .paymentCallbackServiceRequestUpdate(CcdPaymentServiceRequestUpdate.builder()
                                                     .serviceRequestReference("test-ref")
                                                     .serviceRequestStatus("Paid")
                                                     .build())
            .build();

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(duplicateCaseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        verify(coreCaseDataService, never()).startUpdate(anyString(), any(), anyString(), anyBoolean());
    }

    @Test
    void shouldStartAndSubmitEventWithCaseDetails() {
        CaseData caseData = CaseData.builder()
            .id(caseId)
            .paymentServiceRequestReferenceNumber("2026-1750003526111")
            .state(State.SUBMITTED_NOT_PAID)
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("2026-1750003526111")
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentMethod("cash")
                         .paymentReference("reference")
                         .caseReference("reference")
                         .accountNumber("123445555")
                         .build())
            .build();

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        verify(coreCaseDataService).submitUpdate(anyString(), any(), any(), eq(caseId.toString()), eq(true));
    }

    @Test
    void shouldNotStartOrSubmitEventWithoutCaseDetails() {
        when(coreCaseDataService.findCaseById(anyString(), eq("123"))).thenReturn(null);

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber("123")
            .serviceRequestStatus("Paid")
            .build();

        assertThrows(
            NullPointerException.class, () ->
                requestUpdateCallbackService.processCallback(serviceRequestUpdateDto)
        );

        verify(coreCaseDataService, never()).startUpdate(anyString(), any(), anyString(), anyBoolean());
        verify(coreCaseDataService, never()).submitUpdate(anyString(), any(), any(), anyString(), anyBoolean());
    }

    @Test
    void shouldProcessCallbackNotPaid() {
        CaseData caseData = CaseData.builder()
            .id(caseId)
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

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        verify(coreCaseDataService).findCaseById(anyString(), eq(caseId.toString()));
        verify(coreCaseDataService, times(2)).startUpdate(anyString(), any(), eq(caseId.toString()), eq(true));
        verify(coreCaseDataService).submitUpdate(anyString(), any(), any(), eq(caseId.toString()), eq(true));
        verify(allTabService).mapAndSubmitAllTabsUpdate(any(), any(), any(), any(), any());

        verifyNoInteractions(solicitorEmailService);
        verifyNoInteractions(caseWorkerEmailService);
    }

    @Test
    void shouldProcessPendingCallback() throws NotFoundException {
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder()
                                                     .payment(Payment.builder()
                                                                  .paymentServiceRequestReferenceNumber("Paid")
                                                                  .build())
                                                     .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                  .applicationStatus("Submitted")
                                                                                  .build())
                                                     .c2DocumentBundle(C2DocumentBundle.builder()
                                                                           .applicationStatus("Submitted")
                                                                           .build())
                                                     .build()));

        UploadAdditionalApplicationData uploadAdditionalApplicationData =
            UploadAdditionalApplicationData.builder()
                .additionalApplicationsApplyingFor(AdditionalApplicationTypeEnum.otherOrder)
                .temporaryOtherApplicationsBundle(OtherApplicationsBundle.builder()
                                                      .urgencyTimeFrameType(UrgencyTimeFrameType.WITHIN_2_DAYS)
                                                      .build())
                .additionalApplicationFeesToPay("£232.00")
                .build();

        CaseData caseData = CaseData.builder()
            .id(caseId)
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .paymentServiceRequestReferenceNumber("test-reference") // Intentionally mismatched to force AWP logic path
            .uploadAdditionalApplicationData(uploadAdditionalApplicationData)
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("test-ref")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentMethod("cash")
                         .paymentReference("reference")
                         .caseReference("reference")
                         .accountNumber("123445555")
                         .build())
            .build();

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        when(courtFinderService.getNearestFamilyCourt(any(CaseData.class))).thenReturn(court);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        verify(coreCaseDataService, times(1)).findCaseById(anyString(), eq(caseId.toString()));
        verify(coreCaseDataService, times(1)).startUpdate(anyString(), any(), eq(caseId.toString()), eq(true));
        verify(coreCaseDataService, times(1)).submitUpdate(anyString(), any(), any(), eq(caseId.toString()), eq(true));
    }

    @Test
    void shouldSkipProcessingWhenDuplicateAwpPaymentDetected() {
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(
            AdditionalApplicationsBundle.builder()
                .payment(Payment.builder()
                             .paymentServiceRequestReferenceNumber("12345")
                             .status("Paid")
                             .build())
                .build()
        ));

        CaseData caseData = CaseData.builder()
            .id(caseId)
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("12345")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder().paymentReference("ref").build())
            .build();

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        // Because it is a duplicate (existing inner payment already Paid), we should not call startUpdate
        verify(coreCaseDataService, never()).startUpdate(anyString(), any(), anyString(), anyBoolean());
    }

    @Test
    void shouldPopulateAwPCaseDataMapWhenBundleElementMatchesAndPaid() {
        UUID elementId = UUID.randomUUID();
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(
            elementId,
            AdditionalApplicationsBundle.builder()
                .payment(Payment.builder()
                             .paymentServiceRequestReferenceNumber("match-ref")
                             .status("Not paid")
                             .build())
                .build()
        ));

        CaseData startEventCaseData = CaseData.builder()
            .id(caseId)
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("match-ref")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder().paymentReference("p-ref").build())
            .build();

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(startEventCaseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        verify(coreCaseDataService).createCaseDataContent(
            eq(startEventResponse),
            argThat(arg -> {
                if (!(arg instanceof Map<?, ?> m)) {
                    return false;
                }
                return m.containsKey("additionalApplicationsBundle")
                    && m.containsKey("additionalApplicationsBundleId")
                    && "AWP Task".equals(m.get("awpWaTaskName"))
                    && "Yes".equals(m.get("awpWaTaskToBeCreated"));
            })
        );
    }


    @Test
    void shouldProcessCallbackCasePaymentSuccess() {
        CaseData caseData = CaseData.builder()
            .id(caseId)
            .paymentServiceRequestReferenceNumber("matching-case-reference")
            .state(State.SUBMITTED_NOT_PAID)
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("matching-case-reference") // Forces isCasePayment = true
            .serviceRequestStatus("Paid")                       // Forces isStatusPaid = true
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentReference("matching-case-reference")
                         .build())
            .build();

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        ArgumentCaptor<CaseEvent> eventCaptor = ArgumentCaptor.forClass(CaseEvent.class);
        verify(coreCaseDataService, atLeastOnce()).eventRequest(eventCaptor.capture(), anyString());
        List<CaseEvent> capturedEvents = eventCaptor.getAllValues();
        assertTrue(capturedEvents.stream().anyMatch(e -> e == CaseEvent.PAYMENT_SUCCESS_CALLBACK));
    }

    @Test
    void shouldSelectCaseEventForCasePaymentFailure() {
        // Case: isCasePayment = true and status = "Failed"
        CaseData caseData = CaseData.builder()
            .id(caseId)
            .paymentServiceRequestReferenceNumber("case-ref")
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("case-ref")
            .serviceRequestStatus("Failed")
            .payment(PaymentDto.builder().paymentReference("ref").paymentAmount("1").build())
            .build();

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        ArgumentCaptor<CaseEvent> eventCaptor = ArgumentCaptor.forClass(CaseEvent.class);
        verify(coreCaseDataService, atLeastOnce()).eventRequest(eventCaptor.capture(), anyString());
        List<CaseEvent> capturedEvents = eventCaptor.getAllValues();
        assertTrue(capturedEvents.stream().anyMatch(e -> e == CaseEvent.PAYMENT_FAILURE_CALLBACK));
    }

    @Test
    void shouldSelectCaseEventForAwpPaymentSuccess() {
        CaseData awpCaseData = CaseData.builder()
            .id(caseId)
            .paymentServiceRequestReferenceNumber("other-ref")
            .additionalApplicationsBundle(new ArrayList<>())
            .build();

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("awp-ref")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder().paymentReference("p-ref").build())
            .build();

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(awpCaseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        ArgumentCaptor<CaseEvent> eventCaptor = ArgumentCaptor.forClass(CaseEvent.class);
        verify(coreCaseDataService, atLeastOnce()).eventRequest(eventCaptor.capture(), anyString());
        List<CaseEvent> capturedEvents = eventCaptor.getAllValues();
        assertTrue(capturedEvents.stream().anyMatch(e -> e == CaseEvent.AWP_PAYMENT_SUCCESS_CALLBACK));
    }

    @Test
    void shouldSkipProcessingWhenPaymentObjectIsNullInDto() {
        CaseData caseData = CaseData.builder()
            .id(caseId)
            .paymentServiceRequestReferenceNumber("some-ref")
            .build();
        // DTO with null payment - call the private method directly to avoid processCallback's unguarded logging
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("some-ref")
            .serviceRequestStatus("Paid")
            .payment(null)
            .build();

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        // Duplicate check should return true and prevent startUpdate
        verify(coreCaseDataService, never()).startUpdate(anyString(), any(), anyString(), anyBoolean());
    }

    @Test
    void shouldSkipProcessingWhenServiceRequestReferenceMissing() {
        CaseData caseData = CaseData.builder()
            .id(caseId)
            .paymentServiceRequestReferenceNumber("some-ref")
            .build();
        // DTO with empty serviceRequestReference - call the private method directly
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder().paymentReference("p").paymentAmount("1").build())
            .build();

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        // Duplicate check should return true and prevent startUpdate
        verify(coreCaseDataService, never()).startUpdate(anyString(), any(), anyString(), anyBoolean());
    }

    @Test
    void shouldSkipProcessingWhenCaseDataIsNull() {
        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("ref")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder().paymentReference("p").paymentAmount("1").build())
            .build();

        try (org.mockito.MockedStatic<CaseUtils> caseUtilsMock = org.mockito.Mockito.mockStatic(CaseUtils.class)) {
            caseUtilsMock.when(() -> CaseUtils.getCaseData(caseDetails, objectMapper)).thenReturn(null);

            requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

            // Duplicate check should return true and prevent startUpdate
            verify(coreCaseDataService, never()).startUpdate(anyString(), any(), anyString(), anyBoolean());
        }
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
            .serviceRequestStatus("Not paid")                                 // Forces isStatusPaid = false
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentReference("reference")
                         .build())
            .build();

        when(coreCaseDataService.findCaseById(anyString(), eq(caseId.toString()))).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        // Verify it triggers the AWP failure event path
        ArgumentCaptor<CaseEvent> eventCaptor = ArgumentCaptor.forClass(CaseEvent.class);
        verify(coreCaseDataService, atLeastOnce()).eventRequest(eventCaptor.capture(), anyString());
        List<CaseEvent> capturedEvents = eventCaptor.getAllValues();
        assertTrue(capturedEvents.stream().anyMatch(e -> e == CaseEvent.AWP_PAYMENT_FAILURE_CALLBACK));
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

        assertEquals(
            State.SUBMITTED_PAID, updatedCaseData.getState(),
            "Case state should transition to SUBMITTED_PAID when payment is successful"
        );

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

        assertEquals(
            State.SUBMITTED_PAID, updatedCaseData.getState(),
            "Case state should move from WITHDRAWN to SUBMITTED_PAID upon successful payment"
        );

        assertNotNull(
            updatedCaseData.getDateSubmitted(),
            "Date submitted should be set even when coming from a withdrawn state"
        );
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

        assertEquals(
            State.CASE_ISSUED, updatedCaseData.getState(),
            "State should NOT change back to SUBMITTED_PAID if the case is already ISSUED"
        );
    }
}
