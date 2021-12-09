package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PassportPossessionEnum {

    mother("Mother"),
    father("Father"),
    otherPerson("Other");

    private final String displayedValue;
}
