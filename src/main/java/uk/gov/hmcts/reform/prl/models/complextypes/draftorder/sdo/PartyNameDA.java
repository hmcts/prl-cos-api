package uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class PartyNameDA {
    private final String partyName;
}
