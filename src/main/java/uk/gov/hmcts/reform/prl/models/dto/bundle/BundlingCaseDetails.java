package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundlingCaseDetails {

    @JsonProperty("case_data")
    public BundlingCaseData caseData;

    @JsonProperty("bundle_name")
    public String bundleName;

}
