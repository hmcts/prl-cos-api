package uk.gov.hmcts.reform.prl.models.sendandreply;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply;

@Value
@Builder
public class SendAndReplyEventData {

    @JsonProperty("messageObject")
    MessageMetaData messageMetaData;
    String messageContent;
    Object replyMessageDynamicList;
    Message messageReply;
    SendOrReply chooseSendOrReply;

    public static String[] temporaryFields() {
        return new String[]{
            "replyMessageDynamicList", "messageReply", "messageContent",
            "messageReply", "messageMetaData"
        };
    }
}
