package uk.gov.hmcts.reform.prl.utils;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMPLETED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@Slf4j
public class HearingUtils {

    public static boolean isFirstHearingBefore(Hearings hearings,
                                              long days) {
        return isFirstHearing(hearings,
                              days,
                              true,
                              false);
    }

    public static boolean isFirstHearingAfter(Hearings hearings,
                                              long days) {
        return isFirstHearing(hearings,
                              days,
                              false,
                              true);
    }

    public static boolean isFirstHearing(Hearings hearings,
                                         long days,
                                         boolean isBefore,
                                         boolean isAfter) {
        if (null != hearings) {
            LocalDateTime firstHearingLimit = LocalDateTime.now().plusDays(days).withNano(1);
            List<HearingDaySchedule> sortedHearingDaySchedules = nullSafeCollection(hearings.getCaseHearings()).stream()
                .filter(eachHearing -> (eachHearing.getHmcStatus().equals(LISTED)
                    || eachHearing.getHmcStatus().equals(COMPLETED))
                    && null != eachHearing.getHearingDaySchedule())
                .map(CaseHearing::getHearingDaySchedule)
                .flatMap(Collection::stream)
                .sorted(Comparator.comparing(
                    HearingDaySchedule::getHearingStartDateTime,
                    Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .toList();

            if (CollectionUtils.isNotEmpty(sortedHearingDaySchedules)) {
                if (isBefore) {
                    return sortedHearingDaySchedules.get(0).getHearingStartDateTime().isBefore(firstHearingLimit);
                } else if (isAfter) {
                    return sortedHearingDaySchedules.get(0).getHearingStartDateTime().isAfter(firstHearingLimit);
                }
            }
        }
        return false;
    }

}
