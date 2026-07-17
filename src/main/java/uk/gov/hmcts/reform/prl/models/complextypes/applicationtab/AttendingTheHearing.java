package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class AttendingTheHearing {

    @CCD(
            label = "Will the applicant, or anyone else attending court, want to speak Welsh or read and write in Welsh during the proceedings?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isWelshNeeded;
    @CCD(
            label = "Do you know if an interpreter will be needed in the court to explain information in a certain language?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isInterpreterNeeded;
    @CCD(
            label = "Does the applicant, or anyone else attending court have a disability?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isDisabilityPresent;
    @CCD(
            label = "*Describe the adjustments that the court needs to make",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String adjustmentsRequired;
    @CCD(
            label = "Will the court need to make special arrangements for the applicant, or any child involved in the case?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isSpecialArrangementsRequired;
    @CCD(
            label = "*Give details of the special arrangements that are required",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String specialArrangementsRequired;
    @CCD(
            label = "Do you know if an intermediary will be required?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isIntermediaryNeeded;
    @CCD(
            label = "*Set out the reasons that an intermediary is required.",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String reasonsForIntermediary;

}
