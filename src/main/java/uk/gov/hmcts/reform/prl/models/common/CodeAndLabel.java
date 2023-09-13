package uk.gov.hmcts.reform.prl.models.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CodeAndLabel {
    private String code;
    private String label;
}
