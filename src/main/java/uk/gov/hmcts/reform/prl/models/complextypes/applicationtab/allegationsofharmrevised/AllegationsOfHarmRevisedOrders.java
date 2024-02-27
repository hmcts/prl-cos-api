package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder
public class AllegationsOfHarmRevisedOrders {

    private final YesOrNo newOrdersNonMolestation;
    OrderRevised nonMolestationOrder;
    private final YesOrNo newOrdersOccupation;
    OrderRevised occupationOrder;
    private final YesOrNo newOrdersForcedMarriageProtection;
    OrderRevised forcedMarriageProtectionOrder;
    private final YesOrNo newOrdersRestraining;
    OrderRevised restrainingOrder;
    private final YesOrNo newOrdersOtherInjunctive;
    OrderRevised otherInjunctiveOrder;
    private final YesOrNo newOrdersUndertakingInPlace;
    OrderRevised undertakingInPlaceOrder;

}
