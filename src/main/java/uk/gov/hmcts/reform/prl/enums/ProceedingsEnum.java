package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProceedingsEnum {

    @JsonProperty("ongoing")
    ongoing("ongoing", "Ongoing"),
    @JsonProperty("previous")
    previous("previous", "Previous");

    private final String id;
    private final String displayedValue;
}
