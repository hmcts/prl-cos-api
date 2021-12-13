package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum State {

    @JsonProperty("AWAITING_SUBMISSION_TO_HMCTS")
    AWAITING_SUBMISSION_TO_HMCTS("AWAITING_SUBMISSION_TO_HMCTS");

    private final String value;

}
