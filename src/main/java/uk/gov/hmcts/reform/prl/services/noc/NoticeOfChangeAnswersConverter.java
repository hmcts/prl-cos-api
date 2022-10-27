package uk.gov.hmcts.reform.prl.services.noc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.WithSolicitor;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.NoticeOfChangeAnswers;

@Component
public class NoticeOfChangeAnswersConverter {
    public NoticeOfChangeAnswers generateForSubmission(Element<? extends WithSolicitor> respondentElement,
                                                       String applicantName) {
        PartyDetails respondentParty = respondentElement.getValue().toParty();

        return NoticeOfChangeAnswers.builder()
            .respondentFirstName(respondentParty.getFirstName())
            .respondentLastName(respondentParty.getLastName())
            .applicantName(applicantName)
            .build();
    }
}
