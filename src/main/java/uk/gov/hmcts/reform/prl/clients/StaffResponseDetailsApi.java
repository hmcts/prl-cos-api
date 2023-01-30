package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffAPIResponse;

@FeignClient(name = "staff-ref-data-api", url = "${staffDetails.api.url}",configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface StaffResponseDetailsApi {

    @GetMapping(value = "/refdata/internal/staff/usersByServiceName", consumes = "application/json")
    StaffAPIResponse getAllStaffResponseDetails(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestParam("ccd_service_names") String ccdServiceNames
    );
}
