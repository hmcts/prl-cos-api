package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.prl.models.roleassignment.RequestedRoles;
import uk.gov.hmcts.reform.prl.models.roleassignment.request.RoleAssignmentRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.response.RoleAssignmentResponse;

import java.util.List;

@FeignClient(name = "amRoleAssignment",
        url = "${amRoleAssignment.api.url}",
        configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface RoleAssignmentApi {

    @PostMapping(path = "/am/role-assignments", consumes = "application/json")
    RoleAssignmentResponse updateRoleAssignment(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("x-correlation-id") String xcorrelationId,
        @RequestBody RoleAssignmentRequest roleAssignmentRequest);


    @GetMapping(path = "/am/role-assignments/actors/", consumes = "application/json")
    List<RequestedRoles> getRoleAssignments(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("x-correlation-id") String xcorrelationId,
        @RequestParam("actorId") String actorId);
}
