package uk.gov.hmcts.reform.prl.services.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Payment;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CcdPaymentServiceRequestUpdate;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.payment.PaymentAsyncService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.UploadAdditionalApplicationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class PaymentAsyncServiceTest {
    @Mock private SolicitorEmailService solicitorEmailService;
    @Mock private CaseWorkerEmailService caseWorkerEmailService;
    @Mock private AllTabServiceImpl allTabService;
    @Mock private PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    @Mock private CcdCoreCaseDataService coreCaseDataService;
    @Mock private ObjectMapper objectMapper;
    @Mock private CourtFinderService courtFinderService;
    @Mock private SystemUserService systemUserService;
    @Mock private UploadAdditionalApplicationUtils uploadAdditionalApplicationUtils;

    @InjectMocks
    private PaymentAsyncService paymentAsyncService;

    private static final String AUTH = "Bearer TestAuthToken";
    private static final String CASE_ID_STR = "1234567887654321";
    private static final Long CASE_ID = 1234567887654321L;
    private static final String SERVICE_REQUEST_REF = "2026-1750003223075";
    private static final String PAID_STATUS = "Paid";

    @BeforeEach
    void setUp() {
        lenient().when(systemUserService.getSysUserToken()).thenReturn(AUTH);
        lenient().when(systemUserService.getUserId(AUTH)).thenReturn("user-id");
        lenient().when(coreCaseDataService.eventRequest(any(), any()))
            .thenReturn(EventRequestData.builder().build());
    }

    @ParameterizedTest(name = "Run {index}: Transitioning from {0} should result in {1}")
    @CsvSource({
        "SUBMITTED_NOT_PAID, SUBMITTED_PAID",
        "CASE_WITHDRAWN,      SUBMITTED_PAID",
        "CASE_ISSUED,         CASE_ISSUED",
        "JUDICIAL_REVIEW,     JUDICIAL_REVIEW"
    })
    void shouldVerifyStateTransitionDuringPayment(State initialState, State expectedState) {
        CaseData caseData = CaseData.builder().id(1L).state(initialState).build();
        ServiceRequestUpdateDto dto = createSimplePaidDto(SERVICE_REQUEST_REF, "Paid");

        CaseData updatedCaseData = paymentAsyncService.getCaseDataWithStateAndDateSubmitted(dto, caseData);

        assertEquals(expectedState, updatedCaseData.getState(),
                     String.format("Failed to transition from %s to %s", initialState, expectedState));
    }

    @Test
    void shouldNotUpdateIfPaymentIsAlreadyProcessed() {
        ServiceRequestUpdateDto dto = createSimplePaidDto(SERVICE_REQUEST_REF, PAID_STATUS);

        CaseData existingCaseData = CaseData.builder()
            .id(CASE_ID)
            .paymentServiceRequestReferenceNumber(SERVICE_REQUEST_REF)
            .paymentCallbackServiceRequestUpdate(CcdPaymentServiceRequestUpdate.builder()
                                                     .serviceRequestStatus("Paid")
                                                     .build())
            .build();

        CaseDetails details = createMockDetails();

        when(coreCaseDataService.findCaseById(AUTH, CASE_ID_STR)).thenReturn(details);
        when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(existingCaseData);

        paymentAsyncService.handlePaymentCallback(dto);

        verify(coreCaseDataService).findCaseById(AUTH, CASE_ID_STR);
        verifyNoInteractions(allTabService, solicitorEmailService, caseWorkerEmailService);
        verify(coreCaseDataService, never()).startUpdate(any(), any(), any(), anyBoolean());
    }

    @ParameterizedTest(name = "Run {index}: Status={0}, IsCasePayment={1} -> ExpectedEvent={2}")
    @CsvSource({
        "Paid,     true,  PAYMENT_SUCCESS_CALLBACK",
        "Not Paid, true,  PAYMENT_FAILURE_CALLBACK",
        "Paid,     false, AWP_PAYMENT_SUCCESS_CALLBACK",
        "Not Paid, false, AWP_PAYMENT_FAILURE_CALLBACK"
    })
    void shouldVerifyCorrectCcdEventIsTriggered(String status, boolean isCasePayment, CaseEvent expectedEvent) {
        ServiceRequestUpdateDto dto = createSimplePaidDto(SERVICE_REQUEST_REF, status);

        String caseDataRef = isCasePayment ? SERVICE_REQUEST_REF : "different-ref";

        CaseData mockCaseData = CaseData.builder()
            .id(CASE_ID)
            .paymentServiceRequestReferenceNumber(caseDataRef)
            .additionalApplicationsBundle(createAwpBundle("Pending"))
            .state(State.SUBMITTED_NOT_PAID)
            .build();

        stubCcdInteractions(mockCaseData, createMockDetails());

        lenient().when(uploadAdditionalApplicationUtils.getAwPTaskNameWhenPaymentCompleted(any()))
            .thenReturn("AWP Task");

        paymentAsyncService.handlePaymentCallback(dto);

        verify(coreCaseDataService).eventRequest(eq(expectedEvent), any());
    }

    @Test
    void shouldExecuteFullFlowForCasePayment() {
        ServiceRequestUpdateDto dto = createSimplePaidDto("test-ref", PAID_STATUS);
        CaseData mockCaseData = CaseData.builder()
            .id(CASE_ID)
            .paymentServiceRequestReferenceNumber("test-ref")
            .state(State.SUBMITTED_NOT_PAID)
            .build();

        stubCcdInteractions(mockCaseData, createMockDetails());

        paymentAsyncService.handlePaymentCallback(dto);

        verify(coreCaseDataService, atLeast(1)).submitUpdate(eq(AUTH), any(), any(), eq(CASE_ID_STR), anyBoolean());
        verify(solicitorEmailService).sendEmail(any());
        verify(partyLevelCaseFlagsService).generateAndStoreCaseFlags(CASE_ID_STR);
    }

    @Test
    void shouldProcessAdditionalApplicationsBundlePayment() {
        CaseData mockCaseData = CaseData.builder()
            .id(CASE_ID)
            .additionalApplicationsBundle(createAwpBundle("Pending"))
            .build();

        ServiceRequestUpdateDto dto = createSimplePaidDto(SERVICE_REQUEST_REF, PAID_STATUS);

        stubCcdInteractions(mockCaseData, createMockDetails());
        when(uploadAdditionalApplicationUtils.getAwPTaskNameWhenPaymentCompleted(any())).thenReturn("AWP Task");

        paymentAsyncService.handlePaymentCallback(dto);

        verify(uploadAdditionalApplicationUtils).getAwPTaskNameWhenPaymentCompleted(any());
        verify(coreCaseDataService, atLeastOnce()).submitUpdate(eq(AUTH), any(), any(), eq(CASE_ID_STR), eq(true));
        verify(solicitorEmailService).sendEmail(any());
    }

    @Test
    void shouldUpdateExistingFailedPaymentToSuccessInAwpBundle() {
        CaseData mockCaseData = CaseData.builder()
            .id(CASE_ID)
            .additionalApplicationsBundle(createAwpBundle("Failed"))
            .build();

        ServiceRequestUpdateDto dto = createSimplePaidDto(SERVICE_REQUEST_REF, PAID_STATUS);

        stubCcdInteractions(mockCaseData, createMockDetails());
        when(uploadAdditionalApplicationUtils.getAwPTaskNameWhenPaymentCompleted(any())).thenReturn("AWP Task");

        paymentAsyncService.handlePaymentCallback(dto);

        verify(uploadAdditionalApplicationUtils).getAwPTaskNameWhenPaymentCompleted(any());
        verify(coreCaseDataService, atLeastOnce()).submitUpdate(eq(AUTH), any(), any(), eq(CASE_ID_STR), eq(true));
        verify(solicitorEmailService).sendEmail(any());
    }

    private void stubCcdInteractions(CaseData caseData, CaseDetails details) {
        lenient().when(objectMapper.convertValue(any(), eq(CaseData.class))).thenReturn(caseData);
        when(coreCaseDataService.findCaseById(AUTH, CASE_ID_STR)).thenReturn(details);
        when(coreCaseDataService.startUpdate(any(), any(), any(), anyBoolean()))
            .thenReturn(StartEventResponse.builder().caseDetails(details).token("token").build());
    }

    private CaseDetails createMockDetails() {
        return CaseDetails.builder().id(CASE_ID).data(new HashMap<>()).build();
    }

    private ServiceRequestUpdateDto createSimplePaidDto(String reference, String status) {
        return ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(CASE_ID_STR)
            .serviceRequestReference(reference)
            .serviceRequestStatus(status)
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentReference("RC-REF")
                         .paymentMethod("card")
                         .build())
            .build();
    }

    private List<Element<AdditionalApplicationsBundle>> createAwpBundle(String status) {
        List<Element<AdditionalApplicationsBundle>> bundles = new ArrayList<>();
        bundles.add(element(AdditionalApplicationsBundle.builder()
                                .payment(Payment.builder().paymentServiceRequestReferenceNumber(SERVICE_REQUEST_REF).build())
                                .otherApplicationsBundle(OtherApplicationsBundle.builder().applicationStatus(status).build())
                                .build()));
        return bundles;
    }
}
