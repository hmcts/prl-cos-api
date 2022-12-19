package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class SafetyConcerns {
    private final AbuseTypes child;
    private final AbuseTypes applicant;
    private final AbuseTypes respondent;
}
