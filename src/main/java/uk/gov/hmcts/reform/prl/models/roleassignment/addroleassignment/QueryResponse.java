package uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResponse {

    private List<RoleAssignment> roleAssignmentResponse;

}
