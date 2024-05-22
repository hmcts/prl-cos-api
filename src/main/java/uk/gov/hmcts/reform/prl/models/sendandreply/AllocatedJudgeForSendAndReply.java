package uk.gov.hmcts.reform.prl.models.sendandreply;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AllocatedJudgeForSendAndReply {

    private String judgeId;
    private String judgeEmailId;
    private String roleAssignmentId;
    private String status;
}
