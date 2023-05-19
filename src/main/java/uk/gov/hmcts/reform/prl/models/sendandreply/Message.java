package uk.gov.hmcts.reform.prl.models.sendandreply;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalExternalMessageEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageAboutEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Optional.ofNullable;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class Message extends MessageMetaData {

    private String dateSent;
    private String messageContent;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedTime;
    private MessageStatus status;
    private String latestMessage;
    private String messageHistory;
    private YesOrNo isReplying;
    private String replyFrom;
    private String replyTo;

    //PRL-3454 - send & reply message enhancements
    private InternalExternalMessageEnum internalOrExternalMessage;
    private InternalMessageWhoToSendToEnum internalMessageWhoToSendTo;
    private MessageAboutEnum messageAbout;
    private String judicialOrMagistrateTierCode;
    private String judicialOrMagistrateTierValue;
    private String judgeName;
    private String selectedCtscEmail;
    private String recipientEmailAddresses;
    private YesOrNo internalMessageUrgent;
    private String selectedApplicationCode;
    private String selectedApplicationValue;
    private String selectedFutureHearingCode;
    private String selectedFutureHearingValue;
    private String selectedSubmittedDocumentCode;
    private String selectedSubmittedDocumentValue;
    private Document selectedDocument;

    private JudicialUser sendReplyJudgeName;
    private DynamicList judicialOrMagistrateTierList;
    private DynamicList applicationsList;
    private DynamicList futureHearingsList;
    private DynamicList submittedDocumentsList;
    private DynamicList ctscEmailList;

    private List<Element<MessageHistory>> replyHistory;

    @JsonIgnore
    public String getLabelForDynamicList() {
        return String.format(
            "%s, %s, %s",
            super.getMessageSubject(),
            this.dateSent,
            ofNullable(super.getMessageUrgency()).orElse(internalMessageUrgent.getDisplayedValue())
        );
    }
}

