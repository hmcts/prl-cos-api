package uk.gov.hmcts.reform.prl.models.complextypes;

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
public class RespChildAbuse {

    @JsonProperty("respAbuseNatureDescription")
    private String respAbuseNatureDescription;

    @JsonProperty("respBehavioursStartDateAndLength")
    private String respBehavioursStartDateAndLength;

    @JsonProperty("respBehavioursApplicantSoughtHelp")
    private YesOrNo respBehavioursApplicantSoughtHelp;

    @JsonProperty("respBehavioursApplicantHelpSoughtWho")
    private String respBehavioursApplicantHelpSoughtWho;

}
