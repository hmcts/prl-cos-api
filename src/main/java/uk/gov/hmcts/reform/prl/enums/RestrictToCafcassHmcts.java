package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RestrictToCafcassHmcts {
    @JsonProperty("restrictToGroup")
    RESTRICTTOGROUP("Yes - restrict to this group");

    private final String displayedValue;

}
