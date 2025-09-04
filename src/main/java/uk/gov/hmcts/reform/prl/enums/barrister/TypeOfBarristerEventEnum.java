package uk.gov.hmcts.reform.prl.enums.barrister;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum TypeOfBarristerEventEnum {

    @JsonProperty("addBarrister")
    addBarrister("addBarrister", "Add Barrister"),
    @JsonProperty("removeBarrister")
    removeBarrister("removeBarrister", "Remove Barrister");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static TypeOfBarristerEventEnum getValue(String key) {
        return TypeOfBarristerEventEnum.valueOf(key);
    }

}
