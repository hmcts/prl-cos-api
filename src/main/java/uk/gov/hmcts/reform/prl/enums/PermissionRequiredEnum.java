package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum PermissionRequiredEnum {

    @CCD(label = "Yes")
    yes("Yes"),
    @CCD(label = "No, permission now sought")
    noNowSought("No, permission now sought"),
    @CCD(label = "No, permission is not required")
    noNotRequired("No, permission is not required");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static PermissionRequiredEnum getValue(String key) {
        if ("Yes".equalsIgnoreCase(key)) {
            return yes;
        }
        if ("No".equalsIgnoreCase(key)) {
            return noNotRequired;
        }
        return PermissionRequiredEnum.valueOf(key);
    }
}

