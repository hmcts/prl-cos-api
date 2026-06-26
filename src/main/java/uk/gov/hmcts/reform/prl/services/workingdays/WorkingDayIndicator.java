package uk.gov.hmcts.reform.prl.services.workingdays;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.util.Objects.requireNonNull;

/**
 * Counts working days between two dates in England and Wales,
 * where a working day is a weekday that is not a public holiday.
 */
@Service
@RequiredArgsConstructor
public class WorkingDayIndicator {

    private final PublicHolidaysCollection publicHolidaysCollection;

    private boolean isWorkingDay(LocalDate date) {
        requireNonNull(date);
        return !isWeekend(date) && !isPublicHoliday(date);
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().equals(SATURDAY) || date.getDayOfWeek().equals(SUNDAY);
    }

    private boolean isPublicHoliday(LocalDate date) {
        return publicHolidaysCollection.getPublicHolidays().contains(date);
    }

    public int workingDaysBetween(LocalDate from, LocalDate to) {
        requireNonNull(from);
        requireNonNull(to);
        if (!to.isAfter(from)) {
            return 0;
        }
        int count = 0;
        LocalDate cursor = from.plusDays(1);
        while (!cursor.isAfter(to)) {
            if (isWorkingDay(cursor)) {
                count++;
            }
            cursor = cursor.plusDays(1);
        }
        return count;
    }
}
