package uk.gov.hmcts.reform.prl.models.sendandreply;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AllocatedUserForSendAndReply {
    private String idamId;
    private String roleName;
    private String messageIdentifier;
}
