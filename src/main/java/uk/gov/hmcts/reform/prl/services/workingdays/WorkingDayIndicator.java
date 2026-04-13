package uk.gov.hmcts.reform.prl.services.workingdays;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.util.Objects.requireNonNull;

/**
 * Determines whether a given date is a working day in England and Wales
 * (weekdays excluding public holidays) and provides working-day arithmetic.
 */
@Service
@RequiredArgsConstructor
public class WorkingDayIndicator {

    private final PublicHolidaysCollection publicHolidaysCollection;

    public boolean isWorkingDay(LocalDate date) {
        requireNonNull(date);
        return !isWeekend(date) && !isPublicHoliday(date);
    }

    public boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == SATURDAY || date.getDayOfWeek() == SUNDAY;
    }

    public boolean isPublicHoliday(LocalDate date) {
        return publicHolidaysCollection.getPublicHolidays().contains(date);
    }

    /**
     * Returns the given date if it is already a working day, otherwise the next working day.
     */
    public LocalDate getNextWorkingDay(LocalDate date) {
        requireNonNull(date);
        return isWorkingDay(date) ? date : getNextWorkingDay(date.plusDays(1));
    }

    /**
     * Returns the number of working days strictly between {@code from} and {@code to}
     * (exclusive of {@code from}, inclusive of {@code to}). Returns 0 when {@code to}
     * is on or before {@code from}.
     */
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

    /**
     * Returns the date {@code n} working days after the given date.
     */
    public LocalDate addWorkingDays(LocalDate date, int n) {
        requireNonNull(date);
        if (n <= 0) {
            return date;
        }
        LocalDate result = date;
        int remaining = n;
        while (remaining > 0) {
            result = result.plusDays(1);
            if (isWorkingDay(result)) {
                remaining--;
            }
        }
        return result;
    }
}