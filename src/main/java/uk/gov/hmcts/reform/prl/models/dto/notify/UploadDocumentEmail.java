package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@Data
@Getter
@EqualsAndHashCode(callSuper = true)
public class UploadDocumentEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;
    @JsonProperty("dashboardLink")
    private final String dashboardLink;
    @JsonProperty("name")
    private final String name;

    @Builder
    public UploadDocumentEmail(String caseReference,
                               String caseName,
                               String dashboardLink,
                               String name) {

        super(caseReference);
        this.caseName = caseName;
        this.dashboardLink = dashboardLink;
        this.name = name;
    }
}
