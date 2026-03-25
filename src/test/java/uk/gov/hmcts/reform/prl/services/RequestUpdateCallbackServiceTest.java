package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.ApplicationStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Payment;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.payment.PaymentAsyncService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.UploadAdditionalApplicationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
public class RequestUpdateCallbackServiceTest {

    private final String jurisdiction = "PRIVATELAW";
    private final String caseType = "PRLAPPS";
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
    PaymentAsyncService paymentAsyncService;

    @Mock
    UploadAdditionalApplicationUtils uploadAdditionalApplicationUtils;

    private StartEventResponse startEventResponse;
    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        lenient().when(systemUserService.getSysUserToken()).thenReturn(authToken);
        lenient().when(systemUserService.getUserId(authToken)).thenReturn(systemUserId);

        caseDetails = CaseDetails.builder()
            .id(caseId)
            .data(Map.of("id", 1)) // This prevents the .getData() NPE
            .build();

        startEventResponse = StartEventResponse.builder()
            .eventId(eventName)
            .caseDetails(caseDetails)
            .token(eventToken)
            .build();

        // 3. Stub the CoreCaseDataService (NOT the API)
        lenient().when(coreCaseDataService.findCaseById(any(), any())).thenReturn(caseDetails);
        lenient().when(coreCaseDataService.startUpdate(any(), any(), any(), anyBoolean()))
            .thenReturn(startEventResponse);
    }

    @Test
    void shouldStartAndSubmitEventWithCaseDetails()  {
        // 1. Arrange
        CaseData caseData = CaseData.builder()
            .id(1L)
            .paymentServiceRequestReferenceNumber("test-ref")
            .build();

        // Ensure User Service returns what we expect
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
        when(systemUserService.getUserId(any())).thenReturn(systemUserId);

        // CoreCaseDataService stubs - use any() to ensure the "Act" hits them
        when(coreCaseDataService.findCaseById(any(), any())).thenReturn(caseDetails);
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        // Stub the event flow parts
        when(coreCaseDataService.eventRequest(any(), any())).thenReturn(EventRequestData.builder().build());

        // IMPORTANT: Fix the Matcher mismatch here
        when(coreCaseDataService.startUpdate(any(), any(), any(), anyBoolean()))
            .thenReturn(startEventResponse);

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("test-ref")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder().paymentAmount("123").build())
            .build();

        // 2. Act
        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        // 3. Assert
        // Verify that startUpdate was called with the right arguments
        // Use eq() for all or none to satisfy Mockito
        verify(coreCaseDataService).startUpdate(
            eq(authToken),
            any(),
            eq(caseId.toString()),
            eq(true)
        );

        verify(coreCaseDataService).submitUpdate(
            eq(authToken),
            any(),
            any(),
            eq(caseId.toString()),
            eq(true)
        );

    }

    @Test
    void shouldNotStartOrSubmitEventWithoutCaseDetails() {
        when(systemUserService.getSysUserToken()).thenReturn(authToken);
        when(coreCaseDataService.findCaseById(any(), any())).thenReturn(null);

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber("123")
            .serviceRequestStatus("Paid")
            .build();

        // 2. Act & Assert
        // When the service tries to call verifyCaseCreationPaymentReference(null, ...),
        // it will throw a NullPointerException.
        assertThrows(NullPointerException.class, () -> {
            requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);
        });

        // 3. Verify - Prove that no event was ever started because it crashed early
        verifyNoInteractions(paymentAsyncService);
        verify(coreCaseDataService, never()).startUpdate(any(), any(), any(), anyBoolean());
    }

    @Test
    void shouldProcessCallback() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(1L)
            .paymentServiceRequestReferenceNumber("test-reference")
            .build();

        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("test-reference")
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder().paymentAmount("123").build())
            .build();

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        verify(paymentAsyncService).handlePaymentCallback(any(), any(), any());
    }

    @Test
    void shouldProcessCallbackNotPaid() throws Exception {
        // 1. Arrange
        CaseData caseData = CaseData.builder()
            .id(1L)
            .paymentServiceRequestReferenceNumber("test-reference")
            .build();

        // Mocking the service wrapper correctly
        when(coreCaseDataService.findCaseById(authToken, caseId.toString())).thenReturn(caseDetails);
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        // Use any() for the complex request objects to avoid "getEventId" issues
        when(coreCaseDataService.eventRequest(any(), any())).thenReturn(EventRequestData.builder().build());
        when(coreCaseDataService.startUpdate(any(), any(), any(), anyBoolean()))
            .thenReturn(startEventResponse);

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("test-reference")
            .payment(PaymentDto.builder().paymentAmount("123").build())
            .serviceRequestStatus("test") // Not "Paid"
            .build();

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        verify(paymentAsyncService).handlePaymentCallback(serviceRequestUpdateDto, authToken, systemUserId);
        verify(coreCaseDataService).submitUpdate(eq(authToken), any(), any(), eq(caseId.toString()), anyBoolean());
    }

    @Test
    void shouldProcessPendingCallback() throws Exception {
        // 1. Arrange - Setup the AWP Bundle
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder()
                                                     .payment(Payment.builder()
                                                                  .paymentServiceRequestReferenceNumber("Paid") // Matches DTO reference below
                                                                  .build())
                                                     .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                  .applicationStatus(ApplicationStatus
                                                                                                         .SUBMITTED
                                                                                                         .getDisplayedValue())
                                                                                  .build())
                                                     .build()));

        CaseData caseData = CaseData.builder()
            .id(1L)
            .paymentServiceRequestReferenceNumber("test-reference") // DIFFERENT from "Paid"
            .additionalApplicationsBundle(additionalApplicationsBundle)
            .build();

        // Mock the new service wrapper
        when(coreCaseDataService.findCaseById(authToken, caseId.toString())).thenReturn(caseDetails);
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);

        // Stub the CCD event calls
        when(coreCaseDataService.eventRequest(any(), any())).thenReturn(EventRequestData.builder().build());
        when(coreCaseDataService.startUpdate(any(), any(), any(), anyBoolean()))
            .thenReturn(startEventResponse);

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestReference("Paid") // Matches the AWP bundle reference
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder().paymentAmount("123").build())
            .build();

        requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);

        verifyNoInteractions(paymentAsyncService);

        verify(coreCaseDataService).submitUpdate(
            eq(authToken),
            any(),
            any(), // This contains the updated AWP bundle
            eq(caseId.toString()),
            eq(true)
        );
        verify(uploadAdditionalApplicationUtils).getAwPTaskNameWhenPaymentCompleted(any());
    }

    @Test
    void shouldThrowExceptionForProcessCallback() {
        doThrow(new RuntimeException("Database Connection Failed"))
            .when(coreCaseDataService).findCaseById(any(), any());

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(caseId.toString())
            .serviceRequestStatus("Paid")
            .build();

        // 2. Act & Assert
        // This tells JUnit: "I expect this call to blow up with a RuntimeException"
        assertThrows(RuntimeException.class, () -> {
            requestUpdateCallbackService.processCallback(serviceRequestUpdateDto);
        });

        // 3. Verify - Prove that because it failed, it NEVER reached the Async service
        verifyNoInteractions(paymentAsyncService);
    }


    private CaseDataContent buildCaseDataContent(String eventId, String eventToken, Object caseData) {
        return CaseDataContent.builder()
            .eventToken(eventToken)
            .event(Event.builder()
                       .id(eventId)
                       .build())
            .data(caseData)
            .build();
    }
}
