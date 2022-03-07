package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ConfidentialityStatementDisclaimerEnum {

    confidentialityStatementUnderstood("I Understand that "
                                           + "information should be marked as confidential if it is to be kept private");

    private final String displayedValue;

}

