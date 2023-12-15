package uk.gov.hmcts.reform.prl.models.roleassignment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.roleassignment.RoleRequest;

@Data
@Schema(description = "The request object for RoleAssignment")
public class RoleAssignmentRequest {

    private RoleRequest roleRequest;
    private RequestedRoles requestedRoles;
}
