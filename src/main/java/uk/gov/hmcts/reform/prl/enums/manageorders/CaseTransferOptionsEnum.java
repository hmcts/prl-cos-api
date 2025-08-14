package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CaseTransferOptionsEnum {
    @JsonProperty("theHighCourt")
    theHighCourt("theHighCourt", "The high court"),
    @JsonProperty("countyCourt")
    countyCourt("countyCourt", "County court"),
    @JsonProperty("familyProceedingCourt")
    familyProceedingCourt("familyProceedingCourt", "Family proceedings court");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static CaseTransferOptionsEnum getValue(String key) {
        return CaseTransferOptionsEnum.valueOf(key);
    }
}
