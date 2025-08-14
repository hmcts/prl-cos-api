package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoOtherEnum {

    @JsonProperty("disclosureOfPapers")
    disclosureOfPapers("disclosureOfPapers", "Disclosure of papers from previous proceedings"),

    @JsonProperty("parentWithCare")
    parentWithCare("parentWithCare", "Parent with care can apply to transfer");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoOtherEnum getValue(String key) {
        return SdoOtherEnum.valueOf(key);
    }

}

