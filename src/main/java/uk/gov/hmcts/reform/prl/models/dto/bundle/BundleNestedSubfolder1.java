package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundleNestedSubfolder1 {
    private BundleNestedSubfolder1Details value;

    @JsonCreator
    public BundleNestedSubfolder1(@JsonProperty("value") BundleNestedSubfolder1Details value) {
        this.value = value;
    }
}