package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.payment.PaymentAsyncService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RequestUpdateCallbackServiceTest {

    @Mock
    private PaymentAsyncService paymentAsyncService;

    @InjectMocks
    private RequestUpdateCallbackService requestUpdateCallbackService;

    @Test
    void shouldCallAsyncServiceWhenCaseNumberIsValid() {
        ServiceRequestUpdateDto dto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber("1234567887654321")
            .build();

        requestUpdateCallbackService.processCallback(dto);

        verify(paymentAsyncService).handlePaymentCallback(dto);
    }

    @Test
    void shouldNotCallAsyncServiceWhenCaseNumberIsBlank() {
        ServiceRequestUpdateDto dto = ServiceRequestUpdateDto.builder()
            .ccdCaseNumber("")
            .build();

        requestUpdateCallbackService.processCallback(dto);

        verifyNoInteractions(paymentAsyncService);
    }

    @Test
    void shouldNotCallAsyncServiceWhenDtoIsNull() {
        requestUpdateCallbackService.processCallback(null);

        verifyNoInteractions(paymentAsyncService);
    }
}
