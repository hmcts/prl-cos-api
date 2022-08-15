package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class Situation {

    /**
     * type of application.
     */

    private final List<FL401OrderTypeEnum> ordersAppliedFor;

    /**
     * without notice order.
     */
    private final boolean ordersAppliedWithoutNotice;
    private final List<WithoutNoticeReasonEnum> ordersAppliedWithoutNoticeReason;
    private final String ordersAppliedWithoutNoticeReasonDetails;
    private final boolean bailConditionsOnRespondent;
    private final CourtNavDate bailConditionsEndDate;
    private final String additionalDetailsForCourt;

}
