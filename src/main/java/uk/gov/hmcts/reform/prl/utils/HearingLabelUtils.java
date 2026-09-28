package uk.gov.hmcts.reform.prl.utils;

import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

public final class HearingLabelUtils {

    private static final String LABEL_SEPARATOR = " - ";
    private static final DateTimeFormatter HEARING_LABEL_DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm:ss");

    private HearingLabelUtils() {
    }

    public static String buildHearingsTypeLabel(CaseHearing hearing, HearingDaySchedule daySchedule) {
        if (hearing == null || daySchedule == null || daySchedule.getHearingStartDateTime() == null) {
            return null;
        }
        String hearingType = String.valueOf(hearing.getHearingTypeValue());
        String hearingDate = daySchedule.getHearingStartDateTime().format(HEARING_LABEL_DATE_FORMAT);
        return hearingType + LABEL_SEPARATOR + hearingDate;
    }

    public static Set<String> buildHearingsTypeLabels(CaseHearing hearing) {
        if (hearing == null || hearing.getHearingDaySchedule() == null) {
            return Set.of();
        }
        return hearing.getHearingDaySchedule().stream()
            .map(daySchedule -> buildHearingsTypeLabel(hearing, daySchedule))
            .filter(label -> label != null)
            .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * The date-time suffix of a hearings-type dropdown label, e.g. {@code "13/05/2026 09:00:00"}
     * from {@code "Allocation - 13/05/2026 09:00:00"}. Used as a fallback link key when the
     * ref-data lookup that populates the type prefix is unavailable.
     */
    public static Set<String> buildHearingDateSuffixes(CaseHearing hearing) {
        if (hearing == null || hearing.getHearingDaySchedule() == null) {
            return Set.of();
        }
        return hearing.getHearingDaySchedule().stream()
            .map(HearingDaySchedule::getHearingStartDateTime)
            .filter(java.util.Objects::nonNull)
            .map(HEARING_LABEL_DATE_FORMAT::format)
            .collect(Collectors.toUnmodifiableSet());
    }

    public static String extractDateSuffix(String label) {
        if (label == null) {
            return null;
        }
        int idx = label.lastIndexOf(LABEL_SEPARATOR);
        return idx >= 0 ? label.substring(idx + LABEL_SEPARATOR.length()) : label;
    }
}
