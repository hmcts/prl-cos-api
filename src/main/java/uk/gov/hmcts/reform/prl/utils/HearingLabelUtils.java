package uk.gov.hmcts.reform.prl.utils;

import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

public final class HearingLabelUtils {

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
        return hearingType + " - " + hearingDate;
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
}
