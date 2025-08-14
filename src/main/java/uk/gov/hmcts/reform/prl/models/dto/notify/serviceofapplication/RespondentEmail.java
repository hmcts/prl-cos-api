package uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;

@Data
@Getter
@EqualsAndHashCode(callSuper = true)
public class RespondentEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;
    @JsonProperty("respondentName")
    private final String respondentName;
    @JsonProperty("applicantNames")
    private final String applicantNames;
    @JsonProperty("createLink")
    private final String createLink;
    @JsonProperty("accessCode")
    private final String accessCode;

    @Builder
    public RespondentEmail(String caseReference,
                           String caseName,
                           String respondentName,
                           String applicantNames,
                           String createLink,
                           String accessCode) {
        super(caseReference);
        this.caseName = caseName;
        this.respondentName = respondentName;
        this.applicantNames = applicantNames;
        this.createLink = createLink;
        this.accessCode = accessCode;
    }
}
