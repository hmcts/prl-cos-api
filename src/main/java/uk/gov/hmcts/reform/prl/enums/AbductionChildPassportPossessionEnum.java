package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AbductionChildPassportPossessionEnum {

    @JsonProperty("mother")
    MOTHER("mother", "Mother"),
    @JsonProperty("father")
    FATHER("father", "Father"),
    @JsonProperty("other")
    OTHER("other", "Other");

    private final String id;
    private final String displayedValue;
}
