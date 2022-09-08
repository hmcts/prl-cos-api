package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class CitizenEmail extends EmailTemplateVars {

    @JsonProperty("petitionerName")
    private final String petitionerName;

    @JsonProperty("respondentName")
    private final String respondentName;

    @JsonProperty("dashboardLink")
    private final String dashboardLink;

    @JsonProperty("caseName")
    private final String caseName;

    @Builder
    public CitizenEmail(String caseReference, String petitionerName, String respondentName,String dashboardLink,
                        String caseName) {
        super(caseReference);
        this.petitionerName = petitionerName;
        this.respondentName = respondentName;
        this.dashboardLink = dashboardLink;
        this.caseName = caseName;
    }
}
