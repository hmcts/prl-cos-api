package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Miam {
    private final YesOrNo attendedMiam;
    private final YesOrNo willingToAttendMiam;
    private final String reasonNotAttendingMiam;
}
