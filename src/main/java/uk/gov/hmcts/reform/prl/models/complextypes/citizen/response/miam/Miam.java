package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Miam {
    @JsonProperty("attendedMiam")
    private final YesOrNo attendedMiam;
    @JsonProperty("willingToAttendMiam")
    private final YesOrNo willingToAttendMiam;
    @JsonProperty("reasonNotAttendingMiam")
    private final String reasonNotAttendingMiam;
}
