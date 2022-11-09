package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class C100RebuildUrgencyElements {

    @JsonProperty("hu_urgentHearingReasons")
    private YesOrNo urgentHearingRequired;
    @JsonProperty("hu_reasonOfUrgentHearing")
    private String[] reasonOfUrgentHearing;
    @JsonProperty("hu_otherRiskDetails")
    private String otherRiskDetails;
    @JsonProperty("hu_timeOfHearingDetails")
    private String timeOfHearingDetails;
    @JsonProperty("hu_hearingWithNext48HrsDetails")
    private YesOrNo hearingWithNext48HrsDetails;
    @JsonProperty("hu_hearingWithNext48HrsMsg")
    private String hearingWithNext48HrsMsg;
}