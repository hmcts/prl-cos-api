package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffResponse;

import java.util.List;

@FeignClient(name = "staff-ref-data-api", url = "${staffDetails.api.url}",configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface StaffResponseDetailsApi {

    @GetMapping(value = "/refdata/internal/staff/usersByServiceName", consumes = "application/json")
    ResponseEntity<List<StaffResponse>> getAllStaffResponseDetails(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestParam("ccd_service_names") String ccdServiceNames,
        @RequestParam("sort_column") String sortColumn,
        @RequestParam("sort_direction") String sortDirection,
        @RequestParam("page_size") int pageSize,
        @RequestParam("page_number") int pageNumber
    );
}
