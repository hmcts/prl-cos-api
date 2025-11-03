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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcroDatesService {

    public static final LocalTime SEARCH_TIME = LocalTime.of(21, 0);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_OFFSET_DATE;
    private final LaunchDarklyClient launchDarklyClient;

    public LocalDateTime getStartDateForSearch() {
        long searchDuration = launchDarklyClient.getIntValue("acro-fl404a-search-duration");
        return LocalDateTime.of(getCurrentDateForSearch().minusDays(searchDuration), SEARCH_TIME);
    }

    public LocalDateTime getEndDateForSearch() {
        return LocalDateTime.of(getCurrentDateForSearch(), SEARCH_TIME);
    }

    private LocalDate getCurrentDateForSearch() {
        String stringValue = launchDarklyClient.getStringValue("acro-fl404a-search-date");

        LocalDate localDate = LocalDate.now(ZoneId.systemDefault());

        if ("false".equals(stringValue) || "now".equals(stringValue)) {
            return localDate;
        }

        try {
            return LocalDate.parse(stringValue, dateFormatter);
        } catch (DateTimeParseException e) {
            log.warn("could not parse date ... falling back to current date", e);
            return localDate;
        }
    }
}
