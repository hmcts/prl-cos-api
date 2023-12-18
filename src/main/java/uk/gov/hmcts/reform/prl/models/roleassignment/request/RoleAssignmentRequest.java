package uk.gov.hmcts.reform.prl.models.roleassignment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.roleassignment.RequestedRoles;
import uk.gov.hmcts.reform.prl.models.roleassignment.RoleRequest;

import java.util.List;

@Data
@Schema(description = "The request object for RoleAssignment")
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "roleAssignmentRequest")
public class RoleAssignmentRequest {

    private RoleRequest roleRequest;
    private List<RequestedRoles> requestedRoles;
}
