package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SDOPreamblesEnum {

    @JsonProperty("rightToAskCourt")
    rightToAskCourt("rightToAskCourt", "Right to ask court to reconsider this order"),

    @JsonProperty("partyRaisedDomesticAbuse")
    partyRaisedDomesticAbuse("partyRaisedDomesticAbuse", "Party/parties raised domestic abuse issues");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SDOPreamblesEnum getValue(String key) {
        return SDOPreamblesEnum.valueOf(key);
    }

}

