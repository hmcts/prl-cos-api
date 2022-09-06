package uk.gov.hmcts.reform.prl.clients;


import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.dto.payment.OnlineCardPaymentRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentStatusForCitizen;

@ConditionalOnProperty(prefix = "payments", name = "api.url")
@FeignClient(name = "payments", url = "${payments.api.url}")
public interface PaymentApi {

    @PostMapping(value = "/service-request", consumes = "application/json")
    PaymentServiceResponse createPaymentServiceRequest(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestBody PaymentServiceRequest paymentRequest
    );

    @PostMapping(value = "/service-request/{service_request_reference}/card-payments", consumes = "application/json")
    PaymentResponse createPaymentRequest(
            @PathVariable("service_request_reference") String serviceRequestReference,
            @RequestHeader("authorization") String authorization,
            @RequestHeader("service_authorization") String serviceAuthorization,
            @RequestBody OnlineCardPaymentRequest onlineCardPaymentRequest
    );

    @GetMapping(value = "/card-payments/{reference}", consumes = "application/json")
    PaymentStatusForCitizen fetchPaymentStatus(
        @RequestHeader("authorization") String authorization,
        @RequestHeader("serviceAuthorization") String serviceAuthorization,
        @PathVariable("reference") String paymentReference
    );
}
