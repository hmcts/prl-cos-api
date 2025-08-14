package uk.gov.hmcts.reform.prl.utils.noticeofchange;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeParties;

@Component
public class NoticeOfChangePartiesConverter {
    public NoticeOfChangeParties generateCaForSubmission(Element<? extends PartyDetails> element) {
        PartyDetails partyDetails = element.getValue();

        return NoticeOfChangeParties.builder()
            .firstName(partyDetails.getFirstName())
            .lastName(partyDetails.getLastName())
            .build();
    }

    public NoticeOfChangeParties generateDaForSubmission(PartyDetails partyDetails) {
        return NoticeOfChangeParties.builder()
            .firstName(partyDetails.getFirstName())
            .lastName(partyDetails.getLastName())
            .build();
    }
}
