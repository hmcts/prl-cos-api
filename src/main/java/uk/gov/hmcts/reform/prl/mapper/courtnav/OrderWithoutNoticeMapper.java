package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.mapstruct.Mapper;
import uk.gov.hmcts.reform.prl.enums.ReasonForOrderWithoutGivingNoticeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDetailsOfWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.ReasonForWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderWithoutNoticeMapper {

    default WithoutNoticeOrderDetails mapOrderWithoutNotice(CourtNavFl401 source) {
        boolean appliedWithoutNotice = source.getFl401().getSituation().isOrdersAppliedWithoutNotice();
        return WithoutNoticeOrderDetails.builder()
            .orderWithoutGivingNotice(appliedWithoutNotice ? uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes : uk.gov.hmcts.reform.prl.enums.YesOrNo.No)
            .build();
    }

    default ReasonForWithoutNoticeOrder mapReasonForWithoutNotice(CourtNavFl401 source) {
        if (!source.getFl401().getSituation().isOrdersAppliedWithoutNotice()) {
            return null;
        }

        return ReasonForWithoutNoticeOrder.builder()
            .reasonForOrderWithoutGivingNotice(getReasons(source))
            .futherDetails(source.getFl401().getSituation().getOrdersAppliedWithoutNoticeReasonDetails())
            .build();
    }

    default OtherDetailsOfWithoutNoticeOrder mapOtherDetails(CourtNavFl401 source) {
        return OtherDetailsOfWithoutNoticeOrder.builder()
            .otherDetails(source.getFl401().getSituation().getAdditionalDetailsForCourt())
            .build();
    }

    private List<ReasonForOrderWithoutGivingNoticeEnum> getReasons(CourtNavFl401 source) {
        List<WithoutNoticeReasonEnum> reasons = source.getFl401().getSituation().getOrdersAppliedWithoutNoticeReason();
        List<ReasonForOrderWithoutGivingNoticeEnum> mapped = new ArrayList<>();

        for (WithoutNoticeReasonEnum reason : reasons) {
            mapped.add(ReasonForOrderWithoutGivingNoticeEnum.getDisplayedValueFromEnumString(reason.toString()));
        }

        return mapped;
    }
}
