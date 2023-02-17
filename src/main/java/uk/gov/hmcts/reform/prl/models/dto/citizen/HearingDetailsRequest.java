package uk.gov.hmcts.reform.prl.models.dto.citizen;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Data
@Builder
public class HearingDetailsRequest {

    @JsonProperty("caseId")
    private final String caseId;
    @JsonProperty("partyName")
    private final String partyName;
    @JsonProperty("eventId")
    private final String eventId;
    @JsonProperty("partyId")
    private final String partyId;
}
