package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChildAbuseBehaviour {

    @CCD(label = "Type of ChildAbuseBehaviour", searchable = false, typeOverride = FieldType.Text)
    @JsonProperty("typeOfAbuse")
    private ChildAbuseEnum typeOfAbuse;

    @CCD(
            label = "Describe the nature of the behaviour, what happened and who was involved.",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("abuseNatureDescription")
    private String abuseNatureDescription;

    @CCD(
            label = "When did the behaviour start and how long did it continue? \n (Does not need to be exact date and indicate if abuse is ongoing).",
            searchable = false
    )
    @JsonProperty("behavioursStartDateAndLength")
    private String behavioursStartDateAndLength;

    @CCD(label = "Did the applicant seek help?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("behavioursApplicantSoughtHelp")
    private YesOrNo behavioursApplicantSoughtHelp;

    @CCD(
            label = "Who did they seek help from, and what they did to help?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("behavioursApplicantHelpSoughtWho")
    private String behavioursApplicantHelpSoughtWho;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("allChildrenAreRisk")
    private YesOrNo allChildrenAreRisk;

    @CCD(label = " ", searchable = false)
    @JsonProperty("whichChildrenAreRisk")
    private String whichChildrenAreRisk;
}
