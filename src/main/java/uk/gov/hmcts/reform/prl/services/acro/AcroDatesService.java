package uk.gov.hmcts.reform.prl.services.acro;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class AcroDatesService {

    private final LaunchDarklyClient launchDarklyClient;

    public LocalDateTime getStartDateForSearch() {
        long searchDuration = (long) launchDarklyClient.getFeatureValue("acro-fl404a-search-duration");
        return LocalDateTime.of(
            LocalDate.now(ZoneId.systemDefault()).minusDays(searchDuration), LocalTime.of(21, 0, 0));
    }

    public LocalDateTime getEndDateForSearch() {
        return LocalDateTime.of(
            LocalDate.now(ZoneId.systemDefault()),
            LocalTime.of(21, 0, 0)
        );
    }
}
