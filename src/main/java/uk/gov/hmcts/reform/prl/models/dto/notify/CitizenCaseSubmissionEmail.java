package uk.gov.hmcts.reform.prl.models.dto.notify;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CitizenCaseSubmissionEmail extends EmailTemplateVars {
    private final String caseNumber;
    private final String caseLink;
    private final String applicantName;

    private final String caseName;

    @Builder
    public CitizenCaseSubmissionEmail(String caseNumber, String caseLink, String applicantName, String caseName) {
        this.caseNumber = caseNumber;
        this.caseLink = caseLink;
        this.applicantName = applicantName;
        this.caseName = caseName;
    }

}
