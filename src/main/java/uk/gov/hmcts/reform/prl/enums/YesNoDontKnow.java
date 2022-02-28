package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum YesNoDontKnow {

    @JsonProperty("yes")
    yes("yes", "Yes"),
    @JsonProperty("no")
    no("no", "No"),
    @JsonProperty("dontKnow")
    dontKnow("dontKnow", "Don't know");


    private final String id;
    private final String displayedValue;

}
