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
    void getStartDateForSearch() {
        when(launchDarklyClient.getFeatureValue(eq("acro-fl404a-search-duration"))).thenReturn(1);
        assertEquals(
            acroDatesService.getStartDateForSearch(),
            LocalDateTime.of(
                LocalDate.now(ZoneId.systemDefault()).minusDays(1L),
                LocalTime.of(21, 0, 0)
            )
        );
    }

    @Test
    void getEndDateForSearch() {
        assertEquals(
            acroDatesService.getEndDateForSearch(),
            LocalDateTime.of(
                LocalDate.now(ZoneId.systemDefault()),
                LocalTime.of(21, 0, 0)
            )
        );
    }
}
