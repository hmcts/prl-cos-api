package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@JsonIgnoreProperties(ignoreUnknown = true)
@Value
@Builder(toBuilder = true)
public class BundleHearingInfo {
    @JsonProperty("hearingVenueAddress")
    private final String hearingVenueAddress;

    @JsonProperty("hearingDateAndTime")
    private final String hearingDateAndTime;

    @JsonProperty("hearingJudgeName")
    private final String hearingJudgeName;
}