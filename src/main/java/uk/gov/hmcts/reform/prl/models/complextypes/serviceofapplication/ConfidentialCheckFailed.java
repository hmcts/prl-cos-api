package uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfidentialCheckFailed {

    @CCD(label = "Reject Reason ", searchable = false)
    private String confidentialityCheckRejectReason;

    @CCD(label = "Date Rejected", searchable = false)
    private String dateRejected;
}
