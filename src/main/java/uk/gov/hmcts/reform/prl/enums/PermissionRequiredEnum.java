package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PermissionRequiredEnum {

    yes("yes"),
    noNowSought("no now sought"),
    noNotRequired("no, permission not required");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static PermissionRequiredEnum getValue(String key) {
        return PermissionRequiredEnum.valueOf(key);
    }

}

