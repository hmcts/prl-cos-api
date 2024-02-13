package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespChildAbuseBehaviour {

    @JsonProperty("typeOfAbuse")
    private String typeOfAbuse;

    @JsonProperty("abuseNatureDescription")
    private String abuseNatureDescription;

    @JsonProperty("behavioursStartDateAndLength")
    private String behavioursStartDateAndLength;

    @JsonProperty("behavioursApplicantSoughtHelp")
    private YesOrNo behavioursApplicantSoughtHelp;

    @JsonProperty("behavioursApplicantHelpSoughtWho")
    private String behavioursApplicantHelpSoughtWho;

    @JsonProperty("allChildrenAreRisk")
    private YesOrNo allChildrenAreRisk;

    @JsonProperty("whichChildrenAreRisk")
    private String whichChildrenAreRisk;
}
