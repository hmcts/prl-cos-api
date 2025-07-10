package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Situation {

    @NotEmpty(message = "Order type should be provided to proceed with this application")
    private List<FL401OrderTypeEnum> ordersAppliedFor;

    @NotEmpty(message = "ordersAppliedWithoutNotice should be either true or false")
    private boolean ordersAppliedWithoutNotice;
    @NotEmpty(message = " if ordersAppliedWithoutNotice is true then {isOrdersAppliedWithoutNotice ? "
        + "'ordersAppliedWithoutNoticeReason should be provided' : ' '} ")
    private List<WithoutNoticeReasonEnum> ordersAppliedWithoutNoticeReason;
    private String ordersAppliedWithoutNoticeReasonDetails;
    private boolean bailConditionsOnRespondent;
    private CourtNavDate bailConditionsEndDate;
    private String additionalDetailsForCourt;

}
