package uk.gov.hmcts.reform.prl.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    IS_BARRISTER_FEATURE_ENABLED("barristerFeatureEnabled"),
    ACRO_CONFIDENTIAL_DATA_ALLOWED("acroConfidentialDataAllowed");

    private final String name;
}
