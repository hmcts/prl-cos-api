package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConfidentialityStatementDisclaimerEnum {

    confidentialityStatementUnderstood("I Understand that "
                                           + "information should be marked as confidential if it is to be kept private");

    private final String displayedValue;

}

