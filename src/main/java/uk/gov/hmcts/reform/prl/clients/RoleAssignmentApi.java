package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.roleassignment.response.RoleAssignmentResponse;

@FeignClient(name = "amRoleAssignment",
        url = "${amRoleAssignment.api.url}",
        configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface RoleAssignmentApi {

    @GetMapping("/am/role-assignments")
    RoleAssignmentResponse getRoleAssignment(
        @RequestHeader("Authorisation") String authorisation,
        @RequestHeader("serviceAuthorisation") String serviceAuthorisation,
        @RequestHeader("x-correlation-id") String xcorrelationId,
        @RequestHeader("content-type") String contentType,
        @RequestBody Object roleAssignmentRequest);
}
