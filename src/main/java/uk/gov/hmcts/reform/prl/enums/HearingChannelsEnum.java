package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum HearingChannelsEnum {
    @JsonProperty("INTER")
    INTER("INTER", "In person"),
    @JsonProperty("TEL")
    TEL("TEL", "Telephone"),
    @JsonProperty("TELBTM")
    TELBTM("TEL", "Telephone"),
    @JsonProperty("TELCVP")
    TELCVP("TEL", "Telephone"),
    @JsonProperty("TELSKYP")
    TELSKYP("TEL", "Telephone"),
    @JsonProperty("TELOTHER")
    TELOTHER("TEL", "Telephone"),
    @JsonProperty("VID")
    VID("VID", "Video"),
    @JsonProperty("VIDOTHER")
    VIDOTHER("VIDOTHER", "Video"),
    @JsonProperty("VIDSKYPE")
    VIDSKYPE("VIDSKYPE", "Video"),
    @JsonProperty("VIDCVP")
    VIDCVP("VIDCVP", "Video"),
    @JsonProperty("VIDTEAMS")
    VIDTEAMS("VIDTEAMS", "Video"),
    @JsonProperty("VIDVHS")
    VIDVHS("VIDVHS", "Video"),
    @JsonProperty("VIDPVL")
    VIDPVL("VIDPVL", "Video"),
    @JsonProperty("NA")
    NA("NA","Not in attendance"),
    @JsonProperty("ONPPRS")
    ONPPRS("ONPPRS", "On the papers");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static HearingChannelsEnum getValue(String key) {
        return HearingChannelsEnum.valueOf(key);
    }
}
