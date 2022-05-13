package uk.gov.hmcts.reform.prl.models.dto.notify;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ManageOrderEmail extends EmailTemplateVars {


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


    @Builder
    public ManageOrderEmail(String caseReference,
                            String caseName,
                            String applicantName, String courtName,
                            String courtEmail, String firstName,
                            String lastName, String fullName,
                            String caseLink) {
        super(caseReference);
        this.caseName = caseName;
        this.applicantName = applicantName;
        this.courtName = courtName;
        this.courtEmail = courtEmail;
        this.fullName = fullName;
        this.caseLink = caseLink;
    }
}
