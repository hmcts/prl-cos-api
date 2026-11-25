package uk.gov.hmcts.reform.prl.utils;

import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

public final class HearingSelectionUtils {

    private HearingSelectionUtils() {
    }

    public static Optional<CaseHearing> getNextScheduledListedHearing(List<CaseHearing> caseHearings) {
        LocalDateTime now = LocalDateTime.now();

        return nullSafeCollection(caseHearings).stream()
            .filter(caseHearing -> getNextScheduledHearingDay(caseHearing, now).isPresent())
            .min(Comparator.comparing(caseHearing -> getNextScheduledHearingDay(caseHearing, now)
                .map(HearingDaySchedule::getHearingStartDateTime)
                .orElse(LocalDateTime.MAX)));
    }

    public static Optional<HearingDaySchedule> getNextScheduledHearingDay(CaseHearing caseHearing) {
        return getNextScheduledHearingDay(caseHearing, LocalDateTime.now());
    }

    private static Optional<HearingDaySchedule> getNextScheduledHearingDay(CaseHearing caseHearing, LocalDateTime now) {
        if (caseHearing == null || !LISTED.equalsIgnoreCase(caseHearing.getHmcStatus())) {
            return Optional.empty();
        }

        return nullSafeCollection(caseHearing.getHearingDaySchedule()).stream()
            .filter(hearingDaySchedule -> Objects.nonNull(hearingDaySchedule.getHearingStartDateTime()))
            .filter(hearingDaySchedule -> hearingDaySchedule.getHearingStartDateTime().isAfter(now))
            .min(Comparator.comparing(HearingDaySchedule::getHearingStartDateTime));
    }
}
