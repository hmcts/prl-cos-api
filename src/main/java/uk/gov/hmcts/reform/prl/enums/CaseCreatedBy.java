package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CaseCreatedBy {
    SOLICITOR,
    CITIZEN,
    COURT_STAFF,
    COURT_ADMIN
}
