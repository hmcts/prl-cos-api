package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcroDatesServiceTest {

    @Mock
    LaunchDarklyClient launchDarklyClient;

    @InjectMocks
    AcroDatesService acroDatesService;

    @Test
    void getStartDateForSearchNow() {
        when(launchDarklyClient.getIntVariation(eq("acro-fl404a-search-duration"))).thenReturn(1);
        when(launchDarklyClient.getStringVariation(eq("acro-fl404a-search-date"))).thenReturn("now");
        assertEquals(
            acroDatesService.getStartDateForSearch(),
            LocalDateTime.of(
                LocalDate.now(ZoneId.systemDefault()).minusDays(1L),
                LocalTime.of(21, 0)
            )
        );
    }

    @Test
    void getEndDateForSearchForNow() {
        when(launchDarklyClient.getStringVariation(eq("acro-fl404a-search-date"))).thenReturn("now");
        assertEquals(
            acroDatesService.getEndDateForSearch(),
            LocalDateTime.of(
                LocalDate.now(ZoneId.systemDefault()),
                LocalTime.of(21, 0)
            )
        );
    }

    @Test
    void getStartDateForSearchForPastDate() {
        int duration = 1;
        String pastDate = "2025-10-15";
        when(launchDarklyClient.getIntVariation(eq("acro-fl404a-search-duration"))).thenReturn(duration);
        when(launchDarklyClient.getStringVariation(eq("acro-fl404a-search-date"))).thenReturn(pastDate);
        assertEquals(
            acroDatesService.getStartDateForSearch(),
            LocalDateTime.of(
                    LocalDate.parse(pastDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).minusDays(duration),
                LocalTime.of(21, 0)
        )
        );
    }

    @Test
    void getEndDateForSearchForForPastDate() {
        String pastDate = "2025-10-10";
        when(launchDarklyClient.getStringVariation(eq("acro-fl404a-search-date"))).thenReturn(pastDate);
        assertEquals(
            acroDatesService.getEndDateForSearch(),
            LocalDateTime.of(
                LocalDate.parse(pastDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                LocalTime.of(21, 0)
            )
        );
    }
}
