package uk.gov.hmcts.reform.prl.enums.noticeofchange;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum TypeOfNocEventEnum {

    @JsonProperty("addLegalRepresentation")
    addLegalRepresentation("addLegalRepresentation", "Add Legal Representation"),
    @JsonProperty("removeLegalRepresentation")
    removeLegalRepresentation("removeLegalRepresentation", "Remove Legal Representation");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static TypeOfNocEventEnum getValue(String key) {
        return TypeOfNocEventEnum.valueOf(key);
    }

}
