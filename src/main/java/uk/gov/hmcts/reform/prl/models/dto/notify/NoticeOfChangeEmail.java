package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeOfChangeEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;
    @JsonProperty("issueDate")
    private final String issueDate;
    @JsonProperty("solicitorName")
    private final String solicitorName;
    @JsonProperty("caseLink")
    private final String caseLink;
    @JsonProperty("litigantName")
    private final String litigantName;

    @Builder
    public NoticeOfChangeEmail(String caseReference,
                               String caseName,
                               String issueDate,
                               String solicitorName,
                               String caseLink,
                               String litigantName) {
        super(caseReference);
        this.caseName = caseName;
        this.issueDate = issueDate;
        this.solicitorName = solicitorName;
        this.caseLink = caseLink;
        this.litigantName = litigantName;
    }
}
