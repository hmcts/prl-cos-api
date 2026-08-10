package uk.gov.hmcts.reform.prl.enums.serviceofapplication;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "SoaServingRespondentsEnum", generate = true)
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SoaSolicitorServingRespondentsEnum {
    @CCD(label = "Applicant's legal representative")
    @JsonProperty("applicantLegalRepresentative")
    applicantLegalRepresentative("applicantLegalRepresentative", "Applicant's legal representative"),
    @CCD(label = "Court bailiff")
    @JsonProperty("courtBailiff")
    courtBailiff("courtBailiff", "Court bailiff"),
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
    public static SoaSolicitorServingRespondentsEnum getValue(String key) {
        return SoaSolicitorServingRespondentsEnum.valueOf(key);
    }
}
