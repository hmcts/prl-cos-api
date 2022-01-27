package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;

import java.util.List;

@Builder
@Data
public class DomesticAbuse {

    private final String physicalAbuseVictim;
    private final String emotionalAbuseVictim;
    private final String psychologicalAbuseVictim;
    private final String sexualAbuseVictim;
    private final String financialAbuseVictim;
}
