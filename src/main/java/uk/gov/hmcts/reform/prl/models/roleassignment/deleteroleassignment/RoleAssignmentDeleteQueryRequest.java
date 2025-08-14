package uk.gov.hmcts.reform.prl.models.roleassignment.deleteroleassignment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentQueryRequest;

import java.util.List;

@Data
@Schema(description = "The request object for Query RoleAssignment")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleAssignmentDeleteQueryRequest {

    private List<RoleAssignmentQueryRequest> queryRequests;
}
