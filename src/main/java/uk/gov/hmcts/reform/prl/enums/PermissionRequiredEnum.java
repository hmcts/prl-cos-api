package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionRequiredEnum {

    yes("yes"),
    noNowSought("no now sought"),
    noNotRequired("no, permission not required");

    private final String displayedValue;
}

