package uk.gov.hmcts.reform.prl.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.sendandreply.AllocatedJudgeForSendAndReply;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageMetaData;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class SendOrReplyDto {
    @JsonProperty("openMessages")
    private final List<Element<Message>> openMessages;

    @JsonProperty("closedMessages")
    private final List<Element<Message>> closedMessages;

    @JsonProperty("allocatedJudgeForSendAndReply")
    private final List<Element<AllocatedJudgeForSendAndReply>> allocatedJudgeForSendAndReply;

    @JsonProperty("messageObject")
    MessageMetaData messageMetaData;
}
