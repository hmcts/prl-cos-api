package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class AbuseTypes {
    private final Abuse physicalAbuse;
    private final Abuse psychologicalAbuse;
    private final Abuse emotionalAbuse;
    private final Abuse sexualAbuse;
    private final Abuse financialAbuse;
    private final Abuse somethingElse;
}
