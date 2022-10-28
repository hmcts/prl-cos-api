package uk.gov.hmcts.reform.prl.services.noc;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.NoticeOfChangeAnswers;

@Component
public class NoticeOfChangeAnswersConverter {
    public NoticeOfChangeAnswers generateForSubmission(Element<? extends PartyDetails> respondentElement,
                                                       String applicantName) {

        return NoticeOfChangeAnswers.builder()
            .respondentFirstName(respondentElement.getValue().getFirstName())
            .respondentLastName(respondentElement.getValue().getLastName())
            .applicantName(applicantName)
            .build();
    }
}
