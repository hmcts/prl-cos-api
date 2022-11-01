package uk.gov.hmcts.reform.prl.models.dto.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@Getter
@EqualsAndHashCode(callSuper = true)
public class HearingDetailsEmail extends EmailTemplateVars {

    @JsonProperty("caseName")
    private final String caseName;

    @JsonProperty("partyName")
    private final String partyName;

    @JsonProperty("hearingDetailsPageLink")
    private final String hearingDetailsPageLink;

    @Builder
    public HearingDetailsEmail(String caseReference,
                               String caseName,
                               String partyName,
                               String hearingDetailsPageLink) {

        super(caseReference);
        this.caseName = caseName;
        this.partyName = partyName;
        this.hearingDetailsPageLink = hearingDetailsPageLink;
    }
}
