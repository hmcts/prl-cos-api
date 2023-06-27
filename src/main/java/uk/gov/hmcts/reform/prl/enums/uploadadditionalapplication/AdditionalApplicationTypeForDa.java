package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum AdditionalApplicationTypeForDa {

    @JsonProperty("otherOrder")
    otherOrder("otherOrder", "Other specific order - including C1 and C100 orders, and supplements");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static AdditionalApplicationTypeForDa getValue(String key) {
        return AdditionalApplicationTypeForDa.valueOf(key);
    }

}
