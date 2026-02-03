package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LaSocialWorkerEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;
    @JsonProperty("issueDate")
    private final String issueDate;
    @JsonProperty("socialWorkerName")
    private final String socialWorkerName;
    @JsonProperty("caseLink")
    private final String caseLink;

    @Builder
    public LaSocialWorkerEmail(String caseReference,
                               String caseName,
                               String issueDate,
                               String socialWorkerName,
                               String caseLink) {
        super(caseReference);
        this.caseName = caseName;
        this.issueDate = issueDate;
        this.socialWorkerName = socialWorkerName;
        this.caseLink = caseLink;
    }
}
