package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class AbuseTypes {
    @CCD(label = " ", searchable = false)
    private final Abuse physicalAbuse;
    @CCD(label = " ", searchable = false)
    private final Abuse psychologicalAbuse;
    @CCD(label = " ", searchable = false)
    private final Abuse emotionalAbuse;
    @CCD(label = " ", searchable = false)
    private final Abuse sexualAbuse;
    @CCD(label = " ", searchable = false)
    private final Abuse financialAbuse;
    @CCD(label = " ", searchable = false)
    private final Abuse somethingElse;
}
