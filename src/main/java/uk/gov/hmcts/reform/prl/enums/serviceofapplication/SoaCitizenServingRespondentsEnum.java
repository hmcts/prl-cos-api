package uk.gov.hmcts.reform.prl.enums.serviceofapplication;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

import java.util.Arrays;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SoaCitizenServingRespondentsEnum {
    @JsonProperty("courtBailiff")
    courtBailiff("courtBailiff", "Court bailiff"),
    @JsonProperty("unrepresentedApplicant")
    unrepresentedApplicant("unrepresentedApplicant", "Unrepresented applicant who is arranging service"),
    @JsonProperty("courtAdmin")
    courtAdmin("courtAdmin", "Court admin"),
    UNKNOWN_VALUE("unknownValue", "Unknown value");

    @Getter
    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SoaCitizenServingRespondentsEnum getValue(String key) {
        return Arrays.stream(values())
            .filter(value -> value.id.equalsIgnoreCase(key) || value.name().equalsIgnoreCase(key))
            .findFirst()
            .orElse(UNKNOWN_VALUE);
    }
}
