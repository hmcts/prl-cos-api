package uk.gov.hmcts.reform.prl.services.acro;

import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;

import java.time.LocalDateTime;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

public class AcroServiceUtil {

    private AcroServiceUtil() {
        //Util for ACRO data
    }

    /**
     * Code to get NextHearingDate with in case hearing.
     * Fetch order's hearingId.
     * Find the case hearing matching that hearing id from case hearings.
     *
     * @return Next Hearing Date
     */
    public static LocalDateTime getNextHearingDateWithInHearing(CaseHearing hearing) {
        LocalDateTime nextHearingDate = null;
        if (hearing.getHmcStatus().equals(LISTED)) {
            Optional<LocalDateTime> minDateOfHearingDaySche = nullSafeCollection(hearing.getHearingDaySchedule()).stream()
                .map(HearingDaySchedule::getHearingStartDateTime)
                .filter(hearingStartDateTime -> hearingStartDateTime.isAfter(LocalDateTime.now()))
                .min(LocalDateTime::compareTo);
            if (minDateOfHearingDaySche.isPresent()) {
                nextHearingDate = minDateOfHearingDaySche.get();
            }
        }
        return nextHearingDate;
    }

    /**
     * Get order expiry date from order.
     *
     * @return order expiry date
     */
    public static LocalDateTime getOrderExpiryDate(OrderDetails order) {

        if (order.getFl404CustomFields() != null && order.getFl404CustomFields().getFl404bDateOrderEnd() != null) {
            return order.getFl404CustomFields().getFl404bDateOrderEnd();
        }
        return null;
    }
}
