package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class SolicitorEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;

    @JsonProperty("applicantName")
    private final String applicantName;

    @JsonProperty("courtName")
    private final String courtName;

    @JsonProperty("courtEmail")
    private final String courtEmail;

    @JsonProperty("fullName")
    private final String fullName;

    private final String caseLink;

    @JsonProperty("issueDate")
    private final String issueDate;

    @JsonProperty("solicitorName")
    private final String solicitorName;

    @Builder
    public SolicitorEmail(String caseReference,
                          String caseName,
                          String applicantName, String courtName,
                          String courtEmail, String fullName,
                          String caseLink, String issueDate, String solicitorName) {
        super(caseReference);
        this.caseName = caseName;
        this.applicantName = applicantName;
        this.courtName = courtName;
        this.courtEmail = courtEmail;
        this.fullName = fullName;
        this.caseLink = caseLink;
        this.issueDate = issueDate;
        this.solicitorName = solicitorName;
    }
}
