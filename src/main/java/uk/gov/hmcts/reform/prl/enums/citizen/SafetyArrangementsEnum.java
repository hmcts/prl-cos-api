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
public enum SafetyArrangementsEnum {

    @CCD(label = "Separate waiting room")
    @JsonProperty("waitingroom")
    waitingroom("waitingroom","Separate waiting room"),
    @CCD(label = "Separate exits and entrances")
    @JsonProperty("separateexitentry")
    separateexitentry("separateexitentry","Separate exits and entrances"),
    @CCD(label = "Screens so you and the other people in the case cannot see each other")
    @JsonProperty("screens")
    screens("screens","Screens so you and the other people in the case cannot see each other"),
    @CCD(label = "Separate toilets")
    @JsonProperty("separatetoilets")
    separatetoilets("separatetoilets","Separate toilets"),
    @CCD(label = "Advanced viewing of the court")
    @JsonProperty("advancedview")
    advancedview("advancedview","Advanced viewing of the court"),
    @CCD(label = "Visit to court before the hearing")
    @JsonProperty("visitToCourt")
    visitToCourt("visitToCourt", "Visit to court before the hearing"),
    @CCD(label = "Video links")
    @JsonProperty("videolinks")
    videolinks("videolinks","Video links"),
    @CCD(label = "Other")
    @JsonProperty("other")
    other("other","Other"),
    @CCD(label = "No, I do not need any extra support at this time")
    @JsonProperty("noSafetyrequirements")
    noSafetyrequirements("noSafetyrequirements","No, I do not need any extra support at this time");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SafetyArrangementsEnum getValue(String key) {
        return SafetyArrangementsEnum.valueOf(key);
    }
}
