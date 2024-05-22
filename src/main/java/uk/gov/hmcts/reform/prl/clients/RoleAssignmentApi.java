package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;

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


    @GetMapping(value = "/am/role-assignments/actors/{actorId}", consumes = "application/json")
    RoleAssignmentServiceResponse getRoleAssignments(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("x-correlation-id") String xcorrelationId,
        @PathVariable("actorId") String actorId);


    @DeleteMapping(value = "/am/role-assignments/{assignmentId}", consumes = "application/json")
    void removeRoleAssignments(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("x-correlation-id") String xcorrelationId,
        @PathVariable("assignmentId") String assignmentId);
}
