package uk.gov.hmcts.reform.prl.models.sendandreply;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class MessageHistory {

    @CCD(label = "Sender's email", searchable = false)
    private String messageFrom;
    @CCD(label = "Sender's name", searchable = false)
    private String senderName;
    @CCD(label = "Sender role", searchable = false)
    private String senderRole;
    @CCD(label = "To", searchable = false)
    private String messageTo;
    @CCD(label = "Date and time sent", searchable = false)
    private String messageDate;
    @CCD(label = "Urgency", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo isUrgent;
    @CCD(label = "Message subject", searchable = false)
    private String messageSubject;
    @CCD(label = "Message", searchable = false, typeOverride = FieldType.TextArea)
    private String messageContent;
    @CCD(
            label = "Who are you sending the message to?",
            showCondition = "internalOrExternalMessage=\"DO_NOT_SHOW\"",
            searchable = false
    )
    private String internalOrExternalMessage;
    @CCD(label = "Who to send to", showCondition = "internalMessageWhoToSendTo=\"DO_NOT_SHOW\"", searchable = false)
    private String internalMessageWhoToSendTo;
    @CCD(label = "What is it about", searchable = false)
    private String messageAbout;
    @CCD(label = "Judicial or magistrate Tier", searchable = false)
    private String judicialOrMagistrateTierValue;
    @CCD(label = "Judge Name", searchable = false)
    private String judgeName;
    @CCD(label = "CTSC email", showCondition = "selectedCtscEmail=\"DO_NOT_SHOW\"", searchable = false)
    private String selectedCtscEmail;
    @CCD(
            label = "Recipient email addresses",
            showCondition = "recipientEmailAddresses=\"DO_NOT_SHOW\"",
            searchable = false
    )
    private String recipientEmailAddresses;
    @CCD(label = "Application", searchable = false)
    private String selectedApplicationValue;
    @CCD(label = "Selected Future Hearing", searchable = false)
    private String selectedFutureHearingValue;
    @CCD(
            label = "Submitted Document",
            showCondition = "selectedSubmittedDocumentValue=\"DO_NOT_SHOW\"",
            searchable = false
    )
    private String selectedSubmittedDocumentValue;
    @CCD(label = "Document", searchable = false)
    private Document selectedDocument;
    @CCD(label = "Judge Email", searchable = false)
    private String judgeEmail;
    @CCD(label = "Legal adviser name", searchable = false)
    private String legalAdviserName;
    @CCD(label = "Legal adviser email", searchable = false)
    private String legalAdviserEmail;
    @CCD(label = "Hearings link", showCondition = "hearingsLink=\"DO_NOT_SHOW\"", searchable = false)
    private String hearingsLink;
    @CCD(label = "Updated Time", showCondition = "updatedTime=\"DO_NOT_SHOW\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedTime;
    @CCD(label = "Attached documents", searchable = false)
    private List<Element<Document>> externalMessageAttachDocs;

    @CCD(label = "Documents", searchable = false)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Element<Document>> internalMessageAttachDocs;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Sender role", searchable = false)
  private String internalOrExternalSentTo;
  @CCD(
          label = "Selected application code",
          showCondition = "selectedApplicationCode=\"DO_NOT_SHOW\"",
          searchable = false
  )
  private String selectedApplicationCode;
  @CCD(label = "Other application link", showCondition = "otherApplicationLink=\"DO_NOT_SHOW\"", searchable = false)
  private String otherApplicationLink;
  // ==== end synthesised definition-only fields ====
}
