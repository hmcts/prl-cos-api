package uk.gov.hmcts.reform.prl.models.sendandreply;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
public class MessageHistory {

    private String messageFrom;
    private String messageTo;
    private String messageDate;
    private YesOrNo isUrgent;
}
