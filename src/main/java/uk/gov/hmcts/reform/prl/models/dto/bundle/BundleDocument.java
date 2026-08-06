package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "BundleDocument", generate = false)
@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundleDocument {
    private BundleDocumentDetails value;

    @JsonCreator
    public BundleDocument(@JsonProperty("value") BundleDocumentDetails value) {
        this.value = value;
    }
}