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
                LocalDate.of(2026, 5, 25),
                LocalDate.of(2026, 8, 31),
                LocalDate.of(2026, 12, 25),
                LocalDate.of(2026, 12, 28)
            ));
    }

    @Test
    void workingDaysBetweenReturnsNonZeroWhenToAfterFrom() {
        LocalDate from = LocalDate.of(2026, 6, 26);
        LocalDate to = LocalDate.of(2026, 7, 8);
        assertThat(workingDayIndicator.workingDaysBetween(from, to)).isEqualTo(8);
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
}
