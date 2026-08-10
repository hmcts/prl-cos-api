package uk.gov.hmcts.reform.prl.enums.addcafcassofficer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;


@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CafcassOfficerPositionEnum {

    @CCD(label = "Cafcass officer")
    @JsonProperty("cafcassOfficer")
    cafcassOfficer("cafcassOfficer", "Cafcass officer"),
    @CCD(label = "Cafcass guardian")
    @JsonProperty("cafacassGuardian")
    cafacassGuardian("cafacassGuardian", "Cafcass guardian"),
    @CCD(label = "Cafcass solicitor")
    @JsonProperty("cafacassSolicitor")
    cafacassSolicitor("cafacassSolicitor", "Cafcass solicitor"),
    @CCD(label = "Cafcass Cymru officer")
    @JsonProperty("cafcassCymruOfficer")
    cafcassCymruOfficer("cafcassCymruOfficer", "Cafcass Cymru officer"),
    @CCD(label = "Other")
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
