package uk.gov.hmcts.reform.prl.models.roleassignment.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.roleassignment.RequestedRoles;
import uk.gov.hmcts.reform.prl.models.roleassignment.RoleRequest;

import java.util.List;

@Data
@Schema(description = "The response object for RoleAssignment")
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentResponse {

    private RoleRequest roleRequest;
    private List<RequestedRoles> requestedRoles;
}
