package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ConfidentialityStatementDisclaimerEnum {

    @CCD(label = "I understand that information should be marked as confidential if it is to be kept private.")
    confidentialityStatementUnderstood("I Understand that "
                                           + "information should be marked as confidential if it is to be kept private");

    private final String displayedValue;

}

