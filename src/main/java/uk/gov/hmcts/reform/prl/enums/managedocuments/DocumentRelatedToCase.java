package uk.gov.hmcts.reform.prl.enums.managedocuments;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;


@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DocumentRelatedToCase {

    @CCD(label = "Yes, the document belongs to the case")
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
