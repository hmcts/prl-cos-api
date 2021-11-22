package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {

    @JsonProperty("female")
    FEMALE("female", "Female"),
    @JsonProperty("male")
    MALE("male", "Male"),
    @JsonProperty("other")
    OTHER("other", "They identify in another way");

    private final String id;
    private final String displayedValue;

}
