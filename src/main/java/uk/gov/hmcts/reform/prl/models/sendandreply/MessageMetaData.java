package uk.gov.hmcts.reform.prl.models.sendandreply;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class MessageMetaData {

    private  String senderEmail;
    private  String recipientEmail;
    private  String messageSubject;
    private  String messageUrgency;
    private  String messageIdentifier;

}
