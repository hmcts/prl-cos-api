package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ApplicantOccupationEnum {
    @JsonProperty("occupyAsHome")
    occupyAsHome("occupyAsHome", "is entitled to occupy the address as their home"),

    @JsonProperty("rightsInAddress")
    rightsInAddress("rightsInAddress", "has home rights in the address"),

    @JsonProperty("rightsToEnterAddress")
    rightsToEnterAddress("rightsToEnterAddress", "has the right to enter into and occupy the address"),

    @JsonProperty("other1")
    other1("other1", "other");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ApplicantOccupationEnum getValue(String key) {
        return ApplicantOccupationEnum.valueOf(key);
    }
}
