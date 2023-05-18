package uk.gov.hmcts.reform.prl.models.sendandreply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalExternalMessageEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageAboutEnum;
import uk.gov.hmcts.reform.prl.models.documents.Document;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class MessageHistory {

    private String messageFrom;
    private String messageTo;
    private String messageDate;
    private YesOrNo isUrgent;
    private  String messageSubject;
    private String messageContent;
    private InternalExternalMessageEnum internalOrExternalMessageEnum;
    private InternalMessageWhoToSendToEnum internalMessageWhoToSendToEnum;
    private MessageAboutEnum messageAboutEnum;
    private String judicialOrMagistrateTierValue;
    private String judgeName;
    private String selectedCtscEmail;
    private String recipientEmailAddresses;
    private String selectedApplicationValue;
    private String selectedFutureHearingValue;
    private String selectedSubmittedDocumentValue;
    private Document selectedDocument;
}
