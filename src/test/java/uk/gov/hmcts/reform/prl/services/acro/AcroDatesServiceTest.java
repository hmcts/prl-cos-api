package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AcroDatesServiceTest {

    @Test
    void getStartDateForSearch() {

        assertEquals(
            new AcroDatesService().getStartDateForSearch(),
            LocalDateTime.of(
                LocalDate.now(ZoneId.systemDefault()).minusDays(1L),
                LocalTime.of(20, 59, 59)
            )
        );
    }

    @Test
    void getEndDateForSearch() {
        assertEquals(
            new AcroDatesService().getEndDateForSearch(),
            LocalDateTime.of(
                LocalDate.now(ZoneId.systemDefault()),
                LocalTime.of(21, 0, 0)
            )
        );
    }
}
