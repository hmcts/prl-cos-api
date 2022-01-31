package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DomesticAbuseVictim {

    private final String physicalAbuseVictim;
    private final String emotionalAbuseVictim;
    private final String psychologicalAbuseVictim;
    private final String sexualAbuseVictim;
    private final String financialAbuseVictim;
}
