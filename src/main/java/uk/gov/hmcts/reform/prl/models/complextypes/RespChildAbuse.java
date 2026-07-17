package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RespChildAbuse {

    @CCD(
            label = "Describe the nature of the behaviour, what happened and who was involved.",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("respAbuseNatureDescription")
    private String respAbuseNatureDescription;

    @CCD(
            label = "When did the behaviour start and how long did it continue? \n (Does not need to be exact date and indicate if abuse is ongoing).",
            searchable = false
    )
    @JsonProperty("respBehavioursStartDateAndLength")
    private String respBehavioursStartDateAndLength;

    @CCD(label = "Did the respondent seek help?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("respBehavioursApplicantSoughtHelp")
    private YesOrNo respBehavioursApplicantSoughtHelp;

    @CCD(
            label = "Who did they seek help from, and what they did to help?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("respBehavioursApplicantHelpSoughtWho")
    private String respBehavioursApplicantHelpSoughtWho;

}
