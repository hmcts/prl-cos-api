package uk.gov.hmcts.reform.prl.enums.caseflags;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum FlagsVisibiltyEnum {

    INTERNAL("Internal"),
    EXTERNAL("External");

    private final String label;
}
