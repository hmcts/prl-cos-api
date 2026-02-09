package uk.gov.hmcts.reform.prl.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    IS_BARRISTER_FEATURE_ENABLED("barristerFeatureEnabled"),
    IS_CAFCASS_DATE_TIME_FEATURE_ENABLED("cafcassDateTimeFeatureEnabled"),
    IS_OS_COURT_LOOKUP_ENABLED("osCourtLookupEnabled");

    private final String name;
}
