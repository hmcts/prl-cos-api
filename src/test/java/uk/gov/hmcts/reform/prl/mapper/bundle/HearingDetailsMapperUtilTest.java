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
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.getBundleDateTime;

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
        LocalDateTime hearingStartTime = LocalDateTime.now().plusDays(7);
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
        assertEquals(getBundleDateTime(hearingStartTime), result.getHearingDateAndTime());
        assertEquals("Judge Smith", result.getHearingJudgeName());
    }

    @Test
    void shouldReturnEmptyBundleHearingInfoWhenHearingStartDateTimeIsNull() {
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
        assertNull(result.getHearingVenueAddress());
        assertNull(result.getHearingDateAndTime());
        assertNull(result.getHearingJudgeName());
    }

    @Test
    void shouldMapNextScheduledListedHearingWhenFirstListedHearingIsInPast() {
        // Given
        LocalDateTime pastHearingStartTime = LocalDateTime.now().minusDays(1);
        LocalDateTime futureHearingStartTime = LocalDateTime.now().plusDays(3);
        HearingDaySchedule pastSchedule = HearingDaySchedule.hearingDayScheduleWith()
            .hearingStartDateTime(pastHearingStartTime)
            .hearingVenueName("Past Court")
            .hearingVenueAddress("1 Past Street, London")
            .hearingJudgeName("Judge Past")
            .build();
        HearingDaySchedule futureSchedule = HearingDaySchedule.hearingDayScheduleWith()
            .hearingStartDateTime(futureHearingStartTime)
            .hearingVenueName("Future Court")
            .hearingVenueAddress("1 Future Street, London")
            .hearingJudgeName("Judge Future")
            .build();
        CaseHearing pastHearing = CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .hearingDaySchedule(List.of(pastSchedule))
            .build();
        CaseHearing futureHearing = CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .hearingDaySchedule(List.of(futureSchedule))
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(pastHearing, futureHearing))
            .build();

        // When
        BundleHearingInfo result = util.mapHearingDetails(hearings);

        // Then
        assertNotNull(result);
        assertEquals("Future Court\n1 Future Street, London", result.getHearingVenueAddress());
        assertEquals(getBundleDateTime(futureHearingStartTime), result.getHearingDateAndTime());
        assertEquals("Judge Future", result.getHearingJudgeName());
    }

    @Test
    void shouldMapEarliestFutureScheduleWithinListedHearing() {
        // Given
        LocalDateTime laterHearingStartTime = LocalDateTime.now().plusDays(8);
        LocalDateTime nextHearingStartTime = LocalDateTime.now().plusDays(2);
        HearingDaySchedule laterSchedule = HearingDaySchedule.hearingDayScheduleWith()
            .hearingStartDateTime(laterHearingStartTime)
            .hearingVenueName("Later Court")
            .hearingVenueAddress("1 Later Street, London")
            .hearingJudgeName("Judge Later")
            .build();
        HearingDaySchedule nextSchedule = HearingDaySchedule.hearingDayScheduleWith()
            .hearingStartDateTime(nextHearingStartTime)
            .hearingVenueName("Next Court")
            .hearingVenueAddress("1 Next Street, London")
            .hearingJudgeName("Judge Next")
            .build();
        CaseHearing caseHearing = CaseHearing.caseHearingWith()
            .hmcStatus("LISTED")
            .hearingDaySchedule(List.of(laterSchedule, nextSchedule))
            .build();
        Hearings hearings = Hearings.hearingsWith()
            .caseHearings(List.of(caseHearing))
            .build();

        // When
        BundleHearingInfo result = util.mapHearingDetails(hearings);

        // Then
        assertNotNull(result);
        assertEquals("Next Court\n1 Next Street, London", result.getHearingVenueAddress());
        assertEquals(getBundleDateTime(nextHearingStartTime), result.getHearingDateAndTime());
        assertEquals("Judge Next", result.getHearingJudgeName());
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
