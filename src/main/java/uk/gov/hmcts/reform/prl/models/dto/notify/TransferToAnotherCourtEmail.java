package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransferToAnotherCourtEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;

    @JsonProperty("issueDate")
    private final String issueDate;

    @JsonProperty("courtName")
    private final String courtName;

    @JsonProperty("confidentialityText")
    private final String confidentialityText;

    @JsonProperty("applicationType")
    private final String applicationType;

    @Builder
    public TransferToAnotherCourtEmail(String caseReference,
                                       String caseName,
                                       String issueDate,
                                       String confidentialityText,
                                       String courtName,
                                       String applicationType) {
        super(caseReference);
        this.caseName = caseName;
        this.issueDate = issueDate;
        this.courtName = courtName;
        this.confidentialityText = confidentialityText;
        this.applicationType = applicationType;
    }
}
