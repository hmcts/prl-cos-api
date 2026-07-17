package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder
@Data
public class MiamPolicyUpgrade {
    @CCD(
            label = "Has any application been made for a care order, a supervision order, an emergency protection order or an order requiring someone to disclose where a child is or to deliver the child to another person and which: a) is still going on? or b) has finished but the order is still in place?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo mpuChildInvolvedInMiam;
    @CCD(
            label = "Has the applicant attended a Mediation Information & Assessment Meeting (MIAM)?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo mpuApplicantAttendedMiam;
    @CCD(
            label = "Is the applicant claiming exemption from the requirement to attend a MIAM ?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo mpuClaimingExemptionMiam;
    @CCD(label = "MIAM Registration number (URN)", searchable = false)
    private final String mediatorRegistrationNumber;
    @CCD(label = "Family mediation service name", searchable = false)
    private final String familyMediatorServiceName;
    @CCD(label = "Sole Trader Name", searchable = false)
    private final String soleTraderName;
}
