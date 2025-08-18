package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BarristerEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;
    @JsonProperty("issueDate")
    private final String issueDate;
    @JsonProperty("barristerName")
    private final String barristerName;
    @JsonProperty("caseLink")
    private final String caseLink;
    @JsonProperty("litigantName")
    private final String litigantName;

    @JsonProperty("citizenSignUpLink")
    private final String citizenSignUpLink;

    @Builder
    public BarristerEmail(String caseReference,
                          String caseName,
                          String issueDate,
                          String barristerName,
                          String caseLink,
                          String litigantName,
                          String citizenSignUpLink,
                          String accessCode) {
        super(caseReference);
        this.caseName = caseName;
        this.issueDate = issueDate;
        this.barristerName = barristerName;
        this.caseLink = caseLink;
        this.litigantName = litigantName;
        this.citizenSignUpLink = citizenSignUpLink;
    }
}
