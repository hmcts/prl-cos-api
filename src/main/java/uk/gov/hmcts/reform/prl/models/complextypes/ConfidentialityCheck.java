package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Fl401ConfidentialConsent;

@Data
@Builder
public class ConfidentialityCheck {
    private final Fl401ConfidentialConsent confidentialityConsent;
}
