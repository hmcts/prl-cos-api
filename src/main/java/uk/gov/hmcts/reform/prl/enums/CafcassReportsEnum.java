package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CafcassReportsEnum {

    @JsonProperty("courtDirectsLetter")
    courtDirectsLetter("Court directs the type of letters for the hearing"),
    @JsonProperty("safeguardingLetters1")
    safeguardingLetters1("Safeguarding letters"),
    @JsonProperty("privateLawReport")
    privateLawReport("Private Law report Type"),
    @JsonProperty("addendumReport")
    addendumReport("S7 Addendum Report"),
    @JsonProperty("section7Report")
    section7Report("Section 7 Report"),
    @JsonProperty("sixteenReport")
    sixteenReport("Private Law report Type"),
    @JsonProperty("updateSafeguardingLetter")
    updateSafeguardingLetter("Update to Safeguarding Letter"),
    @JsonProperty("safeguardingLetters2")
    safeguardingLetters2("Safeguarding Letter"),
    @JsonProperty("nonS7Report1")
    nonS7Report1("Other Non s7 Report"),
    @JsonProperty("nonS7Report2")
    nonS7Report2("Other Non s7 Report");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static CafcassReportsEnum getValue(String key) {
        return CafcassReportsEnum.valueOf(key);
    }

}

