package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class PartyNameDA {
    private final String partyName;
}
