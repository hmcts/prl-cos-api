package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@@RequiredArgsConstructor
public enum PermissionRequiredEnum {

    Yes("Yes"),
    noNotRequired("No, permission not required");

    private final String displayedValue;

}

