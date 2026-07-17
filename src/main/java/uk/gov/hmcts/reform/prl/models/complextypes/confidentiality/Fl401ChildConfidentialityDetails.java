package uk.gov.hmcts.reform.prl.models.complextypes.confidentiality;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Builder
@Data
public class Fl401ChildConfidentialityDetails {
    @CCD(label = "Child's name:", searchable = false)
    private String fullName;
}
