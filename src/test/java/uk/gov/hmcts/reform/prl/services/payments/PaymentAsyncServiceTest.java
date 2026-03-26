package uk.gov.hmcts.reform.prl.services.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Payment;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
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
import static org.mockito.Mockito.verify;
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

    @BeforeEach
    void setUp() {
        lenient().when(systemUserService.getSysUserToken()).thenReturn(AUTH);
        lenient().when(systemUserService.getUserId(AUTH)).thenReturn("user-id");
        lenient().when(coreCaseDataService.eventRequest(any(), any()))
            .thenReturn(EventRequestData.builder().build());
    }

    @Test
    void shouldUpdateCaseStateToSubmittedWhenPaymentIsSuccessful() {
        CaseData caseData = CaseData.builder().state(State.SUBMITTED_NOT_PAID).build();
        ServiceRequestUpdateDto dto = createSimplePaidDto("test-ref");

        CaseData updatedCaseData = paymentAsyncService.getCaseDataWithStateAndDateSubmitted(dto, caseData);

        assertEquals(State.SUBMITTED_PAID, updatedCaseData.getState());
    }

    @Test
    void shouldNotUpdateCaseStateToSubmittedWhenCurrentCaseStateIsCaseIssuedWhenPaymentIsSuccessful() {
        CaseData caseData = CaseData.builder().id(1L).state(State.CASE_ISSUED).build();
        ServiceRequestUpdateDto dto = createSimplePaidDto("test-ref");

        CaseData updatedCaseData = paymentAsyncService.getCaseDataWithStateAndDateSubmitted(dto, caseData);

        assertEquals(State.CASE_ISSUED, updatedCaseData.getState());
    }

    @Test
    void shouldUpdateCaseStateToSubmittedWhenCurrentCaseStateIsPendingWhenPaymentIsSuccessful() {
        CaseData caseData = CaseData.builder().id(1L).state(State.SUBMITTED_NOT_PAID).build();
        ServiceRequestUpdateDto dto = createSimplePaidDto("test-ref");

        CaseData updatedCaseData = paymentAsyncService.getCaseDataWithStateAndDateSubmitted(dto, caseData);

        assertEquals(State.SUBMITTED_PAID, updatedCaseData.getState());
    }

    @Test
    void shouldUpdateCaseStateToSubmittedWhenCurrentCaseStateIsWithdrawnWhenPaymentIsSuccessful() {
        CaseData caseData = CaseData.builder().id(1L).state(State.CASE_WITHDRAWN).build();
        ServiceRequestUpdateDto dto = createSimplePaidDto("test-ref");

        CaseData updatedCaseData = paymentAsyncService.getCaseDataWithStateAndDateSubmitted(dto, caseData);

        assertEquals(State.SUBMITTED_PAID, updatedCaseData.getState());
    }

    @Test
    void shouldExecuteFullFlowForCasePayment() {
        ServiceRequestUpdateDto dto = createSimplePaidDto("test-ref");
        CaseData mockCaseData = CaseData.builder()
            .id(CASE_ID)
            .paymentServiceRequestReferenceNumber("test-ref")
            .state(State.SUBMITTED_NOT_PAID)
            .build();

        stubCcdInteractions(mockCaseData, createMockDetails());

        paymentAsyncService.handlePaymentCallback(dto);

        verify(coreCaseDataService, atLeast(2)).submitUpdate(eq(AUTH), any(), any(), eq(CASE_ID_STR), anyBoolean());
        verify(solicitorEmailService).sendEmail(any());
        verify(partyLevelCaseFlagsService).generateAndStoreCaseFlags(CASE_ID_STR);
    }

    @Test
    void shouldProcessAdditionalApplicationsBundlePayment() {
        String ref = "PAY-AWP-123";
        CaseData mockCaseData = CaseData.builder()
            .id(CASE_ID)
            .additionalApplicationsBundle(createAwpBundle(ref, "Pending"))
            .build();

        ServiceRequestUpdateDto dto = createSimplePaidDto(ref);

        stubCcdInteractions(mockCaseData, createMockDetails());
        when(uploadAdditionalApplicationUtils.getAwPTaskNameWhenPaymentCompleted(any())).thenReturn("AWP Task");

        paymentAsyncService.handlePaymentCallback(dto);

        verify(uploadAdditionalApplicationUtils).getAwPTaskNameWhenPaymentCompleted(any());
        verify(coreCaseDataService, atLeastOnce()).submitUpdate(eq(AUTH), any(), any(), eq(CASE_ID_STR), eq(true));
        verify(solicitorEmailService).sendEmail(any());
    }

    @Test
    void shouldUpdateExistingFailedPaymentToSuccessInAwpBundle() {
        String ref = "REF-2026-XYZ";
        CaseData mockCaseData = CaseData.builder()
            .id(CASE_ID)
            .additionalApplicationsBundle(createAwpBundle(ref, "Failed"))
            .build();

        ServiceRequestUpdateDto dto = createSimplePaidDto(ref);

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

    private ServiceRequestUpdateDto createSimplePaidDto(String reference) {
        return ServiceRequestUpdateDto.builder()
            .ccdCaseNumber(CASE_ID_STR)
            .serviceRequestReference(reference)
            .serviceRequestStatus("Paid")
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentReference("RC-REF")
                         .paymentMethod("card")
                         .build())
            .build();
    }

    private List<Element<AdditionalApplicationsBundle>> createAwpBundle(String ref, String status) {
        List<Element<AdditionalApplicationsBundle>> bundles = new ArrayList<>();
        bundles.add(element(AdditionalApplicationsBundle.builder()
                                .payment(Payment.builder().paymentServiceRequestReferenceNumber(ref).build())
                                .otherApplicationsBundle(OtherApplicationsBundle.builder().applicationStatus(status).build())
                                .build()));
        return bundles;
    }
}
