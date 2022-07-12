package uk.gov.hmcts.reform.prl.enums.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum NonMolestationEnum {

    nonMolestation("Non-molestation order (FL404A)");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static NonMolestationEnum getValue(String key) {
        return NonMolestationEnum.valueOf(key);
    }

}
