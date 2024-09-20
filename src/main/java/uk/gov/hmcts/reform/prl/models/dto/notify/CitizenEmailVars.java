package uk.gov.hmcts.reform.prl.models.dto.notify;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * This is a generic citizen gov notify email template variables.
 */
@Data
@Getter
@EqualsAndHashCode(callSuper = true)
public class CitizenEmailVars extends EmailTemplateVars {
    private final String caseName;
    private final String partyName;
    private final String applicantName;
    private final String respondentName;
    private final String caseLink;
    private final String doesC1aExist;

    @Builder
    public CitizenEmailVars(String caseReference,
                            String caseName,
                            String partyName,
                            String applicantName,
                            String respondentName,
                            String caseLink,
                            String doesC1aExist) {

        super(caseReference);
        this.caseName = caseName;
        this.partyName = partyName;
        this.applicantName = applicantName;
        this.respondentName = respondentName;
        this.caseLink = caseLink;
        this.doesC1aExist = doesC1aExist;
    }
}
