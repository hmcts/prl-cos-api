package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;

@Data
@Getter
@Builder(toBuilder = true)
@Jacksonized
public class PartyDetailsMeta {
    private final PartyEnum partyType;
    private final Integer partyIndex;
    private final PartyDetails partyDetails;
}
