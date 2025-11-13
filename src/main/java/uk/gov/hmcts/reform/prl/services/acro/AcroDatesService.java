package uk.gov.hmcts.reform.prl.services.acro;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcroDatesService {

    public static final LocalTime SEARCH_TIME = LocalTime.of(21, 0);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final LaunchDarklyClient launchDarklyClient;

    public LocalDateTime getStartDateForSearch() {
        long searchDuration = launchDarklyClient.getIntVariation("acro-fl404a-search-duration");
        return LocalDateTime.of(getCurrentDateForSearch().minusDays(searchDuration), SEARCH_TIME);
    }

    public LocalDateTime getEndDateForSearch() {
        return LocalDateTime.of(getCurrentDateForSearch(), SEARCH_TIME);
    }

    private LocalDate getCurrentDateForSearch() {
        String searchDate = launchDarklyClient.getStringVariation("acro-fl404a-search-date");

        LocalDate localDate = LocalDate.now(ZoneId.systemDefault());

        if ("false".equals(searchDate) || "now".equals(searchDate)) {
            return localDate;
        }

        try {
            return LocalDate.parse(searchDate, dateFormatter);
        } catch (DateTimeParseException e) {
            log.warn("could not parse date {} configured in launch darkly due to ",searchDate, e);
            throw new RuntimeException("could not parse date " + searchDate, e);
        }
    }
}
