package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundleHearingInfo {
    @JsonProperty("hearingVenueId")
    private final String hearingVenueId;

    @JsonProperty("hearingDateAndTime")
    private final String hearingDateAndTime;

    @JsonProperty("hearingJudgeId")
    private final String hearingJudgeId;
}
