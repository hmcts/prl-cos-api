package uk.gov.hmcts.reform.prl.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
