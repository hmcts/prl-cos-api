package uk.gov.hmcts.reform.prl.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * This enum stores all actively used feature toggle flags.
 * Please see `feature-toggle` in application.yaml to see all feature flags. Env vars can be set in cnp_flux_config repo.
 */
@RequiredArgsConstructor
@Getter
public enum Features {

    /**
     * This is just an example. It should be removed when you have a real flag added.
     */
    EXAMPLE("example"),
    EXAMPLE_OFF("example_off"),
    EXAMPLE_NOT_DEFINED("this-flag-is-not-defined-in-config");

    private final String name;

}
