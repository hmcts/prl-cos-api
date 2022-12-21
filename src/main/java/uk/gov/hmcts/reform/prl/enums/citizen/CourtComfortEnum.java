package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CourtComfortEnum {

    @JsonProperty("appropriatelighting")
    appropriatelighting("appropriatelighting","Appropriate lighting"),
    @JsonProperty("breaks")
    breaks("breaks", "Regular breaks"),
    @JsonProperty("space")
    space("space","Space to be able to get up and move around"),
    @JsonProperty("other")
    other("other","Other"),
    @JsonProperty("nosupport")
    nosupport("nosupport", "No, I do not need any extra support at this time");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static CourtComfortEnum getValue(String key) {
        return CourtComfortEnum.valueOf(key);
    }
}
