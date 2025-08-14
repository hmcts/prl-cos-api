package uk.gov.hmcts.reform.prl.models.dto.hearingmanagement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Data
@Builder(toBuilder = true)
public class NextHearingDateRequest {

    @JsonProperty("caseRef")
    private final String caseRef;
    private final NextHearingDetails nextHearingDetails;
}
