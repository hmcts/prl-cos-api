package uk.gov.hmcts.reform.prl.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.sendandreply.AllocatedJudgeForSendAndReply;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageMetaData;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus2RolesQnvhwhAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess;

@Data
@Builder(toBuilder = true)
public class SendOrReplyDto {
    @CCD(
            label = "Message",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus2RolesQnvhwhAccess.class, CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCuAccess.class}
    )
    @JsonProperty("openMessages")
    private final List<Element<Message>> openMessages;

    @CCD(
            label = "Message",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus2RolesQnvhwhAccess.class, CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCuAccess.class}
    )
    @JsonProperty("closedMessages")
    private final List<Element<Message>> closedMessages;

    @CCD(
            label = "AllocatedJudgeForSendAndReply",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class, CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
    )
    @JsonProperty("allocatedJudgeForSendAndReply")
    private final List<Element<AllocatedJudgeForSendAndReply>> allocatedJudgeForSendAndReply;

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess.class, CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess.class}
    )
    @JsonProperty("messageObject")
    MessageMetaData messageMetaData;
}
