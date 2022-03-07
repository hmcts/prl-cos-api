package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum RestrictToCafcassHmcts {
    @JsonProperty("restrictToGroup")
    RESTRICTTOGROUP("Yes - restrict to this group");

    private final String displayedValue;

}
