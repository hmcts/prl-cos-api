package uk.gov.hmcts.reform.prl.models;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Features {

    IS_BARRISTER_FEATURE_ENABLED("barristerFeatureEnabled"),
    IS_CAFCASS_DATE_TIME_FEATURE_ENABLED("cafcassDateTimeFeatureEnabled"),
    IS_OS_COURT_LOOKUP_ENABLED("osCourtLookupEnabled"),
    IS_AWAITING_INFORMATION_ENABLED("awaitingInformationEnabled"),
    IS_EXIT_AWAITING_INFORMATION_ENABLED("exitAwaitingInformationEnabled"),
    IS_CREATE_REQUEST_CIR_UPDATE_TASK_ENABLED("createRequestCirUpdateTaskEnabled"),
    CASE_NAME_WITH_PREFIX_RELEASE_DATE("caseNameWithPrefixReleaseDate");

    private final String name;
}
