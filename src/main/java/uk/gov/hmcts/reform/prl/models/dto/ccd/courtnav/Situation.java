package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum;

import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class Situation {

    /**
     * type of application.
     */
    @Valid
    @NotNull(message = "Order type should be provided to proceed with this application")
    private final List<FL401OrderTypeEnum> ordersAppliedFor;

    /**
     * without notice order.
     */
    @Valid
    @NotNull(message = "ordersAppliedWithoutNotice should be either true or false")
    private final boolean ordersAppliedWithoutNotice;
    @Valid
    @NotNull(message = " if ordersAppliedWithoutNotice is true then {isOrdersAppliedWithoutNotice ? "
        + "'ordersAppliedWithoutNoticeReason should be provided' : ' '} ")
    private final List<WithoutNoticeReasonEnum> ordersAppliedWithoutNoticeReason;
    private final String ordersAppliedWithoutNoticeReasonDetails;
    private final boolean bailConditionsOnRespondent;
    private final CourtNavDate bailConditionsEndDate;
    private final String additionalDetailsForCourt;

}
