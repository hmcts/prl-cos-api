package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DocumentCategoryEnum {

    @JsonProperty("documentCategoryChecklistEnumValue1")
    MAINAPPLICATION("documentCategoryChecklistEnumValue1", "Main application documents(Consent order, MIAM certificates, previous orders)"),
    @JsonProperty("documentCategoryChecklistEnumValue2")
    CORRESPONDENCE("documentCategoryChecklistEnumValue2", "Correspondence"),
    @JsonProperty("documentCategoryChecklistEnumValue3")
    OTHER("documentCategoryChecklistEnumValue3", "Any other document");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DocumentCategoryEnum getValue(String key) {
        return DocumentCategoryEnum.valueOf(key);
    }
}
