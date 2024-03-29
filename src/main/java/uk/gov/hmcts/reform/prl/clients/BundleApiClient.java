package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateResponse;

@FeignClient(name = "bundle", url = "${bundle.api.url}",configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface BundleApiClient {
    @PostMapping(value = "/api/new-bundle", consumes = "application/json")
    BundleCreateResponse createBundleServiceRequest(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestBody BundleCreateRequest bundleCreateRequest
        );
}
