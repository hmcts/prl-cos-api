package uk.gov.hmcts.reform.prl.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentServiceForSystemUser;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SystemUserRoleAssingmentController {

    @Autowired
    private RoleAssignmentServiceForSystemUser roleAssignmentServiceForSystemUser;

    private final AuthorisationService authorisationService;

    @GetMapping("/systemUserRoleAssignment")
    @Operation(description = "role assignment")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Roles assigned successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public ResponseEntity<Object> assignRole(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation) {
        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation))) {
            roleAssignmentServiceForSystemUser.assignHearingRoleToSysUser();
            ResponseEntity<Object> response = new ResponseEntity<>(HttpStatusCode.valueOf(200));
            return response;
        } else {
            throw new ResponseStatusException(UNAUTHORIZED);
        }
    }

}
