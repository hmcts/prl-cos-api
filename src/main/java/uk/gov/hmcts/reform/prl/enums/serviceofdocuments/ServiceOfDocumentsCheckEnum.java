package uk.gov.hmcts.reform.prl.enums.serviceofdocuments;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ServiceOfDocumentsCheckEnum {

    @CCD(label = "A manager needs to check the documents")
    @JsonProperty("managerCheck")
    managerCheck("managerCheck", "A manager needs to check the documents"),

    @CCD(label = "No checks are required")
    @JsonProperty("noCheck")
    noCheck("noCheck", "No checks are required");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ServiceOfDocumentsCheckEnum getValue(String key) {
        return ServiceOfDocumentsCheckEnum.valueOf(key);
    }
}
