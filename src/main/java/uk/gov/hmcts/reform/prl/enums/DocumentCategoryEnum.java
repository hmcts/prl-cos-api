package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "documentCategoryChecklistEnum", generate = true)
@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DocumentCategoryEnum {

    @CCD(label = "Main application documents(Consent order, MIAM certificates, previous orders)")
    @JsonProperty("documentCategoryChecklistEnumValue1")
    documentCategoryChecklistEnumValue1("documentCategoryChecklistEnumValue1",
                                        "Main application documents(Consent order, MIAM certificates, previous orders)"),
    @CCD(label = "Correspondence")
    @JsonProperty("documentCategoryChecklistEnumValue2")
    documentCategoryChecklistEnumValue2("documentCategoryChecklistEnumValue2", "Correspondence"),
    @CCD(label = "Any other document")
    @JsonProperty("documentCategoryChecklistEnumValue3")
    documentCategoryChecklistEnumValue3("documentCategoryChecklistEnumValue3", "Any other document");

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
