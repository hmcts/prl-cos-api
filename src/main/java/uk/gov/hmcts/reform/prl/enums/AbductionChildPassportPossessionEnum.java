package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "abductionChildPassportPossessionEnum", generate = true)
@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum AbductionChildPassportPossessionEnum {

    @CCD(label = "Mother")
    @JsonProperty("mother")
    mother("mother", "Mother"),
    @CCD(label = "Father")
    @JsonProperty("father")
    father("father", "Father"),
    @CCD(label = "Other")
    @JsonProperty("other")
    other("other", "Other");

    private final String id;
    private final String displayedValue;

}
