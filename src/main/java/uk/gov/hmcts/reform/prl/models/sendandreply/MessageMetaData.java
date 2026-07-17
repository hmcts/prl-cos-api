package uk.gov.hmcts.reform.prl.models.sendandreply;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class MessageMetaData {

    @CCD(label = "Sender’s email address", searchable = false, typeOverride = FieldType.Email)
    private  String senderEmail;
    @CCD(label = "Recipient’s email address", searchable = false, typeOverride = FieldType.Email)
    private  String recipientEmail;
    @CCD(label = "Message subject", searchable = false)
    private  String messageSubject;
    @CCD(
            label = "Urgency",
            hint = "Add if it’s urgent, or if a response is requested within a specified time.",
            searchable = false
    )
    private  String messageUrgency;
    @CCD(label = "Urgency", hint = "message identifier used for storing identifier in backend", searchable = false)
    private  String messageIdentifier;

}
