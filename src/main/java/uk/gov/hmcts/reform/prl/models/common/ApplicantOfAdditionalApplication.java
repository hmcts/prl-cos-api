package uk.gov.hmcts.reform.prl.models.common;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ApplicantOfAdditionalApplication {
    String code;
    String name;
}
