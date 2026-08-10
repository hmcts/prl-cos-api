package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "CaseCreatedByEnum", generate = true)
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CaseCreatedBy {
    SOLICITOR,
    CITIZEN,
    COURT_ADMIN
}
