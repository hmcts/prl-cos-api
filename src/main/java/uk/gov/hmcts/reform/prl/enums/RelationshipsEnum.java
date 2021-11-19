package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RelationshipsEnum {

    FATHER("father", "Father"),
    MOTHER("mother", "Mother"),
    STEPFATHER("stepfather", "Step-father"),
    STEPMOTHER("stepMother", "Step-mother"),
    GRANDPARENT("grandparent", "Grandparent"),
    GUARDIAN("guardian", "Guiardian"),
    SPECIAL_GUARDIAN("specialGuardian", "Special Guardian"),
    OTHER("other", "Other");

    private final String id;
    private final String displayedValue;

}
