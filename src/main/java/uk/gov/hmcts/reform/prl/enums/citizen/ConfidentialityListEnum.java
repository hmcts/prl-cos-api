package uk.gov.hmcts.reform.prl.enums.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ConfidentialityListEnum {
    @CCD(label = "Telephone number")
    @JsonProperty("phoneNumber")
    phoneNumber("phoneNumber", "Telephone number"),
    @CCD(label = "Email")
    @JsonProperty("email")
    email("email", "Email"),
    @CCD(label = "Address")
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
