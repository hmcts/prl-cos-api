package uk.gov.hmcts.reform.prl.models.roleassignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.Status;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "roleRequest")
public class RoleRequest {

    private String assignerId;
    private String authenticatedUserId;
    private Boolean byPassOrgDroolRule;
    private String clientId;
    private String correlationId;
    private LocalDateTime created;
    private String id;
    private String log;
    private String process;
    private String reference;
    private Boolean replaceExisting;
    private String requestType;
    private String roleAssignmentId;
    private Status status;
}
