package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DontKnow {

    dontKnow("Don't know");

    private final String displayedValue;

}
