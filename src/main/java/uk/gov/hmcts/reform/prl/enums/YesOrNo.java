package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum YesOrNo {

    @JsonProperty("Yes")
    Yes("Yes"),

    @JsonProperty("No")
    No("No");

    private final String value;


}
