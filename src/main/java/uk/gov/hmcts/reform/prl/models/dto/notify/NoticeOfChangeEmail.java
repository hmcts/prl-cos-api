package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeOfChangeEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;
    @JsonProperty("issueDate")
    private final LocalDate issueDate;
    @JsonProperty("solicitorName")
    private final String solicitorName;
    @JsonProperty("caseLink")
    private final String caseLink;


    @Builder
    public NoticeOfChangeEmail(String caseReference,
                               String caseName,
                               LocalDate issueDate,
                               String solicitorName,
                               String caseLink) {
        super(caseReference);
        this.caseName = caseName;
        this.issueDate = issueDate;
        this.solicitorName = solicitorName;
        this.caseLink = caseLink;
    }
}
