package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum ChildAbuseEnum {

    @JsonProperty("physicalAbuse")
    physicalAbuse("physicalAbuse","Physical abuse"),

    @JsonProperty("psychologicalAbuse")
    psychologicalAbuse("psychologicalAbuse","Psychological abuse"),

    @JsonProperty("sexualAbuse")
    sexualAbuse("sexualAbuse","Sexual abuse"),

    @JsonProperty("emotionalAbuse")
    emotionalAbuse("emotionalAbuse","Emotional abuse"),

    @JsonProperty("financialAbuse")
    financialAbuse("financialAbuse","Financial abuse");

    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static ChildAbuseEnum getValue(String key) {
        return ChildAbuseEnum.valueOf(key);
    }

}
