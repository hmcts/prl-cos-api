package uk.gov.hmcts.reform.prl.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.QueryRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.QueryResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi.SERVICE_AUTHORIZATION;

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


    @DeleteMapping(path = "/am/role-assignments/{assignmentId}", consumes = "application/json")
    RoleAssignmentResponse deleteRoleAssignment(
            @RequestHeader("Authorization") String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestHeader("x-correlation-id") String xcorrelationId,
            @PathVariable("assignmentId") String assignmentId);

    @PostMapping(
            value = "/am/role-assignments/query",
            consumes = APPLICATION_JSON_VALUE,
            headers = {CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE, "size=50"}
    )
    QueryResponse queryRoleAssignments(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
            @RequestBody QueryRequest request
    );


    @GetMapping(value = "/am/role-assignments/actors/{actorId}", consumes = "application/json")
    RoleAssignmentServiceResponse getRoleAssignments(
        @RequestHeader("Authorization") String authorization,
        @RequestHeader("ServiceAuthorization") String serviceAuthorization,
        @RequestHeader("x-correlation-id") String xcorrelationId,
        @PathVariable("actorId") String actorId);
}
