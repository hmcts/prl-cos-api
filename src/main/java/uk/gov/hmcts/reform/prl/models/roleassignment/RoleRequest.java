package uk.gov.hmcts.reform.prl.models.roleassignment;

import lombok.Data;

@Data
public class RoleRequest {

    private String assignerId;
    private String process;
    private String reference;
    private Boolean replaceExisting;
}
