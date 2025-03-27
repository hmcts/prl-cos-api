package uk.gov.hmcts.reform.prl.models.caselink;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class AutomatedHearingCaseLinkReason {
    @JsonProperty("Reason")
    public String reason;
}
