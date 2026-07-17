package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundleSubfolder {
    @CCD(ignore = true)
    private BundleSubfolderDetails value;

    @JsonCreator
    public BundleSubfolder(@JsonProperty("value") BundleSubfolderDetails value) {
        this.value = value;
    }
}