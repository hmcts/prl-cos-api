package uk.gov.hmcts.reform.prl.models.cafcass;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostcodeResult {
    @JsonProperty("DPA")
    private AddressDetails dpa;
}
