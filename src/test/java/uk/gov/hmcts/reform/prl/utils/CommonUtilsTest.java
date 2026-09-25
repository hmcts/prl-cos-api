package uk.gov.hmcts.reform.prl.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommonUtilsTest {

    @Test
    void shouldFormatDateTimeInWinterTime() {
        // Given - Winter time (no BST offset)
        LocalDateTime utcDateTime = LocalDateTime.of(2024, Month.JANUARY, 15, 10, 30, 0);

        // When
        String result = CommonUtils.getBundleDateTime(utcDateTime);

        // Then
        assertNotNull(result);
        assertEquals("15 Jan 2024 10:30 AM", result);
    }

    @Test
    void shouldFormatDateTimeInSummerTime() {
        // Given - Summer time (BST offset +1 hour)
        LocalDateTime utcDateTime = LocalDateTime.of(2024, Month.JULY, 15, 10, 30, 0);

        // When
        String result = CommonUtils.getBundleDateTime(utcDateTime);

        // Then
        assertNotNull(result);
        assertEquals("15 Jul 2024 11:30 AM", result);
    }

    @Test
    void shouldFormatDateAfterBstConversionWhenUtcTimeCrossesMidnight() {
        // Given - 23:30 UTC is 00:30 next day in London during BST
        LocalDateTime utcDateTime = LocalDateTime.of(2024, Month.AUGUST, 5, 23, 30, 0);

        // When
        String result = CommonUtils.getBundleDateTime(utcDateTime);

        // Then
        assertNotNull(result);
        assertEquals("6 Aug 2024 12:30 AM", result);
    }

    @Test
    void shouldReturnEmptyOptionalForInvalidDate() {
        Optional<LocalDate> result = CommonUtils.parseDate("not-a-date");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyOptionalForNullDate() {
        Optional<LocalDate> result = CommonUtils.parseDate(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldParseValidDateUsingReflection() {
        Optional<LocalDate> result = CommonUtils.parseDate("2025-01-15");
        assertNotNull(result);
        assertTrue(result.isPresent());
        assertEquals(LocalDate.of(2025, 1, 15), result.get());
    }
}
