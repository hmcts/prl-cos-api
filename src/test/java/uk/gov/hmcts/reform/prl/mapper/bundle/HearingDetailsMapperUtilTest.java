package uk.gov.hmcts.reform.prl.mapper.bundle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleHearingInfo;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class HearingDetailsMapperUtilTest {

    private final HearingDetailsMapperUtil util = new HearingDetailsMapperUtil();

    @Test
    void shouldReturnEmptyBundleHearingInfoWhenHearingsIsNull() {
        // When
        BundleHearingInfo result = util.mapHearingDetails(null);

        // Then
        assertNotNull(result);
        assertNull(result.getHearingVenueAddress());
        assertNull(result.getHearingDateAndTime());
        assertNull(result.getHearingJudgeName());
    }

    @Test
    void shouldReturnEmptyBundleHearingInfoWhenCaseHearingsIsNull() {
        // Given
        Hearings hearings = Hearings.hearingsWith().build();

        // When
        BundleHearingInfo result = util.mapHearingDetails(hearings);

        // Then
        assertNotNull(result);
        assertNull(result.getHearingVenueAddress());
        assertNull(result.getHearingDateAndTime());
        assertNull(result.getHearingJudgeName());
    }

    @Test
    void shouldReturnEmptyBundleHearingInfoWhenCaseHearingsIsEmpty() {
        // Given
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(Collections.emptyList())
            .build();

        // When
        BundleHearingInfo result = util.mapHearingDetails(hearings);

        // Then
        assertNotNull(result);
        assertNull(result.getHearingVenueAddress());
        assertNull(result.getHearingDateAndTime());
        assertNull(result.getHearingJudgeName());
    }

    @Test
    void shouldReturnEmptyBundleHearingInfoWhenNoListedHearings() {
        // Given
        CaseHearing caseHearing = CaseHearing.caseHearingWith()
            .hmcStatus("PENDING")
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(caseHearing))
            .build();

        // When
        BundleHearingInfo result = util.mapHearingDetails(hearings);

        // Then
        assertNotNull(result);
        assertNull(result.getHearingVenueAddress());
        assertNull(result.getHearingDateAndTime());
        assertNull(result.getHearingJudgeName());
    }

    @Test
    void shouldReturnEmptyBundleHearingInfoWhenListedHearingHasNoHearingDaySchedule() {
        // Given
        CaseHearing caseHearing = CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(caseHearing))
            .build();

        // When
        BundleHearingInfo result = util.mapHearingDetails(hearings);

        // Then
        assertNotNull(result);
        assertNull(result.getHearingVenueAddress());
        assertNull(result.getHearingDateAndTime());
        assertNull(result.getHearingJudgeName());
    }

    @Test
    void shouldReturnEmptyBundleHearingInfoWhenListedHearingHasEmptyHearingDaySchedule() {
        // Given
        CaseHearing caseHearing = CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .hearingDaySchedule(Collections.emptyList())
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(caseHearing))
            .build();

        // When
        BundleHearingInfo result = util.mapHearingDetails(hearings);

        // Then
        assertNotNull(result);
        assertNull(result.getHearingVenueAddress());
        assertNull(result.getHearingDateAndTime());
        assertNull(result.getHearingJudgeName());
    }

    @Test
    void shouldMapHearingDetailsWhenListedHearingHasCompleteData() {
        // Given
        LocalDateTime hearingStartTime = LocalDateTime.of(2024, 1, 15, 10, 30);
        HearingDaySchedule hearingDaySchedule = HearingDaySchedule.hearingDayScheduleWith()
            .hearingStartDateTime(hearingStartTime)
            .hearingVenueName("Central Court")
            .hearingVenueAddress("123 Main Street, London")
            .hearingJudgeName("Judge Smith")
            .build();
        CaseHearing caseHearing = CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .hearingDaySchedule(List.of(hearingDaySchedule))
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(caseHearing))
            .build();

        // When
        BundleHearingInfo result = util.mapHearingDetails(hearings);

        // Then
        assertNotNull(result);
        assertEquals("Central Court\n123 Main Street, London", result.getHearingVenueAddress());
        assertEquals("15 Jan 2024 10:30 AM", result.getHearingDateAndTime()); // Assuming getBundleDateTime formats it this way
        assertEquals("Judge Smith", result.getHearingJudgeName());
    }

    @Test
    void shouldMapHearingDetailsWhenHearingStartDateTimeIsNull() {
        // Given
        HearingDaySchedule hearingDaySchedule = HearingDaySchedule.hearingDayScheduleWith()
            .hearingVenueName("Central Court")
            .hearingVenueAddress("123 Main Street, London")
            .hearingJudgeName("Judge Smith")
            .build();
        CaseHearing caseHearing = CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .hearingDaySchedule(List.of(hearingDaySchedule))
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(caseHearing))
            .build();

        // When
        BundleHearingInfo result = util.mapHearingDetails(hearings);

        // Then
        assertNotNull(result);
        assertEquals("Central Court\n123 Main Street, London", result.getHearingVenueAddress());
        assertEquals("", result.getHearingDateAndTime()); // BLANK_STRING when hearingStartDateTime is null
        assertEquals("Judge Smith", result.getHearingJudgeName());
    }

    @Test
    void shouldReturnHearingVenueAddressOnlyWhenHearingVenueNameIsNull() {
        // Given
        HearingDaySchedule hearingDaySchedule = HearingDaySchedule.hearingDayScheduleWith()
            .hearingVenueAddress("123 Main Street, London")
            .build();

        // When
        String result = util.getHearingVenueAddress(hearingDaySchedule);

        // Then
        assertEquals("123 Main Street, London", result);
    }

    @Test
    void shouldReturnCombinedVenueNameAndAddressWhenBothPresent() {
        // Given
        HearingDaySchedule hearingDaySchedule = HearingDaySchedule.hearingDayScheduleWith()
            .hearingVenueName("Central Court")
            .hearingVenueAddress("123 Main Street, London")
            .build();

        // When
        String result = util.getHearingVenueAddress(hearingDaySchedule);

        // Then
        assertEquals("Central Court\n123 Main Street, London", result);
    }
}
