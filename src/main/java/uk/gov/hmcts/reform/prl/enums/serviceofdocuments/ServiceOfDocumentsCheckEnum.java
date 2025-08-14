package uk.gov.hmcts.reform.prl.enums.serviceofdocuments;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ServiceOfDocumentsCheckEnum {

    @JsonProperty("managerCheck")
    managerCheck("managerCheck", "A manager needs to check the documents"),

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
