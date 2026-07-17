package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class MiamPolicyUpgradeExemptions {

    @CCD(label = "Reasons for the MIAM exemption", searchable = false)
    private final String mpuReasonsForMiamExemption;
    @CCD(
            label = "What evidence of domestic abuse does the applicant have?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String mpuDomesticAbuseEvidence;
    @CCD(label = "Child protection concerns", searchable = false)
    private final String mpuChildProtectionEvidence;
    @CCD(label = "What reasons does the applicant have for the application to be made urgently?", searchable = false)
    private final String mpuUrgencyEvidence;
    @CCD(label = "Has there been previous attendance of a MIAM or non-court dispute resolution?", searchable = false)
    private final String mpuPreviousAttendenceEvidence;
    @CCD(label = "MIAM evidence - what other grounds of exemption apply?", searchable = false)
    private final String mpuOtherGroundsEvidence;

    @CCD(label = "Can you provide evidence?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo mpuIsDomesticAbuseEvidenceProvided;
    @CCD(label = "What evidence of MIAM attendance are you submitting?", searchable = false)
    private String mpuTypeOfPreviousMiamAttendanceEvidence;

}
