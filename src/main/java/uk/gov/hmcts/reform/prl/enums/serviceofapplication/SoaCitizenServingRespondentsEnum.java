package uk.gov.hmcts.reform.prl.enums.serviceofapplication;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SoaCitizenServingRespondentsEnum {
    @CCD(label = "Court bailiff")
    @JsonProperty("courtBailiff")
    courtBailiff("courtBailiff", "Court bailiff"),
    @CCD(label = "Unrepresented applicant who is arranging service")
    @JsonProperty("unrepresentedApplicant")
    unrepresentedApplicant("unrepresentedApplicant", "Unrepresented applicant who is arranging service"),
    @CCD(label = "Court admin")
    @JsonProperty("courtAdmin")
    courtAdmin("courtAdmin", "Court admin");

    private final String id;
    private final String displayedValue;

    public String getId() {
        return id;
    }

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SoaCitizenServingRespondentsEnum getValue(String key) {
        return SoaCitizenServingRespondentsEnum.valueOf(key);
    }
}
