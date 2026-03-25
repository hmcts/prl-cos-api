package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.payment.PaymentAsyncService;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RequestUpdateCallbackService {

    private final PaymentAsyncService paymentAsyncService;

    public void processCallback(ServiceRequestUpdateDto serviceRequestUpdateDto) {
        if (serviceRequestUpdateDto == null || StringUtils.isBlank(serviceRequestUpdateDto.getCcdCaseNumber())) {
            log.error("Discarding payment callback: Missing Case Number in DTO.");
            return;
        }

        paymentAsyncService.handlePaymentCallback(serviceRequestUpdateDto);
    }
}
