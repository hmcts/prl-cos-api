package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum YesNoDontKnow {

    @JsonProperty("yes")
    YES("yes", "Yes"),
    @JsonProperty("no")
    NO("no", "No"),
    @JsonProperty("dontKnow")
    DONT_KNOW("dontKnow", "Don't know");


    private final String id;
    private final String displayedValue;


}
