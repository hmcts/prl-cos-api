package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ChildTypeOfAbuse", generate = true)
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum ChildAbuseEnum {

    @CCD(label = "Physical abuse")
    @JsonProperty("physicalAbuse")
    physicalAbuse("physicalAbuse","Physical abuse"),

    @CCD(label = "Psychological abuse")
    @JsonProperty("psychologicalAbuse")
    psychologicalAbuse("psychologicalAbuse","Psychological abuse"),

    @CCD(label = "Sexual abuse")
    @JsonProperty("sexualAbuse")
    sexualAbuse("sexualAbuse","Sexual abuse"),

    @CCD(label = "Emotional abuse")
    @JsonProperty("emotionalAbuse")
    emotionalAbuse("emotionalAbuse","Emotional abuse"),

    @CCD(label = "Financial abuse")
    @JsonProperty("financialAbuse")
    financialAbuse("financialAbuse","Financial abuse");

    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static ChildAbuseEnum getValue(String key) {
        return ChildAbuseEnum.valueOf(key);
    }

}
