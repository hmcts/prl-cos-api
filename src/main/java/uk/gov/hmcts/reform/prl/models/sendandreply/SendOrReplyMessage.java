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

    @JsonProperty("sendReplyTempDocs4")
    private final List<Element<SendReplyTempDoc>> sendReplyTempDocs4;

    @JsonProperty("sendReplyTempDocs5")
    private final List<Element<SendReplyTempDoc>> sendReplyTempDocs5;

    @JsonProperty("replyDocuments")
    private List<Element<ReplyDocument>> replyDocuments;

    public static String[] temporaryFieldsAboutToStart() {
        return new String[]{
            "messageContent", "respondToMessage", "replyDocuments",
            "messageMetaData", "messageReplyDynamicList", "sendMessageObject",
            "replyMessageObject", "messageReplyTable", "chooseSendOrReply",
            "sendReplyTempDocs4", "sendReplyTempDocs5"
        };
    }

    public static String[] temporaryFieldsAboutToSubmit() {
        return new String[]{
            "messageContent", "replyDocuments",
            "messageMetaData", "messageReplyDynamicList", "messageReplyTable",
            "sendReplyTempDocs4", "sendReplyTempDocs5"
        };
    }
}

