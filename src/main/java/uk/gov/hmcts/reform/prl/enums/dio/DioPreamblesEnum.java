package uk.gov.hmcts.reform.prl.enums.dio;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DioPreamblesEnum {

    @CCD(label = "Right to ask court to reconsider this order")
    @JsonProperty("rightToAskCourt")
    rightToAskCourt("rightToAskCourt", "Right to ask court to reconsider this order"),

    @CCD(label = "Party or parties raising domestic abuse issues")
    @JsonProperty("partyRaisedDomesticAbuse")
    partyRaisedDomesticAbuse("partyRaisedDomesticAbuse", "Party or parties raising domestic abuse issues");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DioPreamblesEnum getValue(String key) {
        return DioPreamblesEnum.valueOf(key);
    }

}
