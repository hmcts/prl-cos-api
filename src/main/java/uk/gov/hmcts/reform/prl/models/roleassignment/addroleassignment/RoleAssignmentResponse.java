package uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Schema(description = "The response object for RoleAssignment")
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentResponse {

    private RoleRequest roleRequest;
    private List<RequestedRoles> requestedRoles;
}
