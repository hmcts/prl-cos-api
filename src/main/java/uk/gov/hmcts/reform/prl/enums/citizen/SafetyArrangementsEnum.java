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
public enum SafetyArrangementsEnum {

    @JsonProperty("waitingroom")
    waitingroom("waitingroom","Separate waiting room"),
    @JsonProperty("separateexitentry")
    separateexitentry("separateexitentry","Separate exits and entrances"),
    @JsonProperty("screens")
    screens("screens","Screens so you and the other people in the case cannot see each other"),
    @JsonProperty("toilet")
    toilet("toilet","Separate toilets"),
    @JsonProperty("advancedview")
    advancedview("advancedview","Advanced viewing of the court"),
    @JsonProperty("videolinks")
    videolinks("videolinks","Video links"),
    @JsonProperty("other")
    other("other","Other"),
    @JsonProperty("nosupport")
    nosupport("nosupport","No, I do not need any extra support at this time");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SafetyArrangementsEnum getValue(String key) {
        return SafetyArrangementsEnum.valueOf(key);
    }
}
