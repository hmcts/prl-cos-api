package uk.gov.hmcts.reform.prl.models.complextypes.confidentiality;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Fl401ChildConfidentialityDetails {
    @JsonProperty("fullName")
    private String fullName;
}
