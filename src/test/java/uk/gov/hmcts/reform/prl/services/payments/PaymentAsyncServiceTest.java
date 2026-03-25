package uk.gov.hmcts.reform.prl.services.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentDto;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.payment.PaymentAsyncService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PaymentAsyncServiceTest {

    @Mock
    private SolicitorEmailService solicitorEmailService;
    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;
    @Mock
    private AllTabServiceImpl allTabService;
    @Mock
    private PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    @Mock
    private CcdCoreCaseDataService coreCaseDataService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private CourtFinderService courtFinderService;
    @Mock
    private ServiceRequestUpdateDto serviceRequestUpdateDto;

    // 2. Use InjectMocks to create a REAL instance of the async service
    @InjectMocks
    private PaymentAsyncService paymentAsyncService;

    private final Long caseId = 1234567887654321L;

    @Test
    void shouldUpdateCaseStateToSubmittedWhenPaymentIsSuccessful() {
        // Arrange
        CaseData caseData = CaseData.builder().state(State.SUBMITTED_NOT_PAID).build();
        ServiceRequestUpdateDto dto = ServiceRequestUpdateDto.builder().serviceRequestStatus("Paid").build();

        // Act - This will now actually run the code because it's a real instance
        CaseData updatedCaseData = paymentAsyncService.getCaseDataWithStateAndDateSubmitted(dto, caseData);

        // Assert
        assertEquals(State.SUBMITTED_PAID, updatedCaseData.getState());
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
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentMethod("cash")
                         .paymentReference("reference")
                         .caseReference("reference")
                         .accountNumber("123445555")
                         .build())
            .serviceRequestStatus("Paid")
            .build();

        CaseData updatedCaseData =
            paymentAsyncService.getCaseDataWithStateAndDateSubmitted(serviceRequestUpdateDto, caseData);

        Assert.assertEquals(State.CASE_ISSUED, updatedCaseData.getState());
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
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentMethod("cash")
                         .paymentReference("reference")
                         .caseReference("reference")
                         .accountNumber("123445555")
                         .build())
            .serviceRequestStatus("Paid")
            .build();

        CaseData updatedCaseData =
            paymentAsyncService.getCaseDataWithStateAndDateSubmitted(serviceRequestUpdateDto, caseData);

        Assert.assertEquals(State.SUBMITTED_PAID, updatedCaseData.getState());
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
            .payment(PaymentDto.builder()
                         .paymentAmount("123")
                         .paymentMethod("cash")
                         .paymentReference("reference")
                         .caseReference("reference")
                         .accountNumber("123445555")
                         .build())
            .serviceRequestStatus("Paid")
            .build();

        CaseData updatedCaseData =
            paymentAsyncService.getCaseDataWithStateAndDateSubmitted(serviceRequestUpdateDto, caseData);

        Assert.assertEquals(State.SUBMITTED_PAID, updatedCaseData.getState());
    }
}
