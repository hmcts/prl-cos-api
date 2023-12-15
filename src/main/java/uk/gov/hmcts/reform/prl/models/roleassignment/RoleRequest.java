package uk.gov.hmcts.reform.prl.models.roleassignment;

import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Status;

import java.time.LocalDateTime;

@Data
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
