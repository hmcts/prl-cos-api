package uk.gov.hmcts.reform.prl.models.dto.bundle.stitch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleDocumentDetails;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class Bundle {


    private List<Element<BundleDocumentDetails>> documents;
}
