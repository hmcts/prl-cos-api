package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ConfidentialityListEnum {
    @JsonProperty("phoneNumber")
    phoneNumber("phoneNumber", "Telephone number"),
    @JsonProperty("email")
    email("email", "Email"),
    @JsonProperty("address")
    address("address", "Address");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ConfidentialityListEnum getValue(String key) {
        return ConfidentialityListEnum.valueOf(key);
    }
}
