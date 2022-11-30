package uk.gov.hmcts.reform.prl.models.dto.notify;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CitizenCaseSubmissionEmail extends EmailTemplateVars {
    private final String caseNumber;
    private final String caseLink;
    private final String applicantName;

    @Builder
    public CitizenCaseSubmissionEmail(String caseNumber, String caseLink, String applicantName) {
        this.caseNumber = caseNumber;
        this.caseLink = caseLink;
        this.applicantName = applicantName;
    }

}
