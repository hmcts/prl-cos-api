package uk.gov.hmcts.reform.prl.enums.managedocuments;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;


@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DocumentRelatedToCase {

    @JsonProperty("relatedToCase")
    RELATED_TO_CASE("Yes, the document belongs to the case");

    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DocumentRelatedToCase getValue(String key) {
        return DocumentRelatedToCase.valueOf(key);
    }

}
