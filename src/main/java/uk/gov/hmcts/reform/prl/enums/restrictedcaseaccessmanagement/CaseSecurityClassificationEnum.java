package uk.gov.hmcts.reform.prl.enums.restrictedcaseaccessmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum CaseSecurityClassificationEnum {
    @JsonProperty("PUBLIC")
    PUBLIC("PUBLIC"),
    @JsonProperty("PRIVATE")
    PRIVATE("PRIVATE"),
    @JsonProperty("RESTRICTED")
    RESTRICTED("RESTRICTED");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }

    public static CaseSecurityClassificationEnum fromValue(String value) {
        return Arrays.stream(values())
                .filter(event -> event.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown classification: " + value));
    }
}
