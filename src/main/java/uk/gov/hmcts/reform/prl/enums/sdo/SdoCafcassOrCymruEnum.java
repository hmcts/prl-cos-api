package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoCafcassOrCymruEnum {

    @JsonProperty("safeguardingCafcassOnly")
    safeguardingCafcassOnly("safeguardingCafcassOnly", "Safeguarding checks: next steps Cafcass"),
    @JsonProperty("safeguardingCafcassCymru")
    safeguardingCafcassCymru("safeguardingCafcassCymru", "Safeguarding checks: next steps Cafcass Cymru"),
    @JsonProperty("partyToProvideDetailsOnly")
    partyToProvideDetailsOnly("partyToProvideDetailsOnly", "Party to provide details of new partner to Cafcass"),
    @JsonProperty("partyToProvideDetailsCmyru")
    partyToProvideDetailsCmyru("partyToProvideDetailsCmyru", "Party to provide details of new partner to Cafcass Cymru"),
    @JsonProperty("section7Report")
    section7Report("section7Report", "Section 7 report/Child impact analysis");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoCafcassOrCymruEnum getValue(String key) {
        return SdoCafcassOrCymruEnum.valueOf(key);
    }

}

