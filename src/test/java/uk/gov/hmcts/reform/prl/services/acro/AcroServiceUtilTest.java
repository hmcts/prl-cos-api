package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AcroServiceUtilTest {

    @Test
    void getNextHearingDateWithInHearing() {
        LocalDateTime hearingStartDateTime = LocalDateTime.now().plusDays(1L);
        final CaseHearing caseHearing = CaseHearing.caseHearingWith().hearingID(Long.valueOf("1234"))
            .hmcStatus("LISTED")
            .hearingType("ABA5-FFH")
            .hearingID(Long.valueOf("2000004659"))
            .hearingDaySchedule(
                List.of(
                    HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueName("ROYAL COURTS OF JUSTICE - QUEENS BUILDING (AND WEST GREEN BUILDING)")
                        .hearingStartDateTime(hearingStartDateTime).hearingEndDateTime(
                            LocalDateTime.parse("2023-05-09T09:45:00")).build(),
                    HearingDaySchedule.hearingDayScheduleWith()
                        .hearingVenueName("ROYAL COURTS OF JUSTICE - QUEENS BUILDING (AND WEST GREEN BUILDING)")
                        .hearingStartDateTime(hearingStartDateTime.plusHours(4L)).hearingEndDateTime(
                            LocalDateTime.parse("2023-05-09T09:45:00")).build()
                )).build();

        LocalDateTime nextHearingDateWithInHearing = AcroServiceUtil.getNextHearingDateWithInHearing(caseHearing);
        assertEquals(nextHearingDateWithInHearing, hearingStartDateTime);
    }

    @Test
    void getOrderExpiryDate() {

        LocalDateTime fl404bDateOrderEnd = LocalDateTime.parse("2023-05-09T09:45:00");
        FL404 fl404CustomFields = FL404.builder()
            .fl404bDateOrderEnd(fl404bDateOrderEnd)
            .build();
        OrderDetails order = OrderDetails.builder().fl404CustomFields(fl404CustomFields).build();
        LocalDateTime orderExpiryDate = AcroServiceUtil.getOrderExpiryDate(order);
        assertEquals(orderExpiryDate, fl404bDateOrderEnd);
    }
}
