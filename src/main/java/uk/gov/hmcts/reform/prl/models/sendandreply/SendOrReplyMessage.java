package uk.gov.hmcts.reform.prl.models.sendandreply;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;

import java.util.List;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class SendOrReplyMessage {

    //PRL-3454 - send & reply messages enhancements
    @JsonProperty("messages")
    private final List<Element<Message>> messages;

    private DynamicList messageReplyDynamicList;
    private YesOrNo respondToMessage;
    private String messageReplyTable;
    private Message sendMessageObject;
    private Message replyMessageObject;

    public static String[] temporaryFieldsAboutToStart() {
        return new String[]{
            "messageContent", "respondToMessage",
            "messageMetaData", "messageReplyDynamicList", "sendMessageObject",
            "replyMessageObject", "messageReplyTable", "chooseSendOrReply"
        };
    }

    public static String[] temporaryFieldsAboutToSubmit() {
        return new String[]{
            "messageContent",
            "messageMetaData", "messageReplyDynamicList", "messageReplyTable"
        };
    }
}

