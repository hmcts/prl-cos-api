package uk.gov.hmcts.reform.prl.enums.manageorders;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SelectTypeOfOrderEnum {
    @CCD(label = "Interim")
    @JsonProperty("interim")
    interim("interim", "Interim"),
    @CCD(label = "General")
    @JsonProperty("general")
    general("general", "General"),
    @CCD(label = "Final")
    @JsonProperty("finl")
    finl("finl", "Final");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SelectTypeOfOrderEnum getValue(String key) {
        return SelectTypeOfOrderEnum.valueOf(key);
    }
}
