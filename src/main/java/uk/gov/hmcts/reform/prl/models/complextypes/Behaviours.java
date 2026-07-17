package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.AbuseTypes;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class Behaviours {

    @CCD(
            label = "*Describe the nature of the abuse, what happened and who was involved.",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String abuseNatureDescription;
    @CCD(
            label = "*When did the Behaviours start and how long did it continue? \n(Does not need to be exact date and indicate if abuse is ongoing).",
            searchable = false
    )
    private String behavioursStartDateAndLength;
    @CCD(label = "*Nature of Behaviours/what happened", searchable = false, typeOverride = FieldType.TextArea)
    private String behavioursNature;
    @CCD(label = "*Did the applicant seek help?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo behavioursApplicantSoughtHelp;
    @CCD(label = "*Who did they seek help from?", searchable = false, typeOverride = FieldType.TextArea)
    private String behavioursApplicantHelpSoughtWho;
    @CCD(
            label = "*Did they do anything? If yes, what did they do?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String behavioursApplicantHelpAction;

    @CCD(
            label = "*Type of abuse",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "AbuseTypes"
    )
    private AbuseTypes typesOfAbuse;
    @CCD(
            label = "*Describe the nature of the behaviour, what happened and who was involved.",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String natureOfBehaviour;
    @CCD(
            label = "*When did the behaviour start and how long did it continue? \n(Does not need to be exact date and indicate if abuse is ongoing)",
            searchable = false
    )
    private String abuseStartDateAndLength;
    @CCD(label = "*Did the respondent seek help?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo respondentSoughtHelp;
    @CCD(
            label = "*Who did they seek help from, and what they did to help?",
            showCondition = "respondentSoughtHelp=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String respondentTypeOfHelp;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Give a short description of what happened and any relevant information so the court can decide what needs to be done.\n   ",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String behavioursDescriptionLabel;
  @CCD(label = "## Add new behaviour", searchable = false, typeOverride = FieldType.Label)
  private String addNewBehaviourLabel;
  // ==== end synthesised definition-only fields ====
}
