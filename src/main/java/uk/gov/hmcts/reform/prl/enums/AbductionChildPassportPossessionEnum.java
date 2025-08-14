package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum AbductionChildPassportPossessionEnum {

    @JsonProperty("mother")
    mother("mother", "Mother"),
    @JsonProperty("father")
    father("father", "Father"),
    @JsonProperty("other")
    other("other", "Other");

    private final String id;
    private final String displayedValue;

}
