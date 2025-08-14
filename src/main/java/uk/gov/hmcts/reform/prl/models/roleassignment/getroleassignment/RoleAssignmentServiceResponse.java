package uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentServiceResponse {

    private List<RoleAssignmentResponse> roleAssignmentResponse;

}
