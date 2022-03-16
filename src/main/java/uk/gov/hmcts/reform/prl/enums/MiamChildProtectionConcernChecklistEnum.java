package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamChildProtectionConcernChecklistEnum {

    @JsonProperty("MIAMChildProtectionConcernChecklistEnum_value_1")
    MIAMChildProtectionConcernChecklistEnum_value_1("The subject of enquiries by a local authority under"
                                                        +
                                                        " section 47 of the Children Act 1989 Act"),
    @JsonProperty("MIAMChildProtectionConcernChecklistEnum_value_2")
    MIAMChildProtectionConcernChecklistEnum_value_2("The subject of a child protection plan"
                                                        +
                                                        " put in place by a local authority");

    private final String displayedValue;
}
