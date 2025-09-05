package uk.gov.hmcts.reform.prl.services.acro;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
public class AcroDatesService {

    public LocalDateTime getStartDateForSearch() {
        return LocalDateTime.of(
            LocalDate.now(ZoneId.systemDefault()).minusDays(19L), LocalTime.of(20, 59, 59));
    }

    public LocalDateTime getEndDateForSearch() {
        return LocalDateTime.of(
            LocalDate.now(ZoneId.systemDefault()),
            LocalTime.of(21, 0, 0)
        );
    }
}
