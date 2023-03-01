package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildAbuse {

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

    @Builder.Default
    @JsonProperty("whichChildrenAreRisk")
    private DynamicMultiSelectList whichChildrenAreRisk;

}
