package uk.gov.hmcts.reform.prl.utils.noticeofchange;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeParties;

@Component
public class NoticeOfChangePartiesConverter {
    public NoticeOfChangeParties generateForSubmission(Element<? extends PartyDetails> element) {
        PartyDetails respondent = element.getValue();

        return NoticeOfChangeParties.builder()
            .respondentFirstName(respondent.getFirstName())
            .respondentLastName(respondent.getLastName())
            .build();
    }
}
