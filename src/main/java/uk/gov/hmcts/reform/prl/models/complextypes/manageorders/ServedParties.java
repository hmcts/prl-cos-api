package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ServedParties {

    private final String partyId;

    private final String partyName;
}
