package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ParentalResponsibilityType {

    @JsonProperty("PR_BY_FATHER")
    PR_BY_FATHER("PR_BY_FATHER", "Parental responsibility by the father"),
    @JsonProperty("PR_BY_SECOND_FEMALE_PARENT")
    PR_BY_SECOND_FEMALE_PARENT("PR_BY_SECOND_FEMALE_PARENT", "Parental responsibility by second female parent");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ParentalResponsibilityType getValue(String key) {
        return ParentalResponsibilityType.valueOf(key);
    }
}
