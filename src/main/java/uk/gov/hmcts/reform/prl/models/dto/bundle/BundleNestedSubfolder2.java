package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundleNestedSubfolder2 {
    private BundleNestedSubfolder2Details value;

    @JsonCreator
    public BundleNestedSubfolder2(@JsonProperty("value") BundleNestedSubfolder2Details value) {
        this.value = value;
    }
}
