package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.RespondentWelshNeedsListEnum;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class AttendToCourt {
    @CCD(
            label = "*Will the respondent or anyone else attending the court want to speak Welsh or read and write in Welsh during the proceedings?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo respondentWelshNeeds;
    @CCD(label = " ", showCondition = "respondentWelshNeeds=\"Yes\"", searchable = false)
    private final List<RespondentWelshNeedsListEnum> respondentWelshNeedsList;
    @CCD(
            label = "*Do you know if an interpreter will be needed in the court to explain information in a certain language?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isRespondentNeededInterpreter;
    @CCD(label = "Interpreter needs", showCondition = "isRespondentNeededInterpreter=\"Yes\"", searchable = false)
    private final List<Element<RespondentInterpreterNeeds>> respondentInterpreterNeeds;
    @CCD(
            label = "*Does the respondent, or anyone else attending court have a disability?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo haveAnyDisability;
    @CCD(
            label = "*Describe the adjustments that the court needs to make.\n\nFor example - someone with a hearing impairment may need an induction loop to be fitted in the courtroom.",
            showCondition = "haveAnyDisability=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String disabilityNeeds;
    @CCD(
            label = "*In some cases, the court can make special arrangements for an adult or child involved in the case. \nFor example - the court may provide a separate waiting room that is set apart from the applicant.\n\nWill the court need to make special arrangements for the respondent, or any child involved in the case?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo respondentSpecialArrangements;
    @CCD(
            label = "*Give details of the special arrangements that are required.  \nFor example, a screen to separate the applicant from the respondent.",
            showCondition = "respondentSpecialArrangements=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String respondentSpecialArrangementDetails;
    @CCD(
            label = "*The court can appoint an intermediary for a vulnerable respondent.  \nThe intermediary helps the respondent to communicate and give evidence during the case.\n\nDo you know if an intermediary will be required?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo respondentIntermediaryNeeds;
    @CCD(
            label = "*Set out the reasons that an intermediary is required.",
            showCondition = "respondentIntermediaryNeeds=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String respondentIntermediaryNeedDetails;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "### Language requirements", searchable = false, typeOverride = FieldType.Label)
  private String languageRequirementsLabel;
  @CCD(label = "### Accessibility", searchable = false, typeOverride = FieldType.Label)
  private String accessibilityLabel;
  @CCD(label = "### Special arrangements", searchable = false, typeOverride = FieldType.Label)
  private String specialArrangementsLabel;
  // ==== end synthesised definition-only fields ====
}
