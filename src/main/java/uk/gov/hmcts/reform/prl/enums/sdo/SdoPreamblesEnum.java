package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoPreamblesEnum {

    @JsonProperty("rightToAskCourt")
    rightToAskCourt("rightToAskCourt", "Right to ask court to reconsider this order"),
    @JsonProperty("afterSecondGateKeeping")
    afterSecondGateKeeping("afterSecondGateKeeping", "After second gatekeeping appointment"),
    @JsonProperty("addNewPreamble")
    addNewPreamble("addNewPreamble", "Add a new preamble");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoPreamblesEnum getValue(String key) {
        return SdoPreamblesEnum.valueOf(key);
    }

}

