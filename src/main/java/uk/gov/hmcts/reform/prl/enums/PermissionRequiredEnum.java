package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum PermissionRequiredEnum {

    yes("Yes"),
    noNowSought("No, permission now sought"),
    noNotRequired("No, permission is not required");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static PermissionRequiredEnum getValue(String key) {
        for (PermissionRequiredEnum permission : PermissionRequiredEnum.values()) {
            if (permission.displayedValue.equalsIgnoreCase(key)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + key);
    }

}

