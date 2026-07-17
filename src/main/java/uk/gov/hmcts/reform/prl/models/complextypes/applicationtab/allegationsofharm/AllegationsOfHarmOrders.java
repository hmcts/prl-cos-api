package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Order;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class AllegationsOfHarmOrders {

    @CCD(label = "Non-molestation order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo ordersNonMolestation;
    @CCD(label = "Non-molestation order", searchable = false)
    Order nonMolestationOrder;
    @CCD(label = "Occupation order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo ordersOccupation;
    @CCD(label = "Occupation order", searchable = false)
    Order occupationOrder;
    @CCD(label = "Forced marriage protection order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo ordersForcedMarriageProtection;
    @CCD(label = "Forced marriage protection order", searchable = false)
    Order forcedMarriageProtectionOrder;
    @CCD(label = "Restraining order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo ordersRestraining;
    @CCD(label = "Restraining order", searchable = false)
    Order restrainingOrder;
    @CCD(label = "Other injunctive order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo ordersOtherInjunctive;
    @CCD(label = "Other injunctive order", searchable = false)
    Order otherInjunctiveOrder;
    @CCD(label = "Undertaking in place order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo ordersUndertakingInPlace;
    @CCD(label = "Undertaking in place order", searchable = false)
    Order undertakingInPlaceOrder;

}
