package uk.gov.hmcts.reform.prl.enums.respondentsolicitor;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum AbuseTypes {

    @JsonProperty("physicalAbuse")
    physicalAbuse("physicalAbuse", "Physical abuse"),

    @JsonProperty("psychologicalAbuse")
    psychologicalAbuse("psychologicalAbuse", "Psychological abuse"),

    @JsonProperty("sexualAbuse")
    sexualAbuse("sexualAbuse", "Sexual abuse"),

    @JsonProperty("emotionalAbuse")
    emotionalAbuse("emotionalAbuse", "Emotional abuse"),

    @JsonProperty("financialAbuse")
    financialAbuse("financialAbuse", "Financial abuse");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static AbuseTypes getValue(String key) {
        return AbuseTypes.valueOf(key);
    }
}
