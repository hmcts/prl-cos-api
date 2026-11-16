package uk.gov.hmcts.reform.prl.models.court;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourtOnboardingResponse {
    private final String epimmsId;
    private final String courtName;
    private final boolean onboarded;
}
