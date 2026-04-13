package uk.gov.hmcts.reform.prl.services.workingdays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class WorkingDayIndicatorTest {

    @Mock
    private PublicHolidaysCollection publicHolidaysCollection;

    @InjectMocks
    private WorkingDayIndicator workingDayIndicator;

    @BeforeEach
    void setUp() {
        // 25 Dec 2026 (Fri) and 28 Dec 2026 (Mon, substitute Boxing Day) are the holidays in play
        lenient().when(publicHolidaysCollection.getPublicHolidays())
            .thenReturn(Set.of(
                LocalDate.of(2026, 12, 25),
                LocalDate.of(2026, 12, 28)
            ));
    }

    @Test
    void weekdayIsAWorkingDay() {
        assertThat(workingDayIndicator.isWorkingDay(LocalDate.of(2026, 4, 14))).isTrue(); // Tue
    }

    @Test
    void saturdayIsNotAWorkingDay() {
        assertThat(workingDayIndicator.isWorkingDay(LocalDate.of(2026, 4, 18))).isFalse(); // Sat
    }

    @Test
    void sundayIsNotAWorkingDay() {
        assertThat(workingDayIndicator.isWorkingDay(LocalDate.of(2026, 4, 19))).isFalse(); // Sun
    }

    @Test
    void publicHolidayIsNotAWorkingDay() {
        assertThat(workingDayIndicator.isWorkingDay(LocalDate.of(2026, 12, 25))).isFalse();
    }

    @Test
    void getNextWorkingDayReturnsSameDateIfWorking() {
        LocalDate tuesday = LocalDate.of(2026, 4, 14);
        assertThat(workingDayIndicator.getNextWorkingDay(tuesday)).isEqualTo(tuesday);
    }

    @Test
    void getNextWorkingDaySkipsWeekend() {
        LocalDate saturday = LocalDate.of(2026, 4, 18);
        assertThat(workingDayIndicator.getNextWorkingDay(saturday))
            .isEqualTo(LocalDate.of(2026, 4, 20)); // Mon
    }

    @Test
    void getNextWorkingDaySkipsWeekendAndHoliday() {
        // 25 Dec 2026 = Fri (holiday), 26-27 weekend, 28 = Mon (substitute holiday), 29 Tue = working
        assertThat(workingDayIndicator.getNextWorkingDay(LocalDate.of(2026, 12, 25)))
            .isEqualTo(LocalDate.of(2026, 12, 29));
    }

    @Test
    void workingDaysBetweenReturnsZeroWhenToEqualsFrom() {
        LocalDate day = LocalDate.of(2026, 4, 14);
        assertThat(workingDayIndicator.workingDaysBetween(day, day)).isZero();
    }

    @Test
    void workingDaysBetweenReturnsZeroWhenToBeforeFrom() {
        assertThat(workingDayIndicator.workingDaysBetween(
            LocalDate.of(2026, 4, 14),
            LocalDate.of(2026, 4, 13))).isZero();
    }

    @Test
    void workingDaysBetweenCountsOneWorkingDay() {
        // Tue 14 Apr 2026 -> Wed 15 Apr 2026 = 1 working day
        assertThat(workingDayIndicator.workingDaysBetween(
            LocalDate.of(2026, 4, 14),
            LocalDate.of(2026, 4, 15))).isEqualTo(1);
    }

    @Test
    void workingDaysBetweenSkipsWeekend() {
        // Fri 17 Apr 2026 -> Mon 20 Apr 2026 = 1 working day (Mon only, Sat+Sun skipped)
        assertThat(workingDayIndicator.workingDaysBetween(
            LocalDate.of(2026, 4, 17),
            LocalDate.of(2026, 4, 20))).isEqualTo(1);
    }

    @Test
    void workingDaysBetweenSkipsHolidays() {
        // Thu 24 Dec 2026 -> Tue 29 Dec 2026. Skips 25 (hol), 26-27 (wknd), 28 (hol). 1 working day.
        assertThat(workingDayIndicator.workingDaysBetween(
            LocalDate.of(2026, 12, 24),
            LocalDate.of(2026, 12, 29))).isEqualTo(1);
    }

    @Test
    void addWorkingDaysZeroReturnsSameDate() {
        LocalDate day = LocalDate.of(2026, 4, 14);
        assertThat(workingDayIndicator.addWorkingDays(day, 0)).isEqualTo(day);
    }

    @Test
    void addWorkingDaysOneSkipsWeekend() {
        // Fri 17 Apr 2026 + 1 working day = Mon 20 Apr 2026
        assertThat(workingDayIndicator.addWorkingDays(LocalDate.of(2026, 4, 17), 1))
            .isEqualTo(LocalDate.of(2026, 4, 20));
    }

    @Test
    void addWorkingDaysThreeFromMonday() {
        // Mon 13 Apr 2026 + 3 working days = Thu 16 Apr 2026
        assertThat(workingDayIndicator.addWorkingDays(LocalDate.of(2026, 4, 13), 3))
            .isEqualTo(LocalDate.of(2026, 4, 16));
    }

    @Test
    void addWorkingDaysAcrossHoliday() {
        // Thu 24 Dec 2026 + 1 working day = Tue 29 Dec 2026 (skips Xmas, weekend, Boxing Day substitute)
        assertThat(workingDayIndicator.addWorkingDays(LocalDate.of(2026, 12, 24), 1))
            .isEqualTo(LocalDate.of(2026, 12, 29));
    }
}