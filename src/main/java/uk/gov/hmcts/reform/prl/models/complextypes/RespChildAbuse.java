package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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
