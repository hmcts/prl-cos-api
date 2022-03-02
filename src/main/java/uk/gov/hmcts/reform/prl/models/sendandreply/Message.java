package uk.gov.hmcts.reform.prl.models.sendandreply;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus;

import java.time.LocalDateTime;

import static java.util.Optional.ofNullable;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class Message extends MessageMetaData {

    private String dateSent;
    private String messageContent;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
    private MessageStatus status;
    private String latestMessage;
    private String messageHistory;
    private YesOrNo isReplying;
    private String replyFrom;
    private String replyTo;


    @JsonIgnore
    public String getLabelForDynamicList() {
        return String.format(
            "%s, %s, %s",
            super.getMessageSubject(),
            this.dateSent,
            ofNullable(super.getMessageUrgency()).orElse("")
        );
    }
}

