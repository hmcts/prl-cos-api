package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundleCreateRequest {
    @JsonProperty("caseTypeId")
    public String caseTypeId;
    @JsonProperty("jurisdictionId")
    public String jurisdictionId;
    @JsonProperty("case_details")
    public CaseDetails caseDetails;
    @JsonProperty("event_id")
    private String eventId;
}
