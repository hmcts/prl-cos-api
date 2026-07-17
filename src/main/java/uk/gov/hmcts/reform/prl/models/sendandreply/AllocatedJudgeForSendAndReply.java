package uk.gov.hmcts.reform.prl.models.sendandreply;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AllocatedJudgeForSendAndReply {

    @CCD(label = "judgeEmailId", showCondition = "judgeEmailId=\"DO_NOT_SHOW\"", searchable = false)
    private String judgeEmailId;
    @CCD(label = "judgeIdamId", showCondition = "judgeIdamId=\"DO_NOT_SHOW\"", searchable = false)
    private String judgeIdamId;
    @CCD(label = "roleAssignmentId", showCondition = "roleAssignmentId=\"DO_NOT_SHOW\"", searchable = false)
    private String roleAssignmentId;
    @CCD(label = "status", showCondition = "status=\"DO_NOT_SHOW\"", searchable = false)
    private String status;
    @CCD(label = "messageIdentifier", showCondition = "messageIdentifier=\"DO_NOT_SHOW\"", searchable = false)
    private String messageIdentifier;
}
