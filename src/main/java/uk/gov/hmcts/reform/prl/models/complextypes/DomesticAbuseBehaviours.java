package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.TypeOfAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "DomesticBehaviours", generate = true)
@Data
@Builder
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DomesticAbuseBehaviours {

    @CCD(label = "*Type of abuse", searchable = false)
    private TypeOfAbuseEnum typeOfAbuse;

    @CCD(
            label = "*Describe the nature of the behaviour, what happened and who was involved.",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String newAbuseNatureDescription;

    @CCD(
            label = "*When did the behaviour start and how long did it continue? \n(Does not need to be exact date and indicate if abuse is ongoing).",
            searchable = false
    )
    private String newBehavioursStartDateAndLength;

    @CCD(label = "*Did the applicant seek help?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo newBehavioursApplicantSoughtHelp;

    @CCD(
            label = "*Who did they seek help from, and what they did to help?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String newBehavioursApplicantHelpSoughtWho;


  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "## Add another behaviour", searchable = false, typeOverride = FieldType.Label)
  private String addNewDomesticBehavioursLabel;
  // ==== end synthesised definition-only fields ====
}
