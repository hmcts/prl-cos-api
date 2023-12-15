package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum Status {

    LIVE,
    REJECTED
}
