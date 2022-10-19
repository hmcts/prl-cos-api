package uk.gov.hmcts.reform.prl.enums.manageorders;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum YesNoNotRequiredEnum {
    @JsonProperty("Yes")
    yes("yes", "Yes"),
    @JsonProperty("no")
    no("no", "No"),
    @JsonProperty("notRequired")
    notRequired("notRequired", "Not required");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static YesNoNotRequiredEnum getValue(String key) {
        return YesNoNotRequiredEnum.valueOf(key);
    }
}
