package uk.gov.hmcts.reform.prl.models.sendandreply;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalExternalMessageEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageReplyToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageAboutEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Optional.ofNullable;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Message extends MessageMetaData {

    public static final int MAX_SUBJECT_LENGTH = 100;
    public static final String DOTS = "...";

    @CCD(label = "Date and time sent", searchable = false)
    private String dateSent;
    @CCD(label = "Message details", searchable = false, typeOverride = FieldType.TextArea)
    private String messageContent;
    @CCD(label = "Updated Time", showCondition = "updatedTime=\"DO_NOT_SHOW\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedTime;
    @CCD(label = "Status", searchable = false)
    private MessageStatus status;
    @CCD(
            label = "Latest message",
            showCondition = "status!=\"CLOSED\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String latestMessage;
    @CCD(label = "Message history", searchable = false, typeOverride = FieldType.TextArea)
    private String messageHistory;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isReplying;
    @CCD(label = "Reply from", searchable = false)
    private String replyFrom;
    @CCD(label = "Reply to", searchable = false)
    private String replyTo;

    //PRL-3454 - send & reply message enhancements
    @CCD(label = "Internal or external message", searchable = false)
    private InternalExternalMessageEnum internalOrExternalMessage;
    @CCD(label = "To", searchable = false)
    private InternalMessageWhoToSendToEnum internalMessageWhoToSendTo;
    @CCD(label = "To", searchable = false)
    private String internalOrExternalSentTo;

    @CCD(
            label = " ",
            showCondition = "externalMessageWhoToSendTo=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList
    )
    private DynamicMultiSelectList externalMessageWhoToSendTo;

    //added for reply as there is no "Other" option
    @CCD(label = "To", searchable = false)
    private InternalMessageReplyToEnum internalMessageReplyTo;
    @CCD(label = "What is it about", searchable = false)
    private MessageAboutEnum messageAbout;
    @CCD(
            label = "Judicial or magistrate Tier selected code",
            showCondition = "judicialOrMagistrateTierCode=\"DO_NOT_SHOW\"",
            searchable = false
    )
    private String judicialOrMagistrateTierCode;
    @CCD(label = "Judicial or magistrate Tier", searchable = false)
    private String judicialOrMagistrateTierValue;
    @CCD(label = "Judge name", searchable = false)
    private String judgeName;
    @CCD(label = "CTSC email", showCondition = "selectedCtscEmail=\"DO_NOT_SHOW\"", searchable = false)
    private String selectedCtscEmail;
    @CCD(label = "Recipient email addresses", searchable = false)
    private String recipientEmailAddresses;
    @CCD(label = "Urgency", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo internalMessageUrgent;
    @CCD(
            label = "Application selected code",
            showCondition = "selectedApplicationCode=\"DO_NOT_SHOW\"",
            searchable = false
    )
    private String selectedApplicationCode;
    @CCD(label = "Application", searchable = false)
    private String selectedApplicationValue;
    @CCD(
            label = "Future hearing selected code",
            showCondition = "selectedFutureHearingCode=\"DO_NOT_SHOW\"",
            searchable = false
    )
    private String selectedFutureHearingCode;
    @CCD(label = "Hearing", searchable = false)
    private String selectedFutureHearingValue;
    @CCD(
            label = "submitted document selected code",
            showCondition = "selectedSubmittedDocumentCode=\"DO_NOT_SHOW\"",
            searchable = false
    )
    private String selectedSubmittedDocumentCode;
    @CCD(label = "Document", showCondition = "selectedSubmittedDocumentValue=\"DO_NOT_SHOW\"", searchable = false)
    private String selectedSubmittedDocumentValue;
    @CCD(label = "Document", searchable = false)
    private Document selectedDocument;
    @CCD(label = "Attached documents", searchable = false)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Element<Document>> externalMessageAttachDocs;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo sendMessageToCafcass;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo sendMessageToOtherParties;
    @CCD(label = " ", searchable = false)
    private String cafcassEmailAddress;
    @CCD(label = " ", searchable = false)
    private String otherPartiesEmailAddress;

    // private List<Element<BulkPrintDetails>> messageBulkPrintDetails;

    //@JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = SendReplyJudgeFilter.class)
    @CCD(label = "Enter name of Judge", searchable = false, typeOverride = FieldType.JudicialUser)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private JudicialUser sendReplyJudgeName;
    @CCD(
            label = "Select a judicial tier or enter a judge's name manually",
            hint = "Select Judicial tier",
            searchable = false,
            typeOverride = FieldType.DynamicList
    )
    private DynamicList judicialOrMagistrateTierList;
    @CCD(label = "Applications list", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList applicationsList;
    @CCD(label = "Future hearings list", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList futureHearingsList;
    @CCD(label = "Submitted documents list", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList submittedDocumentsList;
    @CCD(label = "CTSC email", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList ctscEmailList;

    @CCD(label = "Message history", searchable = false)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Element<MessageHistory>> replyHistory;

    @CCD(label = "Judge Email", searchable = false)
    private String judgeEmail;
    @CCD(label = "Sender's name", searchable = false)
    private String senderName;
    @CCD(label = "From", searchable = false)
    private String senderRole;

    @CCD(label = "Hearings link", showCondition = "hearingsLink=\"DO_NOT_SHOW\"", searchable = false)
    private String hearingsLink;

    @CCD(label = "Documents", searchable = false)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Element<Document>> internalMessageAttachDocs;

    @CCD(label = "Name of the legal adviser", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList legalAdviserList;
    @CCD(label = "Legal adviser email", searchable = false)
    private String legalAdviserEmail;
    @CCD(label = "Legal adviser name", searchable = false)
    private String legalAdviserName;

    @JsonIgnore
    public String getLabelForDynamicList() {
        return String.format(
            "%s, %s, %s",
            super.getMessageSubject(),
            this.dateSent,
            ofNullable(super.getMessageUrgency()).orElse("")
        );
    }

    @JsonIgnore
    public String getLabelForReplyDynamicList() {
        String messageSubject = super.getMessageSubject();
        String subject = "Subject: " + (messageSubject.length() > MAX_SUBJECT_LENGTH ? messageSubject.substring(
            0, MAX_SUBJECT_LENGTH - DOTS.length()) + DOTS : messageSubject);
        String sender = "From: " + senderName;
        return String.format(
            "%s, %s, %s %s",
            sender,
            subject,
            this.dateSent,
            YesOrNo.Yes.equals(this.internalMessageUrgent) ? "Urgent" : "Not Urgent"
        );

    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "This message will now be marked as closed",
          showCondition = "closeMessageLabel=\"DO_NOT_SHOW\"",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String closeMessageLabel;
  @CCD(label = "Other application link", showCondition = "otherApplicationLink=\"DO_NOT_SHOW\"", searchable = false)
  private String otherApplicationLink;
  @CCD(label = "**Your message**", searchable = false, typeOverride = FieldType.Label)
  private String yourMessageLabel;
  // ==== end synthesised definition-only fields ====
}
