package uk.gov.hmcts.reform.prl.clients;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.dto.payment.OnlineCardPaymentRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentGroupReferenceStatusResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentStatusResponse;

@ConditionalOnProperty(prefix = "payments", name = "api.url")
@FeignClient(name = "payments", url = "${payments.api.url}")
public interface PaymentApi {

    @PostMapping(value = "/service-request", consumes = "application/json")
    PaymentServiceResponse createPaymentServiceRequest(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestBody PaymentServiceRequest paymentRequest
    );

    @PostMapping(value = "/service-request/{service-request-reference}/card-payments", consumes = "application/json")
    PaymentResponse createPaymentRequest(
            @PathVariable("service-request-reference") String serviceRequestReference,
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestBody OnlineCardPaymentRequest onlineCardPaymentRequest
    );

    @GetMapping(value = "/card-payments/{reference}", consumes = "application/json")
    PaymentStatusResponse fetchPaymentStatus(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @PathVariable("reference") String paymentReference
    );

    @GetMapping(value = "/payment-groups/{payment-group-reference}", consumes = "application/json")
    PaymentGroupReferenceStatusResponse fetchPaymentGroupReferenceStatus(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @PathVariable("payment-group-reference") String paymentGroupReference
    );
}
