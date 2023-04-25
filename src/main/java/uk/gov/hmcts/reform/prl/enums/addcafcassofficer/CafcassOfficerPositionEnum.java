package uk.gov.hmcts.reform.prl.enums.addcafcassofficer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;


@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CafcassOfficerPositionEnum {

    @JsonProperty("cafcassOfficer")
    cafcassOfficer("cafcassOfficer", "Cafcass officer"),
    @JsonProperty("cafacassGuardian")
    cafacassGuardian("cafacassGuardian", "Cafcass guardian"),
    @JsonProperty("cafacassSolicitor")
    cafacassSolicitor("cafacassSolicitor", "Cafcass solicitor"),
    @JsonProperty("cafcassCymruOfficer")
    cafcassCymruOfficer("cafcassCymruOfficer", "Cafcass Cymru officer"),
    @JsonProperty("other")
    other("other", "Other");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static CafcassOfficerPositionEnum getValue(String key) {
        return CafcassOfficerPositionEnum.valueOf(key);
    }
}
