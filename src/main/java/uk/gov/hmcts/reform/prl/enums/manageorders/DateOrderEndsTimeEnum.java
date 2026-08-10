package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "dateOrderEndsTimeEnum", generate = true)
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DateOrderEndsTimeEnum {

    @CCD(label = "No fixed end date")
    @JsonProperty("noEndDate")
    noEndDate("noEndDate", "No fixed end date"),
    @CCD(label = "Until the next hearing")
    @JsonProperty("untilNextHearing")
    untilNextHearing("untilNextHearing", "Until the next hearing"),
    @CCD(label = "Specific date and time")
    @JsonProperty("specifiedDateAndTime")
    specifiedDateAndTime("specifiedDateAndTime", "Specific date and time");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DateOrderEndsTimeEnum getValue(String key) {
        return DateOrderEndsTimeEnum.valueOf(key);
    }
}
