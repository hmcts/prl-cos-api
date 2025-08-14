package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.Fl401ConfidentialConsentEnum;

@Data
@Builder
public class ConfidentialityCheck {
    private final Fl401ConfidentialConsentEnum fl401ConfidentialConsentEnum;
}
