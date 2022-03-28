package uk.gov.hmcts.reform.prl.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserCode {
    private String code;
}
