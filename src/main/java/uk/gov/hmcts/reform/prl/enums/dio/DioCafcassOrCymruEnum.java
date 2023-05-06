package uk.gov.hmcts.reform.prl.enums.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DioCafcassOrCymruEnum {
    @JsonProperty("cafcassSafeguarding")
    cafcassSafeguarding("cafcassSafeguarding", "CAFCASS safeguarding on issue"),
    @JsonProperty("cafcassCymruSafeguarding")
    cafcassCymruSafeguarding("cafcassCymruSafeguarding", "CAFCASS Cymru safeguarding on issue");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DioCafcassOrCymruEnum getValue(String key) {
        return DioCafcassOrCymruEnum.valueOf(key);
    }
}
