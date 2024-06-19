package uk.gov.hmcts.reform.prl.enums.restrictedcaseaccessmanagement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CaseSecurityClassificationEnum {
    PUBLIC("public"), PRIVATE("private"), RESTRICTED("restricted");

    private final String value;

    public String getValue() {
        return value;
    }

    public static CaseSecurityClassificationEnum fromValue(String value) {
        return Arrays.stream(values())
                .filter(event -> event.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown event name: " + value));
    }
}
