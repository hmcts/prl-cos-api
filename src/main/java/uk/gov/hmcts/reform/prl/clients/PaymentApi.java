package uk.gov.hmcts.reform.prl.clients;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;

@ConditionalOnProperty(prefix = "payments", name = "api.url")
@FeignClient(name = "payments", url = "${payments.api.url}")
public interface PaymentApi {

    @PostMapping(value = "/service-request", consumes = "application/json")
    PaymentServiceResponse createPaymentServiceRequest(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestBody PaymentServiceRequest paymentRequest
    );
}
