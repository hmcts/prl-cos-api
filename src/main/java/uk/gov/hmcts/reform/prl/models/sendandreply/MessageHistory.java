package uk.gov.hmcts.reform.prl.models.sendandreply;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class MessageHistory {

    private String messageFrom;
    private String senderName;
    private String senderRole;
    private String messageTo;
    private String messageDate;
    private YesOrNo isUrgent;
    private String messageSubject;
    private String messageContent;
    private String internalOrExternalMessage;
    private String internalMessageWhoToSendTo;
    private String messageAbout;
    private String judicialOrMagistrateTierValue;
    private String judgeName;
    private String selectedCtscEmail;
    private String recipientEmailAddresses;
    private String selectedApplicationValue;
    private String selectedFutureHearingValue;
    private String selectedSubmittedDocumentValue;
    private Document selectedDocument;
    private String judgeEmail;
    private String otherApplicationLink;
    private String hearingsLink;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedTime;

}
