package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class SendAndReplyNotificationEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;

    @JsonProperty("messageSubject")
    private final String messageSubject;

    @JsonProperty("senderEmail")
    private final String senderEmail;

    @JsonProperty("messageUrgency")
    private final String messageUrgency;

    @JsonProperty("messageContent")
    private final String messageContent;

    private final String caseLink;

    @Builder
    public SendAndReplyNotificationEmail(String caseName,
                                         String messageSubject,
                                         String senderEmail,
                                         String messageUrgency,
                                         String messageContent,
                                         String caseLink,
                                         String caseReference) {
        super(caseReference);
        this.caseName = caseName;
        this.messageSubject = messageSubject;
        this.senderEmail = senderEmail;
        this.messageUrgency = messageUrgency;
        this.messageContent = messageContent;
        this.caseLink = caseLink;
    }
}
