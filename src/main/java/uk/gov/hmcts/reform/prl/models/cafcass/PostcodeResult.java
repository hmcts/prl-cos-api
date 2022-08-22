package uk.gov.hmcts.reform.prl.models.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class PostcodeResult implements Serializable {
    @JsonProperty("DPA")
    private AddressDetails dpa;
}
