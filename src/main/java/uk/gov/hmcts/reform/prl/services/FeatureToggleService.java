package uk.gov.hmcts.reform.prl.services;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.Features;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.models.Features.IS_BARRISTER_FEATURE_ENABLED;
import static uk.gov.hmcts.reform.prl.models.Features.IS_CAFCASS_DATE_TIME_FEATURE_ENABLED;
import static uk.gov.hmcts.reform.prl.models.Features.IS_LA_SOCIAL_WORKER_FEATURE_ENABLED;
import static uk.gov.hmcts.reform.prl.models.Features.IS_OS_COURT_LOOKUP_ENABLED;


/**
 * To add a feature toggle flag:
 * <ul>
 *     <li>add an entry in application properties with key prefixed with {@code feature.toggle.}, eg.
 *     {@code feature.toggle.blah} that should have value of {@code true} or {@code false}</li>
 *     <li>add an entry to {@link Features} class, eg. {@code BLAH("blah")}</li>
 *     <li>add appropriate method to check if feature is enabled, eg. {@code public boolean isBlahEnabled()}</li>
 * </ul>
 * Spring configuration will populate {@link #toggle} map with values from properties file.
 */
@Service
@ConfigurationProperties(prefix = "feature")
@Configuration
@Getter
public class FeatureToggleService {

    @NotNull
    private Map<String, String> toggle = new HashMap<>();

    private boolean isFeatureEnabled(Features feature) {
        return Optional.ofNullable(toggle.get(feature.getName()))
            .map(Boolean::parseBoolean)
            .orElse(false);
    }

    /*
     * Defaulted to true. Only to be set to false in Preview as ACA API is not deployed there
     */
    public boolean isBarristerFeatureEnabled() {
        return isFeatureEnabled(IS_BARRISTER_FEATURE_ENABLED);
    }

    public boolean isLaSocialWorkerFeatureEnabled() {
        return isFeatureEnabled(IS_LA_SOCIAL_WORKER_FEATURE_ENABLED);
    }

    public boolean isCafcassDateTimeFeatureEnabled() {
        return isFeatureEnabled(IS_CAFCASS_DATE_TIME_FEATURE_ENABLED);
    }

    public boolean isOsCourtLookupFeatureEnabled() {
        return isFeatureEnabled(IS_OS_COURT_LOOKUP_ENABLED);
    }
}
