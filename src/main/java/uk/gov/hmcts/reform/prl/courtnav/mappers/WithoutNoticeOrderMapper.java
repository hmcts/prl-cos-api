package uk.gov.hmcts.reform.prl.courtnav.mappers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.stream.Collectors;
import javax.json.JsonObject;

@Component
public class WithoutNoticeOrderMapper {

    public JsonObject map(CourtNavCaseData courtNavCaseData) {

        return new NullAwareJsonObjectBuilder()
            .add("orderWithoutGivingNotice", CommonUtils.getYesOrNoValue(courtNavCaseData.getOrdersAppliedWithoutNotice()))
            .add("reasonForOrderWithoutGivingNotice",
                 courtNavCaseData.getOrdersAppliedWithoutNoticeReason() != null ? courtNavCaseData.getOrdersAppliedWithoutNoticeReason().stream()
                     .map(WithoutNoticeReasonEnum::getDisplayedValue).collect(Collectors.joining(", "))
                     : null)
            .add("futherDetails", courtNavCaseData.getOrdersAppliedWithoutNoticeReasonDetails())
            .add("isRespondentAlreadyInBailCondition", CommonUtils.getYesOrNoValue(courtNavCaseData.getBailConditionsOnRespondent()))
            .add("bailConditionEndDate", String.valueOf(courtNavCaseData.getBailConditionsEndDate()))
            .add("otherDetails", courtNavCaseData.getAdditionalDetailsForCourt())
            .build();

    }
}

