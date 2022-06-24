package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
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

}
