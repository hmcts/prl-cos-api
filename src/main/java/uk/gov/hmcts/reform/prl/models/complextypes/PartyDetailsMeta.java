package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;

@Data
@Builder
public class PartyDetailsMeta {
    private final PartyEnum partyType;
    private final Integer partyIndex;
    private final PartyDetails partyDetails;
}
