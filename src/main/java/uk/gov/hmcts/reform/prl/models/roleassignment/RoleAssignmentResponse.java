package uk.gov.hmcts.reform.prl.models.roleassignment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "The response object to RoleAssignment")
public class RoleAssignmentResponse {

    private RoleRequest roleRequest;
    private RequestedRoles requestedRoles;
}
