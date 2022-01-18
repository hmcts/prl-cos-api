package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RestrictToCafcassHmcts {

    RESTRICTTOGROUP("Yes - restrict to this group");

    private final String displayedValue;

}
