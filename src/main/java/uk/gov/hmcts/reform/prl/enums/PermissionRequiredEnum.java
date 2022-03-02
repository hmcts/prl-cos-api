package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
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

