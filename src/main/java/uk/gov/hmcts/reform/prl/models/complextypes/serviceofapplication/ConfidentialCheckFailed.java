package uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfidentialCheckFailed {

    private List<Element<String>> confidentialityCheckRejectReason;

    private String dateRejected;
}
