package uk.gov.hmcts.reform.prl.enums.restrictedcaseaccess;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonSerialize
public enum CaseSecurityClassification {
    PUBLIC, PRIVATE, RESTRICTED;
}
