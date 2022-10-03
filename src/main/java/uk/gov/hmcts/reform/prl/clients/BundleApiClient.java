package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@FeignClient(name = "bundle", url = "${bundle.api.url}",configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface BundleApiClient {
    @PostMapping(value = "/api/new-bundle", consumes = "application/json")
    PreSubmitCallbackResponse<CaseData> createBundleServiceRequest(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestBody BundleCallbackRequest<CaseData> bundleCallbackRequest
        );
}
