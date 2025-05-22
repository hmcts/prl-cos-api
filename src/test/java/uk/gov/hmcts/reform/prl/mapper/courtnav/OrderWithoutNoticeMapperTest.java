package uk.gov.hmcts.reform.prl.mapper.courtnav;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.enums.ReasonForOrderWithoutGivingNoticeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDetailsOfWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.ReasonForWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.Situation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderWithoutNoticeMapperTest {

    private final OrderWithoutNoticeMapper mapper = new OrderWithoutNoticeMapperImpl();

    @Test
    void shouldMapOrderWithNotice() {
        Situation situation = Situation.builder()
            .ordersAppliedWithoutNotice(false)
            .additionalDetailsForCourt("Court details")
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder().situation(situation).build())
            .build();

        WithoutNoticeOrderDetails order = mapper.mapOrderWithoutNotice(source);
        assertEquals(YesOrNo.No, order.getOrderWithoutGivingNotice());

        ReasonForWithoutNoticeOrder reason = mapper.mapReasonForWithoutNotice(source);
        assertNull(reason);

        OtherDetailsOfWithoutNoticeOrder otherDetails = mapper.mapOtherDetails(source);
        assertEquals("Court details", otherDetails.getOtherDetails());
    }

    @Test
    void shouldMapOrderWithoutNoticeWithReasons() {
        Situation situation = Situation.builder()
            .ordersAppliedWithoutNotice(true)
            .ordersAppliedWithoutNoticeReason(List.of(WithoutNoticeReasonEnum.riskOfSignificantHarm))
            .ordersAppliedWithoutNoticeReasonDetails("Urgent danger")
            .additionalDetailsForCourt("Extra court info")
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder().situation(situation).build())
            .build();

        WithoutNoticeOrderDetails order = mapper.mapOrderWithoutNotice(source);
        assertEquals(YesOrNo.Yes, order.getOrderWithoutGivingNotice());

        ReasonForWithoutNoticeOrder reason = mapper.mapReasonForWithoutNotice(source);
        assertNotNull(reason);
        assertEquals(1, reason.getReasonForOrderWithoutGivingNotice().size());
        assertTrue(reason.getReasonForOrderWithoutGivingNotice().contains(ReasonForOrderWithoutGivingNoticeEnum.harmToApplicantOrChild));
        assertEquals("Urgent danger", reason.getFutherDetails());

        OtherDetailsOfWithoutNoticeOrder otherDetails = mapper.mapOtherDetails(source);
        assertEquals("Extra court info", otherDetails.getOtherDetails());
    }

    @Test
    void shouldHandleEmptyReasonList() {
        Situation situation = Situation.builder()
            .ordersAppliedWithoutNotice(true)
            .ordersAppliedWithoutNoticeReason(List.of())
            .ordersAppliedWithoutNoticeReasonDetails(null)
            .additionalDetailsForCourt("None")
            .build();

        CourtNavFl401 source = CourtNavFl401.builder()
            .fl401(CourtNavCaseData.builder().situation(situation).build())
            .build();

        ReasonForWithoutNoticeOrder reason = mapper.mapReasonForWithoutNotice(source);
        assertNotNull(reason);
        assertTrue(reason.getReasonForOrderWithoutGivingNotice().isEmpty());
        assertNull(reason.getFutherDetails());
    }
}
