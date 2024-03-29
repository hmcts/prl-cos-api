package uk.gov.hmcts.reform.prl.enums.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SpecialGuardianShipEnum {

    specialGuardianShip("Special guardianship order (C43A)");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SpecialGuardianShipEnum getValue(String key) {
        return SpecialGuardianShipEnum.valueOf(key);
    }

}
