package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum HearingChannelsEnum {
    @JsonProperty("INTER")
    INTER("INTER", "In person", "Wyneb yn wyneb"),
    @JsonProperty("TEL")
    TEL("TEL", "Telephone", "Ffôn"),
    @JsonProperty("TELBTM")
    TELBTM("TEL", "Telephone", "Ffôn"),
    @JsonProperty("TELCVP")
    TELCVP("TEL", "Telephone", "Ffôn"),
    @JsonProperty("TELSKYP")
    TELSKYP("TEL", "Telephone", "Ffôn"),
    @JsonProperty("TELOTHER")
    TELOTHER("TEL", "Telephone", "Ffôn"),
    @JsonProperty("VID")
    VID("VID", "Video", "Fideo"),
    @JsonProperty("VIDOTHER")
    VIDOTHER("VIDOTHER", "Video", "Fideo"),
    @JsonProperty("VIDSKYPE")
    VIDSKYPE("VIDSKYPE", "Video", "Fideo"),
    @JsonProperty("VIDCVP")
    VIDCVP("VIDCVP", "Video", "Fideo"),
    @JsonProperty("VIDTEAMS")
    VIDTEAMS("VIDTEAMS", "Video", "Fideo"),
    @JsonProperty("VIDVHS")
    VIDVHS("VIDVHS", "Video", "Fideo"),
    @JsonProperty("VIDPVL")
    VIDPVL("VIDPVL", "Video", "Fideo"),
    @JsonProperty("NA")
    NA("NA", "Not in attendance", "Ddim yn Bresennol"),
    @JsonProperty("ONPPRS")
    ONPPRS("ONPPRS", "On the papers","Ar sail y papurau"),
    @JsonProperty("DEFAULT")
    DEFAULT("DEFAULT", "", "");

    private final String id;
    private final String displayedValue;
    private final String displayedValueWelsh;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    public String getDisplayedValueWelsh() {
        return displayedValueWelsh;
    }

    @JsonCreator
    public static HearingChannelsEnum getValue(String key) {
        if (key != null) {
            Optional<HearingChannelsEnum> hearingSubChannel = Arrays.stream(HearingChannelsEnum.values())
                .filter(hearingChannelsEnum -> hearingChannelsEnum.name().equalsIgnoreCase(key)).findFirst();
            if (hearingSubChannel.isPresent()) {
                return HearingChannelsEnum.valueOf(key);
            }
            if (key.contains("TEL")) {
                return HearingChannelsEnum.TEL;
            }
            if (key.contains("VID")) {
                return HearingChannelsEnum.TEL;
            }
        }
        return HearingChannelsEnum.DEFAULT;
    }
}
