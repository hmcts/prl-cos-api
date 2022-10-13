package uk.gov.hmcts.reform.prl.enums.manageorders;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SelectTypeOfOrderEnum {
    @JsonProperty("interim")
    interim("interim", "Interim"),
    @JsonProperty("general")
    general("general", "General"),
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
