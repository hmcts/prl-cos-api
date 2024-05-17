package uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Schema(description = "The response object for RoleAssignment")
@NoArgsConstructor
@AllArgsConstructor
public class RoleAssignmentResponse {

    private String actorId;
    private String actorIdType;
    private String roleType;
    private String roleName;
    private String classification;
    private String grantType;
    private String roleCategory;
    private Boolean readOnly;
    private LocalDate beginTime;
    private LocalDate created;
    private Attributes attributes;
}
