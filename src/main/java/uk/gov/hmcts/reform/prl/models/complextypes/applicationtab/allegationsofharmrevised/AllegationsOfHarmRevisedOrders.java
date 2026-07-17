package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class AllegationsOfHarmRevisedOrders {

    @CCD(label = "Non-molestation order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo newOrdersNonMolestation;
    @CCD(label = "Non-molestation order", searchable = false)
    OrderRevised nonMolestationOrder;
    @CCD(label = "Occupation order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo newOrdersOccupation;
    @CCD(label = "Occupation order", searchable = false)
    OrderRevised occupationOrder;
    @CCD(label = "Forced marriage protection order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo newOrdersForcedMarriageProtection;
    @CCD(label = "Forced marriage protection order", searchable = false)
    OrderRevised forcedMarriageProtectionOrder;
    @CCD(label = "Restraining order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo newOrdersRestraining;
    @CCD(label = "Restraining order", searchable = false)
    OrderRevised restrainingOrder;
    @CCD(label = "Other injunctive order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo newOrdersOtherInjunctive;
    @CCD(label = "Other injunctive order", searchable = false)
    OrderRevised otherInjunctiveOrder;
    @CCD(label = "Undertaking in place order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo newOrdersUndertakingInPlace;
    @CCD(label = "Undertaking in place order", searchable = false)
    OrderRevised undertakingInPlaceOrder;

}
