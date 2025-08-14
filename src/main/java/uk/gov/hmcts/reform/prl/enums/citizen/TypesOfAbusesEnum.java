package uk.gov.hmcts.reform.prl.enums.citizen;

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
public enum TypesOfAbusesEnum {

    @JsonProperty("physicalAbuse")
    physicalAbuse("physicalAbuse","physicalAbuse"),
    @JsonProperty("psychologicalAbuse")
    psychologicalAbuse("psychologicalAbuse","psychologicalAbuse"),
    @JsonProperty("emotionalAbuse")
    emotionalAbuse("emotionalAbuse","emotionalAbuse"),
    @JsonProperty("sexualAbuse")
    sexualAbuse("sexualAbuse","sexualAbuse"),
    @JsonProperty("financialAbuse")
    financialAbuse("financialAbuse","financialAbuse"),
    @JsonProperty("abduction")
    abduction("abduction","abduction"),
    @JsonProperty("somethingElse")
    somethingElse("somethingElse","somethingElse"),
    @JsonProperty("witnessingDomesticAbuse")
    witnessingDomesticAbuse("witnessingDomesticAbuse","witnessingDomesticAbuse");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static TypesOfAbusesEnum getValue(String key) {
        return TypesOfAbusesEnum.valueOf(key);
    }
}
